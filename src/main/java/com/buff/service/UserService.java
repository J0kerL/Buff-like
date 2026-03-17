package com.buff.service;

import com.buff.model.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author Administrator
 */
public interface UserService {

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 绑定Steam账号
     *
     * @param steamId Steam ID
     */
    void bindSteam(String steamId);

    /**
     * 更新用户头像
     *
     * @param avatarUrl 头像URL
     * @return 更新后的用户信息
     */
    UserVO updateAvatar(String avatarUrl);
}
