package com.buff.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * BUFF平台爬虫工具类
 *
 * @author Administrator
 */
@Slf4j
public class BuffCrawlerUtil {

    /**
     * BUFF平台基础URL
     */
    private static final String BUFF_BASE_URL = "https://buff.163.com";

    /**
     * BUFF市场搜索URL
     */
    private static final String BUFF_MARKET_URL = BUFF_BASE_URL + "/market/csgo";

    /**
     * 请求超时时间（毫秒）
     */
    private static final int TIMEOUT = 15000;

    /**
     * User-Agent列表（随机使用，避免被识别为爬虫）
     */
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0"
    };

    /**
     * 从BUFF平台搜索并获取饰品价格
     *
     * @param itemName 饰品名称
     * @return 价格（人民币），如果未找到返回null
     */
    public static BigDecimal fetchPriceByName(String itemName) {
        if (StrUtil.isBlank(itemName)) {
            log.warn("饰品名称为空，无法查询价格");
            return null;
        }

        try {
            log.debug("开始从BUFF查询饰品价格: {}", itemName);

            // 1. 构造搜索URL
            String keyword = URLEncoder.encode(itemName, StandardCharsets.UTF_8);
            String searchUrl = BUFF_MARKET_URL + "#tab=selling&page_num=1&search=" + keyword;

            // 2. 发送HTTP请求获取页面
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent(getRandomUserAgent())
                    .timeout(TIMEOUT)
                    .referrer(BUFF_BASE_URL)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .ignoreHttpErrors(true)
                    .get();

            // 3. 解析价格
            BigDecimal price = parsePriceFromDocument(doc, itemName);

            if (price != null) {
                log.info("成功获取BUFF价格: {} = ¥{}", itemName, price);
            } else {
                log.warn("未找到饰品价格: {}", itemName);
            }

            return price;

        } catch (IOException e) {
            log.error("网络请求失败，无法获取BUFF价格: {}", itemName, e);
            return null;
        } catch (Exception e) {
            log.error("解析BUFF价格失败: {}", itemName, e);
            return null;
        }
    }

    /**
     * 从BUFF API获取饰品价格（推荐方式）
     * BUFF提供了API接口，比解析HTML更稳定
     *
     * @param itemName 饰品名称
     * @return 价格（人民币），如果未找到返回null
     */
    public static BigDecimal fetchPriceByApi(String itemName) {
        if (StrUtil.isBlank(itemName)) {
            log.warn("饰品名称为空，无法查询价格");
            return null;
        }

        try {
            log.debug("开始从BUFF API查询饰品价格: {}", itemName);

            // BUFF API接口（需要根据实际情况调整）
            String apiUrl = BUFF_BASE_URL + "/api/market/goods";
            String keyword = URLEncoder.encode(itemName, StandardCharsets.UTF_8);

            // 构造请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("game", "csgo");
            params.put("page_num", 1);
            params.put("search", keyword);
            params.put("sort_by", "price.asc");

            // 发送API请求
            Document doc = Jsoup.connect(apiUrl)
                    .data(convertMapToStringMap(params))
                    .userAgent(getRandomUserAgent())
                    .timeout(TIMEOUT)
                    .referrer(BUFF_MARKET_URL)
                    .header("Accept", "application/json")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();

            // 解析JSON响应
            String jsonResponse = doc.body().text();
            log.debug("BUFF API响应: {}", jsonResponse);

            // 注意：BUFF API可能需要登录或有其他限制
            // 如果API不可用，建议使用fetchPriceByName方法通过HTML解析获取价格
            // 这里暂时返回null，实际使用时需要根据API响应格式解析JSON

            return null;

        } catch (Exception e) {
            log.error("调用BUFF API失败: {}", itemName, e);
            return null;
        }
    }

    /**
     * 从HTML文档中解析价格
     */
    private static BigDecimal parsePriceFromDocument(Document doc, String itemName) {
        try {
            // 方法1: 查找商品列表中的第一个价格
            Elements priceElements = doc.select(".f_Strong");
            if (!priceElements.isEmpty()) {
                String priceText = priceElements.first().text();
                return parsePriceText(priceText);
            }

            // 方法2: 查找其他可能的价格元素
            priceElements = doc.select(".price");
            if (!priceElements.isEmpty()) {
                String priceText = priceElements.first().text();
                return parsePriceText(priceText);
            }

            // 方法3: 查找data-price属性
            Elements items = doc.select("[data-price]");
            if (!items.isEmpty()) {
                String priceAttr = items.first().attr("data-price");
                return parsePriceText(priceAttr);
            }

            log.warn("未找到价格元素: {}", itemName);
            return null;

        } catch (Exception e) {
            log.error("解析价格失败: {}", itemName, e);
            return null;
        }
    }

    /**
     * 解析价格文本
     * 支持格式: "¥123.45", "123.45", "123"
     */
    private static BigDecimal parsePriceText(String priceText) {
        if (StrUtil.isBlank(priceText)) {
            return null;
        }

        try {
            // 移除货币符号和空格
            String cleanPrice = priceText
                    .replace("¥", "")
                    .replace("￥", "")
                    .replace(",", "")
                    .trim();

            return new BigDecimal(cleanPrice);

        } catch (NumberFormatException e) {
            log.warn("价格格式错误: {}", priceText);
            return null;
        }
    }

    /**
     * 获取随机User-Agent
     */
    private static String getRandomUserAgent() {
        int index = (int) (Math.random() * USER_AGENTS.length);
        return USER_AGENTS[index];
    }

    /**
     * 转换Map<String, Object>为Map<String, String>
     */
    private static Map<String, String> convertMapToStringMap(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试爬取价格
        String[] testItems = {
                "AK-47 | 二西莫夫 (略有磨损)",
                "AWP | 龙狙 (崭新出厂)",
                "M4A4 | 龙王 (久经沙场)"
        };

        for (String itemName : testItems) {
            BigDecimal price = fetchPriceByName(itemName);
            System.out.println(itemName + " = ¥" + price);

            // 避免请求过快
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
