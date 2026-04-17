package com.bylw.controller;

import com.bylw.common.Result;
import com.bylw.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp", "image/gif"};

    @PostMapping("/image")
    public Result<?> uploadImage(HttpServletRequest request,
                                  @RequestParam("file") MultipartFile file) {
        verifyLogin(request);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WEBP、GIF 格式");
        }

        try {
            // Ensure upload directory exists
            Path uploadPath = Paths.get("src/main/resources/static/images/uploads/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String ext = getExtension(contentType);
            String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "." + ext;
            Path filepath = uploadPath.resolve(filename);

            file.transferTo(filepath.toFile());

            return Result.success("/images/uploads/" + filename);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    private boolean isAllowedType(String contentType) {
        for (String allowed : ALLOWED_TYPES) {
            if (allowed.equals(contentType)) return true;
        }
        return false;
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
    }

    private void verifyLogin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录，请先获取令牌");
        }
        String token = authHeader.replace("Bearer ", "");
        jwtUtil.getUserId(token);
    }
}
