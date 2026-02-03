package com.buff.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OSS配置属性
 *
 * @author Administrator
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * OSS端点
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;
}
