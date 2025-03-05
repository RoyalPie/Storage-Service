package com.EVO.storage_service.controller;

import com.EVO.storage_service.dto.FileDownloadDTO;
import com.EVO.storage_service.dto.FileResponse;
import com.EVO.storage_service.service.FileService;
import com.EVO.storage_service.service.ImageService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicFileController {
    @Autowired
    private FileService fileService;

    @Autowired
    private ImageService imageService;

    @GetMapping(value = "/image/{id}",
                produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> viewImage(
            @PathVariable Long id,
            @RequestParam(required = false) Double ratio,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height) {
        try {
            byte[] imageBytes = imageService.getImage(id, width, height, ratio);

            return ResponseEntity.ok()
                    .body(imageBytes);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public List<FileResponse> uploadFile(@RequestParam("file") MultipartFile[] files) throws IOException {
        List<FileResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            FileResponse response = fileService.uploadFile(file, "truongtk1711@gmail.com", "public");
            responses.add(response);
        }

        return responses;
    }
    @GetMapping("/files")
    public ResponseEntity<Page<FileResponse>> getPublicFiles(
            @RequestParam(required = false) String extensionType,
            @RequestParam(required = false) String ownerId,
            @RequestParam(defaultValue = "created") String dateFilterMode,
            @RequestParam(required = false) Instant filterDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String sortValues) {


        String sortBy = sortValues.split(",")[0];
        String sortDir = sortValues.split(",")[1];
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FileResponse> files = fileService.getPublicFiles(extensionType, ownerId, dateFilterMode, filterDate, pageable);
        return ResponseEntity.ok(files);
    }
    @DeleteMapping("/delete-file/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id){
        fileService.deleteFile(id);
        return ResponseEntity.ok("Successful delete");
    }
    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId) {
        FileDownloadDTO fileDownload = fileService.downloadFile(fileId);

        ByteArrayResource resource = new ByteArrayResource(fileDownload.getFileData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDownload.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDownload.getFileName() + "\"")
                .body(resource);
    }

}
