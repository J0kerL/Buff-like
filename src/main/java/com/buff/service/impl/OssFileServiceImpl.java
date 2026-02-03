package com.buff.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.buff.common.ResultCode;
import com.buff.config.OssProperties;
import com.buff.exception.BusinessException;
import com.buff.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS文件服务实现
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileServiceImpl implements FileService {

    private final OssProperties ossProperties;
    private OSS ossClient;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        log.info("OSS客户端初始化成功");
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSS客户端关闭");
        }
    }

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件名不能为空");
            }

            // 获取文件扩展名
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            // 生成对象名：日期/UUID.扩展名
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            String objectName = "buff/" + date + "/" + filename;

            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    objectName,
                    file.getInputStream()
            );
            ossClient.putObject(putObjectRequest);

            // 返回访问URL
            String fileUrl = "https://" + ossProperties.getBucketName() + "." + 
                           ossProperties.getEndpoint() + "/" + objectName;
            log.info("文件上传OSS成功: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传OSS失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        try {
            // 从URL中提取对象名
            String bucketDomain = "https://" + ossProperties.getBucketName() + "." + 
                                ossProperties.getEndpoint() + "/";
            if (!fileUrl.startsWith(bucketDomain)) {
                return false;
            }

            String objectName = fileUrl.substring(bucketDomain.length());

            // 删除文件
            ossClient.deleteObject(ossProperties.getBucketName(), objectName);
            log.info("文件从OSS删除成功: {}", objectName);
            return true;

        } catch (Exception e) {
            log.error("文件从OSS删除失败", e);
            return false;
        }
    }
}
