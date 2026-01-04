package com.allinfluencer.backend.mypage.application.command.model;

import java.time.LocalDateTime;

public record CreateOfferCommand(
        String receiverId,
        Integer amount,
        String description,
        LocalDateTime deadline
) {
}


