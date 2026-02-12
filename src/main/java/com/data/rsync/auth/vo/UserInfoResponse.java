package com.data.rsync.auth.vo;

import lombok.Data;
import java.util.List;

/**
 * 用户信息响应对象
 */
@Data
public class UserInfoResponse {
    private String username;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String status;
    private List<String> roles;
}
