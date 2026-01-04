package com.allinfluencer.backend.mypage.application.query.dto;

import java.time.LocalDateTime;

public record OfferSummaryDto(
        String id,
        String jobPostId,
        Integer amount,
        String description,
        LocalDateTime deadline,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Receiver receiver
) {
    public record Receiver(
            String id,
            String displayName,
            String avatar
    ) {
    }
}


