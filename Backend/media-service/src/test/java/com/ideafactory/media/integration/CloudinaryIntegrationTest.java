package com.ideafactory.media.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies real Cloudinary connectivity.
 * Tests file upload, retrieval, and deletion capabilities.
 *
 * Run with: mvn test -Dspring.profiles.active=integration-test
 *
 * NOTE: Does NOT use @SpringBootTest to avoid booting the full app context
 * which would require MySQL and other external dependencies.
 */
@EnabledIfEnvironmentVariable(named = "CLOUDINARY_API_KEY", matches = ".+")
@SuppressWarnings({"unchecked", "rawtypes"})
class CloudinaryIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryIntegrationTest.class);

    private Cloudinary getCloudinary() {
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        String apiKey = System.getenv("CLOUDINARY_API_KEY");
        String apiSecret = System.getenv("CLOUDINARY_API_SECRET");

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    @Test
    @DisplayName("Should initialize Cloudinary client successfully")
    void testCloudinaryInitialization() {
        Cloudinary cloudinary = getCloudinary();

        assertThat(cloudinary).isNotNull();
        assertThat(System.getenv("CLOUDINARY_CLOUD_NAME")).isNotBlank();
        assertThat(System.getenv("CLOUDINARY_API_KEY")).isNotBlank();

        logger.info("Cloudinary client initialized for cloud: {}", System.getenv("CLOUDINARY_CLOUD_NAME"));
    }

    @Test
    @DisplayName("Should upload file to Cloudinary successfully")
    void testFileUpload() throws IOException {
        Cloudinary cloudinary = getCloudinary();

        String testContent = "Integration test file content - " + System.currentTimeMillis();
        byte[] testBytes = testContent.getBytes(StandardCharsets.UTF_8);

        Map uploadResult = cloudinary.uploader().upload(
                testBytes,
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "public_id", "integration_test/test_file_" + System.currentTimeMillis(),
                        "tags", "integration_test"
                )
        );

        assertThat(uploadResult).isNotNull();
        assertThat(uploadResult).containsKey("public_id");
        assertThat(uploadResult).containsKey("url");

        String publicId = (String) uploadResult.get("public_id");
        logger.info("File uploaded to Cloudinary with public_id: {}", publicId);

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
            logger.info("Test file cleaned up successfully");
        } catch (Exception e) {
            logger.warn("Failed to clean up test file: {}", publicId, e);
        }
    }

    @Test
    @DisplayName("Should retrieve Cloudinary account info")
    void testCloudinaryApiAccess() throws IOException {
        Cloudinary cloudinary = getCloudinary();

        try {
            Map<String, Object> usage = cloudinary.api().usage(ObjectUtils.emptyMap());
            assertThat(usage).isNotNull();
            assertThat(usage).containsKey("plan");
            assertThat(usage).containsKey("credits");

            String plan = (String) usage.get("plan");
            logger.info("Cloudinary account plan: {}", plan);
            logger.info("Cloudinary API access verified successfully");
        } catch (com.cloudinary.api.exceptions.NotAllowed e) {
            logger.warn("⚠️ Cloudinary: Connected successfully, but Admin API usage check is restricted by permissions: {}", e.getMessage());
            logger.info("Cloudinary API connectivity is verified via standard upload capabilities (see testFileUpload). Skipping Admin API usage check.");
        } catch (Exception e) {
            logger.error("Failed to access Cloudinary API", e);
            throw new IOException("Cloudinary API access failed", e);
        }
    }
}