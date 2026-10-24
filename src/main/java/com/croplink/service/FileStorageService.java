package com.croplink.service;

import com.croplink.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp",
            "image/bmp", "bmp");

    @Value("${app.storage.local.upload-dir:uploads/products}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("An image file is required", 400);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException("Image files must be 5 MB or smaller", 400);
        }
        String contentType = file.getContentType();
        String extension = contentType == null ? null : IMAGE_EXTENSIONS.get(contentType.toLowerCase(Locale.ROOT));
        if (extension == null) {
            throw new AppException("Only JPEG, PNG, GIF, WebP, and BMP images are supported", 400);
        }

        try {
            if (!hasExpectedImageSignature(file, contentType.toLowerCase(Locale.ROOT))) {
                throw new AppException("The uploaded file content does not match its image type", 400);
            }
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            String filename = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(filename);
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath);
            }
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new AppException("Failed to store image", 500);
        }
    }

    private boolean hasExpectedImageSignature(MultipartFile file, String contentType) throws IOException {
        byte[] header;
        try (var inputStream = file.getInputStream()) {
            header = inputStream.readNBytes(12);
        }
        return switch (contentType) {
            case "image/jpeg" -> header.length >= 3
                    && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
            case "image/png" -> startsWith(header, new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
            case "image/gif" -> startsWith(header, new byte[]{'G', 'I', 'F', '8'})
                    && header.length >= 6 && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
            case "image/bmp" -> startsWith(header, new byte[]{'B', 'M'});
            case "image/webp" -> startsWith(header, new byte[]{'R', 'I', 'F', 'F'})
                    && header.length >= 12
                    && Arrays.equals(Arrays.copyOfRange(header, 8, 12), new byte[]{'W', 'E', 'B', 'P'});
            default -> false;
        };
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return value.length >= prefix.length
                && Arrays.equals(Arrays.copyOf(value, prefix.length), prefix);
    }

    public void deleteFile(String fileUrl) {
        String prefix = "/uploads/products/";
        if (fileUrl == null || !fileUrl.startsWith(prefix)) {
            return;
        }
        String filename = fileUrl.substring(prefix.length());
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            throw new AppException("Invalid stored image path", 400);
        }
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(filename).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new AppException("Invalid stored image path", 400);
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new AppException("Failed to delete image", 500);
        }
    }
}
