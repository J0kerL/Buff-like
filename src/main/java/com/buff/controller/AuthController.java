package com.buff.controller;

import com.buff.common.Result;
import com.buff.model.dto.LoginDTO;
import com.buff.model.dto.RegisterDTO;
import com.buff.service.AuthService;
import com.buff.model.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * @author Administrator
 */
@Tag(name = "认证管理", description = "用户登录、注册相关接口")
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success(loginVO);
    }

    @Operation(summary = "刷新令牌", description = "使用Refresh Token换取新的Access Token和Refresh Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(
            @Parameter(description = "Refresh Token")
            @NotBlank(message = "refreshToken不能为空")
            @RequestParam String refreshToken) {
        LoginVO loginVO = authService.refresh(refreshToken);
        return Result.success(loginVO);
    }

    @Operation(summary = "退出登录", description = "注销Refresh Token（Access Token 会在短时间后自然过期）")
    @PostMapping("/logout")
    public Result<Void> logout(
            @Parameter(description = "Refresh Token")
            @NotBlank(message = "refreshToken不能为空")
            @RequestParam String refreshToken) {
        authService.logout(refreshToken);
        return Result.success();
    }

    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码")
    @PostMapping("/code")
    public Result<String> sendCode(
            @Parameter(description = "手机号", example = "13800000001")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String mobile) {
        String code = authService.sendCode(mobile);
        return Result.success(code);
    }
}
