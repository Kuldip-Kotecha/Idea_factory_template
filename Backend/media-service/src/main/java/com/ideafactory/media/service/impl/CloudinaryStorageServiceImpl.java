package com.ideafactory.media.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.ideafactory.media.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("cloudinaryStorage")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
@SuppressWarnings({"null", "unused", "deprecation"})
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
    public String saveFile(byte[] data, String path, String fileName) throws IOException {
        try {
            String publicId = path + "/" + fileName;

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "access_mode", "public"
            ));

            logger.debug("File uploaded to Cloudinary with public_id: {}", publicId);
            // Return the public_id (not the full URL) for consistent retrieval
            return publicId;
        } catch (Exception e) {
            logger.error("Failed to upload file to Cloudinary: {}/{}", path, fileName, e);
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

@Override
    public byte[] loadFile(String path) throws IOException {
        try {
            String publicId = extractPublicIdFromPath(path);
            if (!publicId.endsWith(".wav")) {
                publicId = publicId + ".wav";
            }

            // Generate a direct Cloudinary URL using the SDK
            String cloudinaryUrl = cloudinary.url()
                    .secure(true)
                    .resourceType("video") // Audio files use 'video' resource type in Cloudinary
                    .generate(publicId);

            logger.debug("Fetching file from Cloudinary URL: {}", cloudinaryUrl);
            URL url = new URL(cloudinaryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (Exception e) {
            logger.error("Failed to load file from Cloudinary: {}", path, e);
            throw new IOException("Failed to load file from Cloudinary: " + e.getMessage(), e);
        }
    }

@Override
    public org.springframework.core.io.Resource loadFileResource(String path) throws IOException {
        try {
            String publicId = extractPublicIdFromPath(path);
            if (!publicId.endsWith(".wav")) {
                publicId = publicId + ".wav";
            }

            // Generate a direct Cloudinary URL using the SDK
            String cloudinaryUrl = cloudinary.url()
                    .secure(true)
                    .resourceType("video") // Audio files use 'video' resource type in Cloudinary
                    .generate(publicId);

            logger.debug("Generated Cloudinary streaming URL for public_id: {}", publicId);
            return new org.springframework.core.io.UrlResource(cloudinaryUrl);
        } catch (Exception e) {
            logger.error("Failed to load file resource from Cloudinary: {}", path, e);
            throw new IOException("Failed to load file resource from Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void archiveFolder(String folderPath) throws IOException {
        try {
            // Cloudinary doesn't have folder move concept - we'll use tags to mark as archived
            // List all resources with the folder prefix
            @SuppressWarnings("rawtypes")
            Map resourcesResult = cloudinary.api().resources(ObjectUtils.asMap(
                    "prefix", folderPath,
                    "max_results", 100
            ));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resources = (List<Map<String, Object>>) resourcesResult.get("resources");

            if (resources == null || resources.isEmpty()) {
                logger.info("No resources found to archive in folder: {}", folderPath);
                return;
            }

            // Tag them as archived using Cloudinary uploader addTag
            for (Map<String, Object> resource : resources) {
                String publicId = (String) resource.get("public_id");
                String resourceType = (String) resource.getOrDefault("resource_type", "image");
                
                // Use uploader to add tag to existing resource - Cloudinary 2.x requires String[] for publicIds
                cloudinary.uploader().addTag("archived", new String[]{publicId}, ObjectUtils.asMap(
                        "resource_type", resourceType
                ));
                logger.debug("Archived resource: {}", publicId);
            }

            logger.info("Archived {} resources from folder: {}", resources.size(), folderPath);
        } catch (Exception e) {
            logger.error("Failed to archive folder: {}", folderPath, e);
            throw new IOException("Failed to archive folder: " + e.getMessage(), e);
        }
    }

    @Override
    public String mergeFiles(List<String> paths, String outputSubDir, String outputFileName) throws IOException {
        if (paths == null || paths.isEmpty()) {
            logger.warn("mergeFiles called with empty or null paths list");
            throw new IOException("No paths provided for merging");
        }

        logger.info("Starting merge of {} files to {}/{}", paths.size(), outputSubDir, outputFileName);
        logger.debug("Paths to merge: {}", paths);

        try {
            List<byte[]> fileContents = new ArrayList<>();
            for (int i = 0; i < paths.size(); i++) {
                String path = paths.get(i);
                String publicId = extractPublicIdFromPath(path);
                if (!publicId.endsWith(".wav")) {
                    publicId = publicId + ".wav";
                }
                String cloudinaryUrl = cloudinary.url()
                        .secure(true)
                        .resourceType("video")
                        .generate(publicId);

                logger.debug("Fetching file {}/{} from Cloudinary: {} (public_id: {})",
                        i + 1, paths.size(), cloudinaryUrl, publicId);

                URL url = new URL(cloudinaryUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode;
                try {
                    responseCode = connection.getResponseCode();
                } catch (IOException e) {
                    logger.error("Failed to connect to Cloudinary for file {}/{}: {} (public_id: {})",
                            i + 1, paths.size(), path, publicId, e);
                    throw new IOException("Failed to connect to Cloudinary: " + path, e);
                }

                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    logger.error("File not found in Cloudinary at {}/{}: {} (public_id: {}, URL: {})",
                            i + 1, paths.size(), path, publicId, cloudinaryUrl);
                    throw new IOException("File not found in Cloudinary: " + path + " (public_id: " + publicId + ")");
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    logger.error("Cloudinary returned error code {}/{} for file {}: {} (public_id: {})",
                            i + 1, paths.size(), responseCode, path, publicId);
                    throw new IOException("Cloudinary returned HTTP " + responseCode + " for file: " + path);
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] content = inputStream.readAllBytes();
                    logger.debug("Downloaded {} bytes from {}", content.length, publicId);
                    fileContents.add(content);
                }
            }

            logger.info("Successfully downloaded {} files, starting merge", fileContents.size());

            ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();
            for (byte[] content : fileContents) {
                mergedStream.write(content);
            }

            String publicId = outputSubDir + "/" + outputFileName;
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(mergedStream.toByteArray(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "access_mode", "public"
            ));

            logger.info("Merged file uploaded to Cloudinary with public_id: {}", publicId);
            return publicId;
        } catch (IOException e) {
            logger.error("Failed to merge {} files to {}/{}", paths.size(), outputSubDir, outputFileName, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error merging files", e);
            throw new IOException("Failed to merge files: " + e.getMessage(), e);
        }
    }

    /**
     * Extract public_id from a Cloudinary URL, full URL, or raw public_id path
     * Examples:
     * - Full URL: https://res.cloudinary.com/daajkbogx/video/upload/v1234567890/interviews/1/12.webm
     * - Public ID: interviews/1/12.webm
     * - Local path: /app/uploads/1/12.webm (won't work with Cloudinary)
     * Returns: interviews/1/12 (without version and extension)
     */
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

                int firstSlash = path.indexOf('/');
                if (firstSlash > 0) {
                    String firstSegment = path.substring(0, firstSlash);
                    if (firstSegment.length() > 1 && firstSegment.charAt(0) == 'v') {
                        boolean isVersion = true;
                        for (int i = 1; i < firstSegment.length(); i++) {
                            if (firstSegment.charAt(i) < '0' || firstSegment.charAt(i) > '9') {
                                isVersion = false;
                                break;
                            }
                        }
                        if (isVersion) {
                            path = path.substring(firstSlash + 1);
                        }
                    }
                }
            } else {
                logger.warn("Unexpected Cloudinary URL format: {}", path);
            }
        } else if (path.startsWith("/app/uploads/") || path.startsWith("/tmp/")) {
            path = path.replace("/app/uploads/", "").replace("/tmp/", "");
        }

        path = path.replace('\\', '/');

        return path;
    }
}
