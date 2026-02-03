package com.buff.util;

/**
 * 用户上下文工具类
 * 用于在请求线程中存储和获取当前登录用户信息
 *
 * @author Administrator
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
