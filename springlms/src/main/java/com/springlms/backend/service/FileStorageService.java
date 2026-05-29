package com.springlms.backend.service;

import com.springlms.backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.storage.path:./uploads}")
    private String storagePath;

    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String safeName = originalName == null ? "upload" : originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = UUID.randomUUID() + "_" + safeName;

        try {
            Path target = Paths.get(storagePath).resolve(key).normalize();
            Files.createDirectories(target.getParent());
            Files.write(target, file.getBytes());
        } catch (IOException ex) {
            throw new BadRequestException("Unable to save uploaded file.");
        }

        return key;
    }

    public byte[] load(String key) {
        if (key == null || key.isBlank()) {
            throw new BadRequestException("Attachment does not have a downloadable file.");
        }

        Path path = Paths.get(storagePath).resolve(key).normalize();

        if (!path.startsWith(Paths.get(storagePath).normalize())) {
            throw new BadRequestException("Invalid file path.");
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new BadRequestException("File could not be read from storage.");
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(storagePath).resolve(key).normalize());
        } catch (IOException ex) {
            // log but don't rethrow — orphaned files are recoverable, broken transactions are not
        }
    }
}
