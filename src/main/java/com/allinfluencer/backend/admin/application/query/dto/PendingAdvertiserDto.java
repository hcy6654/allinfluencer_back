package com.allinfluencer.backend.admin.application.query.dto;

import java.time.LocalDateTime;

public record PendingAdvertiserDto(
        String userId,
        String email,
        String companyName,
        String industry,
        String businessRegistrationNumber,
        LocalDateTime submittedAt
) {
}

