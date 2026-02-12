package com.data.rsync.auth.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.data.rsync.common.utils.RedisCacheManager;

/**
 * JWT工具类，用于生成和验证token
 */
@Component
public class JwtUtils implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:86400000}")
    private long jwtRefreshExpiration;

    @Value("${jwt.issuer:data-rsync-system}")
    private String jwtIssuer;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisCacheManager redisCacheManager;

    private SecretKey signingKey;

    /**
     * 初始化方法，在Bean初始化时执行
     */
    @Override
    public void afterPropertiesSet() {
        initSigningKey();
        logger.info("JwtUtils initialized successfully with signing key");
    }

    /**
     * 初始化签名密钥
     */
    public void initSigningKey() {
        try {
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                // 生成安全的随机密钥
                signingKey = MacProvider.generateKey(SignatureAlgorithm.HS256);
                jwtSecret = Base64.getEncoder().encodeToString(signingKey.getEncoded());
                logger.info("Generated secure random JWT secret key");
            } else {
                // 尝试从配置的密钥创建
                try {
                    // 先尝试Base64解码
                    byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
                    if (keyBytes.length >= 32) {
                        signingKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
                        logger.debug("Initialized JWT signing key from Base64 encoded configuration");
                    } else {
                        logger.warn("JWT secret key length is insufficient, generating a new secure key");
                        signingKey = MacProvider.generateKey(SignatureAlgorithm.HS256);
                        jwtSecret = Base64.getEncoder().encodeToString(signingKey.getEncoded());
                    }
                } catch (IllegalArgumentException e) {
                    // 如果不是Base64编码，使用原始密钥
                    byte[] keyBytes = jwtSecret.getBytes();
                    if (keyBytes.length >= 32) {
                        signingKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
                        logger.debug("Initialized JWT signing key from configuration");
                    } else {
                        logger.warn("JWT secret key length is insufficient, generating a new secure key");
                        signingKey = MacProvider.generateKey(SignatureAlgorithm.HS256);
                        jwtSecret = Base64.getEncoder().encodeToString(signingKey.getEncoded());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to initialize JWT signing key", e);
            throw new RuntimeException("Failed to initialize JWT signing key", e);
        }
    }

    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        if (signingKey == null) {
            initSigningKey();
        }
        return signingKey;
    }

    /**
     * 生成JWT token
     * @param subject 主体（通常是用户名）
     * @param claims 自定义声明
     * @return JWT token
     * @throws IllegalArgumentException 参数错误时抛出
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }

        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + jwtExpiration);

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuer(jwtIssuer)
                    .setIssuedAt(now)
                    .setExpiration(expirationDate)
                    .signWith(SignatureAlgorithm.HS256, getSigningKey())
                    .compact();

            logger.debug("Generated JWT token for subject: {}", subject);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate JWT token for subject: {}", subject, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * 生成刷新token
     * @param subject 主体（通常是用户名）
     * @return 刷新token
     * @throws IllegalArgumentException 参数错误时抛出
     */
    public String generateRefreshToken(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }

        try {
            Date now = new Date();
            Date expirationDate = new Date(now.getTime() + jwtRefreshExpiration);

            String token = Jwts.builder()
                    .setSubject(subject)
                    .setIssuer(jwtIssuer)
                    .setIssuedAt(now)
                    .setExpiration(expirationDate)
                    .signWith(SignatureAlgorithm.HS256, getSigningKey())
                    .compact();

            logger.debug("Generated refresh token for subject: {}", subject);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate refresh token for subject: {}", subject, e);
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }

    /**
     * 验证JWT token
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Token is null or empty");
            return false;
        }

        try {
            // 检查token是否在黑名单中
            if (isTokenInBlacklist(token)) {
                logger.warn("Token is in blacklist: {}", token.substring(0, 20) + "...");
                return false;
            }

            Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token);
            logger.debug("Token validation successful");
            return true;
        } catch (SignatureException e) {
            logger.warn("Token signature validation failed", e);
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("Token format is invalid", e);
            return false;
        } catch (ExpiredJwtException e) {
            logger.warn("Token has expired", e);
            return false;
        } catch (UnsupportedJwtException e) {
            logger.warn("Token type is unsupported", e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Token argument is invalid", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            return false;
        }
    }

    /**
     * 解析JWT token
     * @param token JWT token
     * @return Claims对象
     * @throws Exception 解析异常
     */
    public Claims parseToken(String token) throws Exception {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            Claims claims = Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(token).getBody();
            logger.debug("Token parsed successfully for subject: {}", claims.getSubject());
            return claims;
        } catch (Exception e) {
            logger.error("Failed to parse token: {}", token.substring(0, 20) + "...", e);
            throw e;
        }
    }

    /**
     * 从token中获取主体
     * @param token JWT token
     * @return 主体
     */
    public String getSubject(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            logger.warn("Failed to get subject from token", e);
            return null;
        }
    }

    /**
     * 从token中获取过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        try {
            return parseToken(token).getExpiration();
        } catch (Exception e) {
            logger.warn("Failed to get expiration date from token", e);
            return null;
        }
    }

    /**
     * 从token中获取签发时间
     * @param token JWT token
     * @return 签发时间
     */
    public Date getIssuedAt(String token) {
        try {
            return parseToken(token).getIssuedAt();
        } catch (Exception e) {
            logger.warn("Failed to get issued at time from token", e);
            return null;
        }
    }

    /**
     * 从token中获取自定义声明
     * @param token JWT token
     * @param claimName 声明名称
     * @return 声明值
     */
    public Object getClaim(String token, String claimName) {
        if (claimName == null || claimName.trim().isEmpty()) {
            logger.warn("Claim name cannot be null or empty");
            return null;
        }

        try {
            return parseToken(token).get(claimName);
        } catch (Exception e) {
            logger.warn("Failed to get claim '{}' from token", claimName, e);
            return null;
        }
    }

    /**
     * 将token加入黑名单
     * @param token JWT token
     */
    public void blacklistToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Token cannot be null or empty for blacklisting");
            return;
        }

        try {
            Claims claims = parseToken(token);
            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;

            if (ttl > 0) {
                // 将token加入Redis黑名单，设置过期时间为token的剩余有效期
                redisCacheManager.set("jwt:blacklist:" + token, "", ttl);
                logger.debug("Token added to blacklist with TTL: {}ms", ttl);
            } else {
                logger.debug("Token already expired, no need to add to blacklist");
            }
        } catch (ExpiredJwtException e) {
            logger.debug("Token already expired, skipping blacklist", e);
        } catch (Exception e) {
            logger.error("Failed to add token to blacklist", e);
        }
    }

    /**
     * 检查token是否在黑名单中
     * @param token JWT token
     * @return 是否在黑名单中
     */
    public boolean isTokenInBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Token cannot be null or empty for blacklist check");
            return false;
        }

        try {
            boolean inBlacklist = redisCacheManager.exists("jwt:blacklist:" + token);
            logger.debug("Token blacklist check result: {}", inBlacklist);
            return inBlacklist;
        } catch (Exception e) {
            logger.error("Failed to check token in blacklist", e);
            // 当Redis不可用时，默认返回false，避免影响正常认证流程
            return false;
        }
    }

    /**
     * 从Authorization头中提取token
     * @param authorizationHeader Authorization头
     * @return token
     */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(tokenPrefix)) {
            String token = authorizationHeader.substring(tokenPrefix.length());
            logger.debug("Token extracted successfully from Authorization header");
            return token;
        }
        logger.debug("No token found in Authorization header");
        return null;
    }

    /**
     * 刷新token
     * @param refreshToken 刷新token
     * @return 新的访问token
     */
    public String refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.warn("Refresh token cannot be null or empty");
            return null;
        }

        try {
            if (!validateToken(refreshToken)) {
                logger.warn("Refresh token validation failed");
                return null;
            }

            String subject = getSubject(refreshToken);
            if (subject == null) {
                logger.warn("Failed to get subject from refresh token");
                return null;
            }

            String newToken = generateToken(subject, null);
            logger.debug("Token refreshed successfully for subject: {}", subject);
            return newToken;
        } catch (Exception e) {
            logger.error("Failed to refresh token", e);
            return null;
        }
    }

    /**
     * 获取token前缀
     * @return token前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }
}
