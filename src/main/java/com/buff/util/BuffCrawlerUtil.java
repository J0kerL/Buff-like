package com.buff.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Steam Community Market 价格查询工具类
 * 使用 Steam 官方公开的 Market Price Overview API，无需登录/付费，
 *
 * @author Administrator
 */
@Slf4j
public class BuffCrawlerUtil {

    /**
     * Steam Community Market Price Overview API
     */
    private static final String STEAM_PRICE_API =
            "https://steamcommunity.com/market/priceoverview/";

    /**
     * CS2 (原 CSGO) 的 Steam AppID
     */
    private static final int CSGO_APP_ID = 730;

    /**
     * 货币代码：23 = CNY 人民币
     */
    private static final int CURRENCY_CNY = 23;

    /**
     * 请求超时时间（毫秒）
     */
    private static final int TIMEOUT_MS = 10_000;

    /**
     * 根据物品英文市场哈希名（market_hash_name）查询 Steam 市场最低价。
     *
     * @param marketHashName 英文物品名，例如 "AK-47 | Asiimov (Field-Tested)"
     * @return 最低成交价（CNY），查询失败返回 null
     */
    public static BigDecimal fetchPriceByName(String marketHashName) {
        if (StrUtil.isBlank(marketHashName)) {
            log.warn("market_hash_name 为空，跳过价格查询");
            return null;
        }

        try {
            String encodedName = URLEncoder.encode(marketHashName, StandardCharsets.UTF_8);
            String url = STEAM_PRICE_API
                    + "?appid=" + CSGO_APP_ID
                    + "&currency=" + CURRENCY_CNY
                    + "&market_hash_name=" + encodedName;

            log.debug("查询 Steam 市场价格: {}", marketHashName);

            HttpResponse response = HttpRequest.get(url)
                    .header("Accept", "application/json")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .timeout(TIMEOUT_MS)
                    .execute();

            if (!response.isOk()) {
                log.warn("Steam API 返回非 200 状态: status={}, item={}", response.getStatus(), marketHashName);
                return null;
            }

            String body = response.body();
            log.debug("Steam API 响应: {}", body);

            return parseSteamPriceResponse(body, marketHashName);

        } catch (Exception e) {
            log.error("查询 Steam 市场价格失败: item={}", marketHashName, e);
            return null;
        }
    }

    /**
     * 解析 Steam Market API 的 JSON 响应，提取最低价格。
     * 响应格式示例: {"success":true,"lowest_price":"¥ 1,234.00","volume":"12"}
     */
    private static BigDecimal parseSteamPriceResponse(String json, String itemName) {
        try {
            JSONObject obj = JSONUtil.parseObj(json);

            if (!obj.getBool("success", false)) {
                log.warn("Steam API 返回 success=false: item={}", itemName);
                return null;
            }

            // 优先使用 lowest_price，其次 median_price
            String priceStr = obj.getStr("lowest_price");
            if (StrUtil.isBlank(priceStr)) {
                priceStr = obj.getStr("median_price");
            }

            if (StrUtil.isBlank(priceStr)) {
                log.warn("Steam API 响应中无价格字段: item={}", itemName);
                return null;
            }

            return parseChinesePriceText(priceStr, itemName);

        } catch (Exception e) {
            log.error("解析 Steam 价格响应失败: item={}, json={}", itemName, json, e);
            return null;
        }
    }

    /**
     * 解析含人民币符号的价格文本。
     * 支持格式: "¥ 1,234.00"、"¥1234.00"、"CNY 1,234.00" 等。
     */
    private static BigDecimal parseChinesePriceText(String priceText, String itemName) {
        try {
            // 移除货币符号、非断行空格(\u00a0)、普通空格、逗号
            String clean = priceText
                    .replace("¥", "")
                    .replace("￥", "")
                    .replace("CNY", "")
                    .replace("\u00a0", "")
                    .replace(",", "")
                    .trim();

            BigDecimal price = new BigDecimal(clean);
            log.info("Steam 价格查询成功: {} = ¥{}", itemName, price);
            return price;

        } catch (NumberFormatException e) {
            log.warn("价格文本解析失败: item={}, text={}", itemName, priceText);
            return null;
        }
    }
}
