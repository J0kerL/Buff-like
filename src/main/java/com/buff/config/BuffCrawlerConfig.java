package com.buff.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * BUFF爬虫配置
 *
 * @author Administrator
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "buff.crawler")
public class BuffCrawlerConfig {

    /**
     * 是否启用真实爬虫（false则使用模拟数据）
     */
    private Boolean enabled = false;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 15000;

    /**
     * 请求间隔最小延迟（毫秒）
     */
    private Integer minDelay = 2000;

    /**
     * 请求间隔最大延迟（毫秒）
     */
    private Integer maxDelay = 5000;

    /**
     * 最大重试次数
     */
    private Integer maxRetry = 3;

    /**
     * 代理服务器地址（可选）
     */
    private String proxyHost;

    /**
     * 代理服务器端口（可选）
     */
    private Integer proxyPort;
}
