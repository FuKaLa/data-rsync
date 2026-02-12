package com.data.rsync.auth.vo;

import lombok.Data;

/**
 * 刷新token响应对象
 */
@Data
public class RefreshTokenResponse {
    private String token;
    private String refreshToken;
}
