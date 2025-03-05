package com.EVO.storage_service.service;

import com.EVO.storage_service.dto.FileDownloadDTO;
import com.EVO.storage_service.dto.FileResponse;
import com.EVO.storage_service.entity.File;
import com.EVO.storage_service.repository.FileRepository;
import com.EVO.storage_service.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    @Value("${file.storage.public-path}")
    private String storagePath;
    @Value("${file.storage.get-path}")
    private String getFilePath;

    public FileResponse uploadFile(MultipartFile file, String ownerId, String accessType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID() + fileExtension;

        Path directory = Paths.get(storagePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        Path filePath = directory.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Detect file type
        Tika tika = new Tika();
        String MIMEType = tika.detect(filePath.toFile());

        // Get file size
        String fileSize = FileUtils.getReadableFileSize(Files.size(filePath));

        String url = accessType + "/" + fileName;

        // Save file metadata to DB
        File fileEntity = new File(null, originalFileName, fileExtension, url, ownerId, accessType, fileSize, MIMEType);
        fileRepository.save(fileEntity);

        return new FileResponse(originalFileName, fileEntity.getOwnerId(), fileEntity.getAccessType(), fileSize);
    }

    public Page<FileResponse> getPublicFiles(String extensionType, String ownerId,
                                             String dateFilterMode, Instant filterDate, Pageable pageable) {

        Page<File> files = fileRepository.searchPublicFiles(extensionType, ownerId, dateFilterMode, filterDate, pageable);

        return files.map(file -> FileResponse.builder()
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .ownerId(file.getOwnerId())
                .accessType(file.getAccessType())
                .build()
        );
    }

    @Transactional
    public void deleteFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
        Path filePath = Paths.get(getFilePath + file.getUrl());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file: " + file.getFileName(), e);
        }

        fileRepository.delete(file);
    }

    public FileDownloadDTO downloadFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        try {
            Path filePath = Paths.get(getFilePath + file.getUrl());
            byte[] fileData = Files.readAllBytes(filePath);

            return new FileDownloadDTO(file.getFileName(), file.getExtensionType(), fileData);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getFileName(), e);
        }
    }
}
