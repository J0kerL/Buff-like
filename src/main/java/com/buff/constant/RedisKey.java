package com.buff.constant;

/**
 * Redis Key常量
 *
 * @author Administrator
 */
public interface RedisKey {

    /**
     * 验证码前缀
     */
    String SMS_CODE_PREFIX = "sms:code:";

    /**
     * 用户信息缓存前缀
     */
    String USER_INFO_PREFIX = "user:info:";

    /**
     * 饰品模板缓存前缀
     */
    String ITEM_TEMPLATE_PREFIX = "item:template:";

    /**
     * 市场挂单缓存前缀
     */
    String MARKET_LISTING_PREFIX = "market:listing:";

    /**
     * 订单锁前缀
     */
    String ORDER_LOCK_PREFIX = "order:lock:";

    /**
     * 库存锁前缀
     */
    String INVENTORY_LOCK_PREFIX = "inventory:lock:";

    /**
     * Refresh Token 前缀
     */
    String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    /**
     * 获取验证码Key
     */
    static String getSmsCodeKey(String mobile) {
        return SMS_CODE_PREFIX + mobile;
    }

    /**
     * 获取用户信息Key
     */
    static String getUserInfoKey(Long userId) {
        return USER_INFO_PREFIX + userId;
    }

    /**
     * 获取饰品模板Key
     */
    static String getItemTemplateKey(Long templateId) {
        return ITEM_TEMPLATE_PREFIX + templateId;
    }

    /**
     * 获取市场挂单Key
     */
    static String getMarketListingKey(Long listingId) {
        return MARKET_LISTING_PREFIX + listingId;
    }

    /**
     * 获取订单锁Key
     */
    static String getOrderLockKey(Long listingId) {
        return ORDER_LOCK_PREFIX + listingId;
    }

    /**
     * 获取库存锁Key
     */
    static String getInventoryLockKey(Long inventoryId) {
        return INVENTORY_LOCK_PREFIX + inventoryId;
    }

    /**
     * 获取Refresh Token Key
     */
    static String getRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + refreshToken;
    }
}
