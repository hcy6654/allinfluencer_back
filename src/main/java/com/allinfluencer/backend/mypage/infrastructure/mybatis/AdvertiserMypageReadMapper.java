package com.allinfluencer.backend.mypage.infrastructure.mybatis;

import com.allinfluencer.backend.mypage.application.query.dto.JobPostSummaryDto;
import com.allinfluencer.backend.mypage.application.query.dto.OfferSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdvertiserMypageReadMapper {

    List<JobPostSummaryRow> selectMyJobPosts(
            @Param("userId") String userId,
            @Param("status") String status,
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    JobPostRefRow selectJobPostRef(
            @Param("userId") String userId,
            @Param("jobPostId") String jobPostId
    );

    List<OfferSummaryRow> selectJobPostOffers(
            @Param("userId") String userId,
            @Param("jobPostId") String jobPostId,
            @Param("status") String status,
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    record JobPostSummaryRow(
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
        public JobPostSummaryDto toDto() {
            return new JobPostSummaryDto(
                    id,
                    title,
                    description,
                    budget,
                    categories,
                    platforms,
                    deadline,
                    status,
                    createdAt,
                    updatedAt,
                    new JobPostSummaryDto.Stats(applicationCount, offerCount)
            );
        }
    }

    record JobPostRefRow(
            String id,
            String title
    ) {
    }

    record OfferSummaryRow(
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
        public OfferSummaryDto toDto() {
            return new OfferSummaryDto(
                    id,
                    jobPostId,
                    amount,
                    description,
                    deadline,
                    status,
                    createdAt,
                    updatedAt,
                    new OfferSummaryDto.Receiver(receiverId, receiverDisplayName, receiverAvatar)
            );
        }
    }
}


