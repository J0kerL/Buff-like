package com.buff.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.buff.common.ResultCode;
import com.buff.config.JwtProperties;
import com.buff.constant.RedisKey;
import com.buff.model.dto.LoginDTO;
import com.buff.model.dto.RegisterDTO;
import com.buff.model.entity.User;
import com.buff.exception.BusinessException;
import com.buff.mapper.UserMapper;
import com.buff.service.AuthService;
import com.buff.util.JwtUtils;
import com.buff.util.RedisUtils;
import com.buff.model.vo.LoginVO;
import com.buff.model.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * 认证服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final JwtProperties jwtProperties;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户是否存在
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 使用BCrypt验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 构造返回结果
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        String token = jwtUtils.generateToken(user.getId());
        String refreshToken = createRefreshToken(user.getId());
        return new LoginVO(token, refreshToken, userVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 验证验证码
        if (!verifyCode(registerDTO.getMobile(), registerDTO.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "验证码错误");
        }

        // 检查用户名是否存在
        User existUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查手机号是否存在
        if (registerDTO.getMobile() != null) {
            existUser = userMapper.selectByMobile(registerDTO.getMobile());
            if (existUser != null) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "手机号已被注册");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 使用BCrypt加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setMobile(registerDTO.getMobile());
        user.setAvatar("https://buff-like.oss-cn-beijing.aliyuncs.com/defaultAvatar.png");
        user.setBalance(BigDecimal.ZERO);
        user.setVersion(0);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "注册失败");
        }

        // 删除验证码
        redisUtils.delete(RedisKey.getSmsCodeKey(registerDTO.getMobile()));

        // 构造返回结果
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        String token = jwtUtils.generateToken(user.getId());
        String refreshToken = createRefreshToken(user.getId());

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return new LoginVO(token, refreshToken, userVO);
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        // 检查refreshToken是否为空或仅包含空白字符
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            // 如果refreshToken为空，则抛出参数错误异常
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "refreshToken不能为空");
        }

        // 根据传入的refreshToken生成Redis中的键名
        String key = RedisKey.getRefreshTokenKey(refreshToken);
        // 从Redis中获取与refreshToken关联的用户ID值
        Object userIdValue = redisUtils.get(key);
        // 检查Redis中是否存在该refreshToken对应的用户信息
        if (userIdValue == null) {
            // 如果没有找到对应的用户信息，则抛出令牌无效异常
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 将获取到的用户ID值转换为Long类型
        Long userId = toLong(userIdValue);
        // 检查转换后的用户ID是否有效
        if (userId == null) {
            // 如果用户ID无效，则抛出令牌无效异常
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 从Redis中删除已使用的refreshToken，确保refreshToken只能使用一次
        redisUtils.delete(key);

        // 根据用户ID查询数据库中的用户信息
        User user = userMapper.selectById(userId);
        // 检查用户是否存在
        if (user == null) {
            // 如果用户不存在，则抛出用户未找到异常
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 生成新的访问令牌(access token)
        String newAccessToken = jwtUtils.generateToken(userId);
        // 生成新的刷新令牌(refresh token)
        String newRefreshToken = createRefreshToken(userId);

        // 创建用户视图对象用于返回用户信息
        UserVO userVO = new UserVO();
        // 复制用户实体的属性到用户视图对象
        BeanUtils.copyProperties(user, userVO);

        // 返回包含新访问令牌、新刷新令牌和用户信息的登录结果对象
        return new LoginVO(newAccessToken, newRefreshToken, userVO);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "refreshToken不能为空");
        }
        redisUtils.delete(RedisKey.getRefreshTokenKey(refreshToken));
    }

    @Override
    public String sendCode(String mobile) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 存储到Redis，有效期5分钟
        redisUtils.set(RedisKey.getSmsCodeKey(mobile), code, 300);

        // 实际项目中应该调用短信服务发送验证码
        log.info("发送验证码: mobile={}, code={}", mobile, code);

        return code;
    }

    @Override
    public boolean verifyCode(String mobile, String code) {
        String cachedCode = (String) redisUtils.get(RedisKey.getSmsCodeKey(mobile));
        return code != null && code.equals(cachedCode);
    }

    private String createRefreshToken(Long userId) {
        Long refreshExpiration = jwtProperties.getRefreshExpiration();
        if (refreshExpiration == null || refreshExpiration <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "refreshExpiration配置无效");
        }

        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        long ttlSeconds = refreshExpiration / 1000;
        if (ttlSeconds <= 0) {
            ttlSeconds = 1;
        }

        boolean ok = redisUtils.set(RedisKey.getRefreshTokenKey(refreshToken), userId, ttlSeconds);
        if (!ok) {
            throw new BusinessException(ResultCode.REDIS_ERROR.getCode(), "写入Refresh Token失败");
        }

        return refreshToken;
    }

    private Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String v = (String) value;
            if (v.trim().isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(v);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (value != null) {
            try {
                return Long.parseLong(Objects.toString(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
