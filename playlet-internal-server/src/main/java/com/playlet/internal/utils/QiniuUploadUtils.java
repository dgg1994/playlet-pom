package com.playlet.internal.utils;

import com.playlet.internal.config.QiniuConfig;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 七牛云上传工具类
 * 使用方式：QiniuUploadUtils.upload(file)
 */
@Slf4j
@Component
public class QiniuUploadUtils {

    private static QiniuUploadUtils instance;

    @Autowired
    private QiniuConfig qiniuConfig;

    @Autowired
    private Auth qiniuAuth;

    @Autowired
    private UploadManager qiniuUploadManager;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取实例（用于静态方法调用）
     */
    private static QiniuUploadUtils getInstance() {
        if (instance == null) {
            throw new RuntimeException("QiniuUploadUtils 未初始化，请检查Spring配置");
        }
        return instance;
    }

    // ==================== 静态方法 ====================

    /**
     * 上传文件（MultipartFile）
     */
    public static String uploadFile(MultipartFile file, String dir) {
        return getInstance().fileUpload(file, dir);
    }

    /**
     * 上传文件到指定目录
     * @param file 短剧视频文件
     * @param dir 指定目录（短剧id+剧集id）
     * @return
     */
    public static String uploadVideo(MultipartFile file, String dir) {
        return getInstance().videoUpload(file, dir);
    }

    /**
     * 上传字节数组
     */
    public static String upload(byte[] data, String fileName) {
        return getInstance().doUpload(data, fileName, null);
    }

    /**
     * 上传字节数组到指定目录
     */
    public static String upload(byte[] data, String fileName, String dir) {
        return getInstance().doUpload(data, fileName, dir);
    }

    /**
     * 上传输入流
     */
    public static String upload(InputStream inputStream, String fileName) {
        return getInstance().doUpload(inputStream, fileName, null);
    }

    /**
     * 上传输入流到指定目录
     */
    public static String upload(InputStream inputStream, String fileName, String dir) {
        return getInstance().doUpload(inputStream, fileName, dir);
    }

    /**
     * 上传文件（指定完整路径）
     */
    public static String uploadWithFullPath(MultipartFile file, String fullPath) {
        return getInstance().doUploadWithFullPath(file, fullPath);
    }

    /**
     * 上传字节数组（指定完整路径）
     */
    public static String uploadWithFullPath(byte[] data, String fullPath) {
        return getInstance().doUploadWithFullPath(data, fullPath);
    }

    /**
     * 删除文件
     */
    public static boolean delete(String fileName) {
        return getInstance().doDelete(fileName);
    }

    /**
     * 删除文件（从URL中提取路径）
     */
    public static boolean deleteByUrl(String fileUrl) {
        return getInstance().doDeleteByUrl(fileUrl);
    }

    /**
     * 生成七牛云访问URL
     */
    public static String getFileUrl(String fileName) {
        return getInstance().qiniuConfig.getFileUrl(fileName);
    }

    // ==================== 实例方法 ====================

