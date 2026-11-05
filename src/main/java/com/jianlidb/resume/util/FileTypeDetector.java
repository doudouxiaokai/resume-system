package com.jianlidb.resume.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileTypeDetector {

    public enum FileType {
        IMAGE, TEXT_PDF, IMAGE_PDF, UNKNOWN
    }

    public static FileType detectFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return FileType.UNKNOWN;
        }

        String lowerFilename = filename.toLowerCase();

        // 检查是否为图片
        if (lowerFilename.endsWith(".jpg") ||
                lowerFilename.endsWith(".jpeg") ||
                lowerFilename.endsWith(".png")) {
            return FileType.IMAGE;
        }

        // 检查是否为PDF
        if (lowerFilename.endsWith(".pdf")) {
            // 简化处理，直接返回TEXT_PDF
            // 在实际项目中，这里需要更复杂的检测逻辑
            return FileType.TEXT_PDF;
        }

        return FileType.UNKNOWN;
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

    public static boolean isSupportedFileType(String filename) {
        String extension = getFileExtension(filename);
        return extension.equals("pdf") ||
                extension.equals("jpg") ||
                extension.equals("jpeg") ||
                extension.equals("png");
    }
}