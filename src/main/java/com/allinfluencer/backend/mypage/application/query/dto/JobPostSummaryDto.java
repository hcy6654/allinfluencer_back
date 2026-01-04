package com.allinfluencer.backend.mypage.application.query.dto;

import java.time.LocalDateTime;

public record JobPostSummaryDto(
        String id,
        String title,
        String description,
        Integer budget,
        String[] categories,
        String[] platforms,
        LocalDateTime deadline,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Stats stats
) {
    public record Stats(
            int applicationCount,
            int offerCount
    ) {
    }
}


