package com.EVO.storage_service.service;

import com.EVO.storage_service.dto.FileDownloadDTO;
import com.EVO.storage_service.dto.FileResponse;
import com.EVO.storage_service.entity.File;
import com.EVO.storage_service.repository.FileRepository;
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
import java.util.Objects;

import static com.EVO.storage_service.utils.FileUtils.getReadableFileSize;
import static com.EVO.storage_service.utils.FileUtils.hashMD5;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    @Value("${file.storage.path}")
    private String storagePath;

    public FileResponse uploadFile(MultipartFile file, String ownerId, String accessType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = hashMD5(originalFileName) + fileExtension;

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
        String fileSize = getReadableFileSize(Files.size(filePath));

        // Save file metadata to DB
        File fileEntity = new File(null, originalFileName, fileExtension, fileName, ownerId, accessType, fileSize, MIMEType);
        fileRepository.save(fileEntity);

        return new FileResponse(originalFileName, fileEntity.getOwnerId(), fileEntity.getAccessType(), fileSize);
    }

    public Page<FileResponse> getFiles(String extensionType, String ownerId,
                                             String dateFilterMode, String filterDate, String accessType, Pageable pageable) {
        Instant startDate = null;
        Instant endDate = null;
        if(!(filterDate == null)){
            startDate = Instant.parse(filterDate+"T00:00:00Z");
            endDate = Instant.parse(filterDate+"T23:59:59Z");
        }

        Page<File> files = fileRepository.searchPublicFiles(accessType,extensionType, ownerId, dateFilterMode, startDate, endDate, pageable);

        return files.map(file -> FileResponse.builder()
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .ownerId(file.getOwnerId())
                .accessType(file.getAccessType())
                .build()
        );
    }

    @Transactional
    public void deletePublicFile(Long fileId, String ownerId) {
        File file = fileRepository.findPublicFile(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
        if(Objects.equals(file.getOwnerId(), ownerId)){
            Path filePath = Paths.get(storagePath + file.getStorageFileName());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Error deleting file: " + file.getFileName(), e);
            }
            fileRepository.delete(file);
        }else throw new RuntimeException("Wrong accessType or Wrong Owner");
    }
    @Transactional
    public void deletePrivateFile(Long fileId) {
        File file = fileRepository.findPrivateFile(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        Path filePath = Paths.get(storagePath + file.getStorageFileName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file: " + file.getFileName(), e);
        }
        fileRepository.delete(file);
    }
    public FileDownloadDTO downloadPublicFile(Long fileId) {
        File file = fileRepository.findPublicFile(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        try {
            Path filePath = Paths.get(storagePath + file.getStorageFileName());
            byte[] fileData = Files.readAllBytes(filePath);

            return new FileDownloadDTO(file.getFileName(), file.getExtensionType(), fileData);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getFileName(), e);
        }
    }
}
