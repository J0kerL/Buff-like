package com.buff.service;

import com.buff.model.dto.LoginDTO;
import com.buff.model.dto.RegisterDTO;
import com.buff.model.vo.LoginVO;

/**
 * 认证服务接口
 *
 * @author Administrator
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含token和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    LoginVO register(RegisterDTO registerDTO);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的Token和用户信息
     */
    LoginVO refresh(String refreshToken);

    /**
     * 登出
     *
     * @param refreshToken 刷新Token
     */
    void logout(String refreshToken);

    /**
     * 发送验证码
     *
     * @param mobile 手机号
     */
    String sendCode(String mobile);

    /**
     * 验证验证码
     *
     * @param mobile 手机号
     * @param code   验证码
     * @return 是否正确
     */
    boolean verifyCode(String mobile, String code);
}
