package com.allinfluencer.backend.mypage.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationStatusRequest(
        @NotBlank String status
) {
}

