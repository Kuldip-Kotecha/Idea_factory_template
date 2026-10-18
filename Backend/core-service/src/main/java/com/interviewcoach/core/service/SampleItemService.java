package com.interviewcoach.core.service;

import com.interviewcoach.core.dto.SampleItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SampleItemService {
    Page<SampleItemDTO> getItemsByUser(Long userId, Pageable pageable);
    SampleItemDTO getItemById(Long id, Long userId);
    SampleItemDTO createItem(Long userId, SampleItemDTO request);
    SampleItemDTO updateItem(Long id, Long userId, SampleItemDTO request);
    void deleteItem(Long id, Long userId);
}
