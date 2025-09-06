package com.xyz.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageStorageService {

    // 可配置：保存目录与 URL 前缀
    @Value("${app.upload.dir:C:/Users/X/Pictures/BSS}")
    private String uploadDir;

    @Value("${app.static.prefix:/images}")
    private String staticPrefix;

    public String saveBase64Image(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("empty image");
        }

        String data = base64.trim();
        String mime = "image/png"; // 默认
        String payload = data;

        // 兼容 data URL
        if (data.startsWith("data:")) {
            int comma = data.indexOf(',');
            if (comma <= 0) throw new IllegalArgumentException("invalid data url");
            String header = data.substring(5, comma); // e.g. "image/png;base64"
            payload = data.substring(comma + 1);
            int semi = header.indexOf(';');
            mime = (semi > 0 ? header.substring(0, semi) : header);
        }

        byte[] bytes = java.util.Base64.getDecoder().decode(payload);

        // 根据 MIME 选扩展名
        String ext = switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".png"; // 兜底
        };

        // 生成文件名
        String filename = java.util.UUID.randomUUID().toString().replace("-", "") + ext;

        // 写文件
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("cannot create upload dir: " + uploadDir);
        }
        java.nio.file.Path path = java.nio.file.Paths.get(uploadDir, filename);
        try {
            java.nio.file.Files.write(path, bytes);
        } catch (java.io.IOException e) {
            throw new RuntimeException("write image failed", e);
        }

        // 返回可被前端访问的 URL（与静态映射一致）
        return staticPrefix + "/" + filename;
    }
}
