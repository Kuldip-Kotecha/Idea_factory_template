package com.ideafactory.user.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ideafactory.user.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Service("cloudinaryStorage")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
@SuppressWarnings({"deprecation", "unused"})
public class CloudinaryStorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryStorageServiceImpl.class);

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImpl(
            @Value("${CLOUDINARY_CLOUD_NAME}") String cloudName,
            @Value("${CLOUDINARY_API_KEY}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));

        logger.info("CloudinaryStorageService initialized with cloud: {}", cloudName);
    }

    @Override
    public String saveFile(byte[] data, String subDir, String fileName) throws IOException {
        try {
            String publicId = "resumes/" + subDir + "/" + fileName;

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "raw"
            ));

            logger.debug("Resume uploaded to Cloudinary with public_id: {}", publicId);
            return publicId;
        } catch (Exception e) {
            logger.error("Failed to upload resume to Cloudinary: {}/{}", subDir, fileName, e);
            throw new IOException("Failed to upload resume to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] loadFile(String path) throws IOException {
        try {
            String publicId = extractPublicIdFromPath(path);
            String cloudinaryUrl = cloudinary.url()
                    .secure(true)
                    .resourceType("raw")
                    .generate(publicId);

            logger.debug("Fetching resume from Cloudinary URL: {}", cloudinaryUrl);
            URL url = new URL(cloudinaryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (Exception e) {
            logger.error("Failed to load resume from Cloudinary: {}", path, e);
            throw new IOException("Failed to load resume from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String path) throws IOException {
        try {
            String publicId = extractPublicIdFromPath(path);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
            logger.info("Deleted resume from Cloudinary: {}", publicId);
        } catch (Exception e) {
            logger.error("Failed to delete resume from Cloudinary: {}", path, e);
            throw new IOException("Failed to delete resume from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void archiveFolder(String folderPath) throws IOException {
        try {
            String prefix = "resumes/" + folderPath;
            @SuppressWarnings("rawtypes")
            Map resourcesResult = cloudinary.api().resources(ObjectUtils.asMap(
                    "prefix", prefix,
                    "max_results", 100,
                    "resource_type", "raw"
            ));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resources = (List<Map<String, Object>>) resourcesResult.get("resources");

            if (resources == null || resources.isEmpty()) {
                logger.info("No resources found to archive: {}", prefix);
                return;
            }

            for (Map<String, Object> resource : resources) {
                String publicId = (String) resource.get("public_id");
                cloudinary.uploader().addTag("archived", new String[]{publicId}, ObjectUtils.asMap(
                        "resource_type", "raw"
                ));
                logger.debug("Archived resume: {}", publicId);
            }

            logger.info("Archived {} resumes from folder: {}", resources.size(), folderPath);
        } catch (Exception e) {
            logger.error("Failed to archive folder: {}", folderPath, e);
            throw new IOException("Failed to archive folder: " + e.getMessage(), e);
        }
    }

    private String extractPublicIdFromPath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            int questionMarkIndex = path.indexOf('?');
            if (questionMarkIndex != -1) {
                path = path.substring(0, questionMarkIndex);
            }

            int uploadIndex = path.indexOf("/upload/");
            if (uploadIndex != -1) {
                path = path.substring(uploadIndex + 8);

                int slashIndex = path.indexOf('/');
                if (slashIndex != -1) {
                    String segment = path.substring(0, slashIndex);
                    if (segment.equals("raw") || segment.equals("video") || segment.equals("image")) {
                        path = path.substring(slashIndex + 1);
                    }
                }

                int versionIndex = path.indexOf('/');
                if (versionIndex != -1 && path.length() > versionIndex + 1 && path.charAt(versionIndex + 1) >= '0' && path.charAt(versionIndex + 1) <= '9') {
                    path = path.substring(versionIndex + 1);
                }
            }
        }

        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1) {
            path = path.substring(0, dotIndex);
        }

        path = path.replace('\\', '/');
        return path;
    }
}