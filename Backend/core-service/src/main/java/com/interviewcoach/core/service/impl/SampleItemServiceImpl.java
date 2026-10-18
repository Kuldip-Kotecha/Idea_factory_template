package com.interviewcoach.core.service.impl;

import com.interviewcoach.core.domain.SampleItem;
import com.interviewcoach.core.dto.SampleItemDTO;
import com.interviewcoach.core.exception.ResourceNotFoundException;
import com.interviewcoach.core.repository.SampleItemRepository;
import com.interviewcoach.core.service.SampleItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SampleItemServiceImpl implements SampleItemService {

    private final SampleItemRepository repository;

    public SampleItemServiceImpl(SampleItemRepository repository) {
        this.repository = repository;
    }

    private SampleItemDTO toDTO(SampleItem item) {
        return new SampleItemDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStatus(),
                item.getCreatedAt() != null ? item.getCreatedAt().toString() : ""
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SampleItemDTO> getItemsByUser(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SampleItemDTO getItemById(Long id, Long userId) {
        SampleItem item = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SampleItem not found with id: " + id));
        return toDTO(item);
    }

    @Override
    public SampleItemDTO createItem(Long userId, SampleItemDTO request) {
        SampleItem item = new SampleItem(
                userId,
                request.getTitle(),
                request.getDescription(),
                request.getStatus() != null ? request.getStatus() : "ACTIVE"
        );
        SampleItem saved = repository.save(item);
        return toDTO(saved);
    }

    @Override
    public SampleItemDTO updateItem(Long id, Long userId, SampleItemDTO request) {
        SampleItem item = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SampleItem not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            item.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            item.setStatus(request.getStatus().trim());
        }

        SampleItem updated = repository.save(item);
        return toDTO(updated);
    }

    @Override
    public void deleteItem(Long id, Long userId) {
        SampleItem item = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("SampleItem not found with id: " + id));
        repository.delete(item);
    }
}
