package com.allinfluencer.backend.mypage.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateJobPostRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        String requirements,
        Integer budget,
        String[] categories,
        String[] platforms,
        LocalDateTime deadline
) {
}


