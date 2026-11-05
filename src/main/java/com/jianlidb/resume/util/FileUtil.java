package com.jianlidb.resume.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileUtil {

    public static String saveFile(MultipartFile file, String uploadPath) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString() +
                fileExtension;

        Path filePath = uploadDir.resolve(uniqueFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        log.info("文件保存成功: {}", filePath.toString());

        return filePath.toString();
    }

    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    public static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static boolean isImageFile(String filename) {
        String extension = getFileExtension(filename);
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    public static boolean isPdfFile(String filename) {
        String extension = getFileExtension(filename);
        return extension.equals("pdf");
    }
}