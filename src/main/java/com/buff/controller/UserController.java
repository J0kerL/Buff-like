package com.buff.controller;

import com.buff.common.Result;
import com.buff.service.FileService;
import com.buff.service.UserService;
import com.buff.model.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 * @author Administrator
 */
@Tag(name = "用户管理", description = "用户信息相关接口")
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    @Operation(summary = "绑定Steam账号", description = "绑定用户的Steam账号")
    @PostMapping("/bind-steam")
    public Result<Void> bindSteam(
            @Parameter(description = "Steam ID", example = "76561198000000001")
            @NotBlank(message = "Steam ID不能为空")
            @RequestParam String steamId) {
        userService.bindSteam(steamId);
        return Result.success();
    }

    @Operation(summary = "上传头像", description = "上传用户头像图片到OSS并更新用户头像")
    @PostMapping("/avatar")
    public Result<UserVO> uploadAvatar(
            @Parameter(description = "头像图片文件")
            @RequestParam("file") MultipartFile file) {
        // 上传文件到OSS
        String avatarUrl = fileService.upload(file);
        // 更新用户头像
        UserVO userVO = userService.updateAvatar(avatarUrl);
        return Result.success(userVO);
    }
}
