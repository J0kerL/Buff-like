package com.buff.service.impl;

import com.buff.common.ResultCode;
import com.buff.model.entity.User;
import com.buff.exception.BusinessException;
import com.buff.mapper.UserMapper;
import com.buff.service.UserService;
import com.buff.util.UserContext;
import com.buff.model.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return getUserById(userId);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSteam(String steamId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查是否已绑定
        if (user.getSteamId() != null && !user.getSteamId().isEmpty()) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "已绑定Steam账号");
        }

        // 更新Steam ID
        user.setSteamId(steamId);
        int result = userMapper.updateById(user);

        if (result > 0) {
            log.info("用户绑定Steam成功: userId={}, steamId={}", userId, steamId);
        }

    }
}
