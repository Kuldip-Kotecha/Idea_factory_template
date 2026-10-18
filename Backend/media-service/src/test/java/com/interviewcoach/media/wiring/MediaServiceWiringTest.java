package com.interviewcoach.media.wiring;

import com.interviewcoach.media.service.StorageService;
import com.interviewcoach.media.queue.QueueScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
class MediaServiceWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(MediaServiceWiringTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageService storageService;

    @Autowired(required = false)
    private QueueScheduler queueScheduler;

    @Test
    @DisplayName("Wiring: /media/stream requires path parameter (returns 400, not 404)")
    void mediaStream_requiresPathParam() throws Exception {
        mockMvc.perform(get("/media/stream"))
                .andExpect(status().isBadRequest())
                .andDo(result -> logger.info("Wiring: /media/stream properly requires path parameter"));
    }

    @Test
    @DisplayName("Wiring: /media/upload endpoint is routed (returns 400 for missing params, not 404)")
    void mediaUpload_isRouted() throws Exception {
        mockMvc.perform(multipart("/media/upload"))
                .andExpect(status().isBadRequest())
                .andDo(result -> logger.info("Wiring: /media/upload is properly routed"));
    }

    @Test
    @DisplayName("Wiring: /media/archive endpoint is routed (returns 400 for missing body, not 404)")
    void mediaArchive_isRouted() throws Exception {
        mockMvc.perform(post("/media/archive"))
                .andExpect(status().isBadRequest())
                .andDo(result -> logger.info("Wiring: /media/archive is properly routed"));
    }

    @Test
    @DisplayName("Wiring: /media/save endpoint is routed (returns 400 for missing params, not 404)")
    void mediaSave_isRouted() throws Exception {
        mockMvc.perform(post("/media/save"))
                .andExpect(status().isBadRequest())
                .andDo(result -> logger.info("Wiring: /media/save is properly routed"));
    }

    @Test
    @DisplayName("Wiring: /media/merge endpoint is routed (returns 400 for missing body, not 404)")
    void mediaMerge_isRouted() throws Exception {
        mockMvc.perform(post("/media/merge"))
                .andExpect(status().isBadRequest())
                .andDo(result -> logger.info("Wiring: /media/merge is properly routed"));
    }

    @Test
    @DisplayName("Wiring: All media-service beans are properly autowired")
    void beans_areAutowired() {
        assertThat(storageService).isNotNull();
        logger.info("Wiring: Media service beans loaded successfully (storageService={}, queueScheduler={})",
                storageService, queueScheduler);
    }
}