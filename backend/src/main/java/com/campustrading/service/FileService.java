package com.campustrading.service;

import com.campustrading.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    // 图片文件魔数（文件头字节签名）
    private static final byte[][] IMAGE_MAGIC_NUMBERS = {
        {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},             // JPEG
        {(byte) 0x89, 0x50, 0x4E, 0x47},                      // PNG
        {(byte) 0x47, 0x49, 0x46, 0x38},                      // GIF (GIF89a / GIF87a)
        {(byte) 0x52, 0x49, 0x46, 0x46},                      // WebP (RIFF....WEBP)
        {(byte) 0x42, 0x4D},                                   // BMP
    };

    @Value("${file.upload.path}")
    private String uploadPath;

    public String uploadImage(MultipartFile file) {
        // 1. 非空校验
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }

        // 2. 文件大小硬限制（后端兜底）
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 10MB");
        }

        // 3. 原始文件名提取扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // 4. 扩展名白名单校验
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的图片格式，仅允许: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 5. 魔数校验（防止 Content-Type 伪造）
        validateMagicNumber(file);

        // 6. 安全的文件名生成（UUID 防路径穿越）
        String filename = UUID.randomUUID().toString() + extension;

        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(filename).normalize();
            // 确保解析后的路径仍在 uploadDir 内（防路径穿越）
            if (!filePath.startsWith(uploadDir)) {
                throw new BusinessException("非法的文件路径");
            }

            file.transferTo(filePath.toFile());
            return "/uploads/" + filename;

        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 读取文件头部字节，与已知图片格式的魔数比对。
     * 防止攻击者修改 Content-Type 头绕过类型检查上传恶意文件。
     */
    private void validateMagicNumber(MultipartFile file) {
        int maxMagicBytes = 8; // 取最长的魔数长度即可
        byte[] header = new byte[maxMagicBytes];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header, 0, maxMagicBytes);
            if (bytesRead < 4) {
                throw new BusinessException("无法识别的文件格式");
            }

            for (byte[] magic : IMAGE_MAGIC_NUMBERS) {
                if (startsWith(header, magic)) {
                    return; // 匹配合法魔数，放行
                }
            }
            throw new BusinessException("文件内容不是有效的图片格式");
        } catch (IOException e) {
            throw new BusinessException("文件读取失败");
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }
}
