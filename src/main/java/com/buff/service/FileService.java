package com.buff.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 *
 * @author Administrator
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问URL
     */
    String upload(MultipartFile file);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean delete(String fileUrl);
}
