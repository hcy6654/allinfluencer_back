package com.allinfluencer.backend.mypage.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface AdvertiserMypageReadPort {

    List<JobPostSummaryProjection> findMyJobPosts(
            String userId,
            String status,
            LocalDateTime cursor,
            int limitPlusOne
    );

    JobPostRefProjection findJobPostRef(String userId, String jobPostId);

    List<OfferSummaryProjection> findJobPostOffers(
            String userId,
            String jobPostId,
            String status,
            LocalDateTime cursor,
            int limitPlusOne
    );

    record JobPostSummaryProjection(
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
            int applicationCount,
            int offerCount
    ) {
    }

    record JobPostRefProjection(String id, String title) {
    }

    record OfferSummaryProjection(
            String id,
            String jobPostId,
            Integer amount,
            String description,
            LocalDateTime deadline,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String receiverId,
            String receiverDisplayName,
            String receiverAvatar
    ) {
    }
}


