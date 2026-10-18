package com.ideafactory.core.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideafactory.core.controller.SampleItemController;
import com.ideafactory.core.dto.SampleItemDTO;
import com.ideafactory.core.service.SampleItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ActiveProfiles("smoke")
class SampleItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SampleItemService sampleItemService;

    @MockBean
    private com.ideafactory.core.security.JwtUtil jwtUtil;

    @Test
    void getItems_returnsPaginatedList() throws Exception {
        SampleItemDTO item = new SampleItemDTO(1L, "Test Item", "Description", "ACTIVE", "2026-10-18");
        when(sampleItemService.getItemsByUser(eq(1L), any())).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/core/items?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Item"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void createItem_returnsCreatedItem() throws Exception {
        SampleItemDTO request = new SampleItemDTO(null, "New Item", "Description", "ACTIVE", null);
        SampleItemDTO created = new SampleItemDTO(1L, "New Item", "Description", "ACTIVE", "2026-10-18");

        when(sampleItemService.createItem(eq(1L), any())).thenReturn(created);

        mockMvc.perform(post("/api/core/items?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Item"));
    }
}
