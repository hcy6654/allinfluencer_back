package com.allinfluencer.backend.mypage.application.port.out;

import java.time.LocalDateTime;

public interface AdvertiserMypageCommandPort {
    void insertJobPost(
            String id,
            String userId,
            String companyId,
            String title,
            String description,
            String requirements,
            Integer budget,
            String[] categories,
            String[] platforms,
            LocalDateTime deadline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    );

    void insertOffer(
            String id,
            String jobPostId,
            String senderId,
            String receiverId,
            Integer amount,
            String description,
            LocalDateTime deadline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    );
}


