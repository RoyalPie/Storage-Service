package com.EVO.storage_service.service;

import com.EVO.storage_service.entity.File;
import com.EVO.storage_service.repository.FileRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class ImageService {
    @Value("${file.storage.path}")
    private String storagePath;

    @Autowired
    private FileRepository fileRepository;

    public byte[] getImage(String name, Integer width, Integer height, Double ratio, String accessType) throws IOException {
        File imageOpt = fileRepository.findImage(name, accessType).orElseThrow(()->new FileNotFoundException("Image not found"));

        java.io.File imageFile = new java.io.File(storagePath + imageOpt.getStorageFileName());

        if (!imageFile.exists()) {
            throw new FileNotFoundException("Image not found in storage" );
        }

        BufferedImage originalImage = ImageIO.read(imageFile);
        BufferedImage resizedImage = resizeImage(originalImage, width, height, ratio);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);
        return outputStream.toByteArray();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, Integer width, Integer height, Double ratio) {
        int newWidth = (width != null) ? width : originalImage.getWidth();
        int newHeight = (height != null) ? height : originalImage.getHeight();

        if (ratio != null) {
            newWidth = (int) (originalImage.getWidth() * ratio);
            newHeight = (int) (originalImage.getHeight() * ratio);
        }

        return Scalr.resize(originalImage, Scalr.Method.BALANCED, newWidth, newHeight);
    }
}
