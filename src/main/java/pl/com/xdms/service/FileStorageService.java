package pl.com.xdms.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.configuration.FileStorageProperties;
import pl.com.xdms.controller.ReferenceController;
import pl.com.xdms.exception.FileStorageException;
import pl.com.xdms.exception.MyFileNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created on 26.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceController.class);

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Path storeFile(MultipartFile file) {
        //Changing fileName to unique name with saving extension
        //RandomStringUtils is used to get random String
        String fileName = String.format("%s.%s",
                RandomStringUtils.randomAlphanumeric(20),
                file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1));
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file " + fileName);
            }
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            LOG.info("Saving File name: {}; Path: {}", fileName, targetLocation);

            return targetLocation;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            LOG.info("Requested file from Path: {}",filePath.toString());
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
