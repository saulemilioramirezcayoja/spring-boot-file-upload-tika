package com.spring_boot_file_upload_tika.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class UploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        String encodedFileName = file.getOriginalFilename();

        if (encodedFileName == null || !encodedFileName.contains("-") || !encodedFileName.endsWith(".txt")) {
            throw new IOException("Invalid file name format");
        }

        byte[] encodedData = file.getBytes();
        byte[] decodedData = Base64.getDecoder().decode(new String(encodedData));

        Tika tika = new Tika();
        String mimeType = tika.detect(decodedData);
        String extension = getExtensionFromMimeType(mimeType);

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName = encodedFileName.substring(0, encodedFileName.lastIndexOf(".txt")) + extension;
        Path filePath = uploadPath.resolve(originalFileName);

        if (Files.exists(filePath)) {
            return "Duplicate file. File with the same name already exists: " + filePath;
        }

        Files.write(filePath, decodedData);

        return "File stored successfully: " + filePath;
    }

    private String getExtensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            case "text/plain" -> ".txt";
            default -> "";
        };
    }
}
