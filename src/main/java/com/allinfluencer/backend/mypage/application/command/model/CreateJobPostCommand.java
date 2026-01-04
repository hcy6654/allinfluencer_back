package com.allinfluencer.backend.mypage.application.command.model;

import java.time.LocalDateTime;

public record CreateJobPostCommand(
        String title,
        String description,
        String requirements,
        Integer budget,
        String[] categories,
        String[] platforms,
        LocalDateTime deadline
) {
}


