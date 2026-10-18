package com.ideafactory.core.repository;

import com.ideafactory.core.domain.SampleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SampleItemRepository extends JpaRepository<SampleItem, Long> {
    Page<SampleItem> findByUserId(Long userId, Pageable pageable);
    Optional<SampleItem> findByIdAndUserId(Long id, Long userId);
}
