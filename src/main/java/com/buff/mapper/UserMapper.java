package com.buff.mapper;

import com.buff.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 *
 * @author Administrator
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    User selectByMobile(@Param("mobile") String mobile);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int updateById(User user);

    /**
     * 更新用户余额（乐观锁）
     */
    int updateBalance(@Param("id") Long id,
                      @Param("balance") java.math.BigDecimal balance,
                      @Param("version") Integer version);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}
