package com.vending.module.product.controller;

import com.vending.common.result.Result;
import com.vending.common.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final MinioService minioService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", required = false, defaultValue = "products") String prefix
    ) {
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }

        String fileUrl = minioService.uploadFile(file, prefix);
        return Result.success(fileUrl);
    }
}
