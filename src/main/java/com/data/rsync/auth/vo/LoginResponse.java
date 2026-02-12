package com.data.rsync.auth.vo;

import lombok.Data;
import com.data.rsync.auth.model.User;

/**
 * 登录响应对象
 */
@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private User user;
}
