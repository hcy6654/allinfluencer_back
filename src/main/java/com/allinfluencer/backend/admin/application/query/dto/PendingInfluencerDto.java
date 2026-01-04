package com.allinfluencer.backend.admin.application.query.dto;

import java.time.LocalDateTime;

public record PendingInfluencerDto(
        String userId,
        String email,
        String displayName,
        String[] categories,
        LocalDateTime submittedAt
) {
}

