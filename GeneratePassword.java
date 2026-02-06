import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成 BCrypt 加密的密码
 */
public class GeneratePassword {

    public static void main(String[] args) {
        // 创建 BCryptPasswordEncoder 实例
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 生成 admin123 的加密密码
        String rawPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 输出结果
        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt 加密密码: " + encodedPassword);
        
        // 验证密码是否正确
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
    }

}
