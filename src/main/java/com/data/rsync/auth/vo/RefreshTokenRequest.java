package com.data.rsync.auth.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 刷新token请求对象
 */
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
