package com.vending.common.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif"
    );

    public void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully", bucketName);
                
                minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(getPublicReadPolicy(bucketName))
                        .build()
                );
                log.info("Bucket '{}' set to public read", bucketName);
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket", e);
        }
    }

    public String uploadFile(MultipartFile file, String prefix) {
        createBucketIfNotExists();

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("只支持 JPG/PNG/GIF 格式的图片");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        String objectName = prefix + "/" + filename;

        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );

            log.info("File uploaded successfully: {}", objectName);
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("Error uploading file", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            log.info("File deleted successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Error deleting file", e);
        }
    }

    public String getPresignedUrl(String objectName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting presigned URL", e);
            throw new RuntimeException("获取访问URL失败");
        }
    }

    private String extractObjectName(String fileUrl) {
        String prefix = endpoint + "/" + bucketName + "/";
        return fileUrl.startsWith(prefix) ? fileUrl.substring(prefix.length()) : fileUrl;
    }

    private String getPublicReadPolicy(String bucket) {
        return "{\n" +
               "  \"Version\": \"2012-10-17\",\n" +
               "  \"Statement\": [\n" +
               "    {\n" +
               "      \"Effect\": \"Allow\",\n" +
               "      \"Principal\": \"*\",\n" +
               "      \"Action\": [\"s3:GetObject\"],\n" +
               "      \"Resource\": [\"arn:aws:s3:::" + bucket + "/*\"]\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
}
