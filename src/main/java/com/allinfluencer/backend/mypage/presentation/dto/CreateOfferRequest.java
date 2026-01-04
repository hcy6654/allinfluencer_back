package com.allinfluencer.backend.mypage.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateOfferRequest(
        @NotBlank String receiverId,
        @NotNull @Min(0) Integer amount,
        String description,
        LocalDateTime deadline
) {
}