    /**
     * 执行上传（MultipartFile）视频
     */
    private String videoUpload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        try {
//            String fileName = generateFileName(file.getOriginalFilename());
            String fileName = file.getOriginalFilename();
            String fullPath = buildFullPath(fileName, dir);
            
            String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
            Response response = qiniuUploadManager.put(file.getBytes(), fullPath, upToken);

            if (response.isOK()) {
                String url = qiniuConfig.getFileUrl(fullPath);
                log.info("文件上传成功: {}", url);
                return url;
            } else {
                log.error("上传失败: {}", response.bodyString());
                throw new RuntimeException("上传失败: " + response.bodyString());
            }
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new RuntimeException("文件读取失败", e);
        }
    }
    
    private String fileUpload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        try {
            String fileName = generateFileName(file.getOriginalFilename());
//            String fileName = file.getOriginalFilename();
            String fullPath = buildFullPath(fileName, dir);
            
            String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
            Response response = qiniuUploadManager.put(file.getBytes(), fullPath, upToken);

            if (response.isOK()) {
                String url = qiniuConfig.getFileUrl(fullPath);
                log.info("文件上传成功: {}", url);
                return url;
            } else {
                log.error("上传失败: {}", response.bodyString());
                throw new RuntimeException("上传失败: " + response.bodyString());
            }
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new RuntimeException("文件读取失败", e);
        }
    }

    /**
     * 执行上传（字节数组）
     */
    private String doUpload(byte[] data, String fileName, String dir) {
        if (data == null || data.length == 0) {
            throw new RuntimeException("文件数据为空");
        }

        try {
            String fullFileName = generateFileName(fileName);
            String fullPath = buildFullPath(fullFileName, dir);
            
            String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
            Response response = qiniuUploadManager.put(data, fullPath, upToken);

            if (response.isOK()) {
                String url = qiniuConfig.getFileUrl(fullPath);
                log.info("文件上传成功: {}", url);
                return url;
            } else {
                log.error("上传失败: {}", response.bodyString());
                throw new RuntimeException("上传失败: " + response.bodyString());
            }
        } catch (Exception e) {
            log.error("上传失败", e);
            throw new RuntimeException("上传失败", e);
        }
    }

    /**
     * 执行上传（输入流）
     */
    private String doUpload(InputStream inputStream, String fileName, String dir) {
        if (inputStream == null) {
            throw new RuntimeException("输入流为空");
        }

        try {
            byte[] data = inputStream.readAllBytes();
            return doUpload(data, fileName, dir);
        } catch (IOException e) {
            log.error("读取输入流失败", e);
            throw new RuntimeException("读取输入流失败", e);
        }
    }

    /**
     * 执行上传（指定完整路径）
     */
    private String doUploadWithFullPath(MultipartFile file, String fullPath) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        try {
            String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
            Response response = qiniuUploadManager.put(file.getBytes(), fullPath, upToken);

            if (response.isOK()) {
                String url = qiniuConfig.getFileUrl(fullPath);
                log.info("文件上传成功: {}", url);
                return url;
            } else {
                log.error("上传失败: {}", response.bodyString());
                throw new RuntimeException("上传失败: " + response.bodyString());
            }
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new RuntimeException("文件读取失败", e);
        }
    }

    /**
     * 执行上传（字节数组指定完整路径）
     */
    private String doUploadWithFullPath(byte[] data, String fullPath) {
        if (data == null || data.length == 0) {
            throw new RuntimeException("文件数据为空");
        }

        try {
            String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucket());
            Response response = qiniuUploadManager.put(data, fullPath, upToken);

            if (response.isOK()) {
                String url = qiniuConfig.getFileUrl(fullPath);
                log.info("文件上传成功: {}", url);
                return url;
            } else {
                log.error("上传失败: {}", response.bodyString());
                throw new RuntimeException("上传失败: " + response.bodyString());
            }
        } catch (Exception e) {
            log.error("上传失败", e);
            throw new RuntimeException("上传失败", e);
        }
    }

    /**
     * 删除文件
     */
    private boolean doDelete(String fileName) {
        try {
            Configuration cfg = new Configuration(Region.autoRegion());
            BucketManager bucketManager = new BucketManager(qiniuAuth, cfg);
            
            Response response = bucketManager.delete(qiniuConfig.getBucket(), fileName);
            
            if (response.isOK()) {
                log.info("文件删除成功: {}", fileName);
                return true;
            } else {
                log.error("文件删除失败: {}", response.bodyString());
                return false;
            }
        } catch (Exception e) {
            log.error("删除异常", e);
            return false;
        }
    }

    /**
     * 从URL中提取路径并删除
     */
    private boolean doDeleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            // 提取路径部分
            String fileName = fileUrl;
            if (fileUrl.contains("://")) {
                int start = fileUrl.indexOf("/", 8);
                if (start > 0) {
                    fileName = fileUrl.substring(start);
                }
            }
            // 移除可能的查询参数
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            return doDelete(fileName);
        } catch (Exception e) {
            log.error("删除失败", e);
            return false;
        }
    }

    /**
     * 生成唯一文件名（自动按日期分目录）
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + extension;
    }

    /**
     * 构建完整路径
     */
    private String buildFullPath(String fileName, String dir) {
        if (dir == null || dir.isEmpty()) {
            // 默认按日期分目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            return datePath + "/" + fileName;
        }
        // 确保目录以/结尾
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }
        return dir + fileName;
    }
    
    public static String replaceFileExtension(String url, String newExtension) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        int lastDotIndex = url.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return url + "." + newExtension; // 如果没有扩展名，直接追加
        }
        return url.substring(0, lastDotIndex) + "." + newExtension;
    }
}