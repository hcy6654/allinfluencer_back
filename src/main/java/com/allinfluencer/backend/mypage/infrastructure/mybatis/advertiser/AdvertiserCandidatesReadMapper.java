package com.allinfluencer.backend.mypage.infrastructure.mybatis.advertiser;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdvertiserCandidatesReadMapper {

    JobPostRefRow selectJobPostRef(@Param("userId") String userId, @Param("jobPostId") String jobPostId);

    List<ApplicantRow> selectApplicants(
            @Param("userId") String userId,
            @Param("jobPostId") String jobPostId,
            @Param("status") String status,
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    List<OfferRow> selectOffers(
            @Param("userId") String userId,
            @Param("jobPostId") String jobPostId,
            @Param("status") String status,
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    record JobPostRefRow(String id, String title) {}

    record ApplicantRow(
            String id,
            String status,
            LocalDateTime appliedAt,
            String influencerId,
            String influencerDisplayName,
            String influencerAvatar,
            String headline,
            Integer followers,
            Double avgEngagement,
            String[] categories
    ) {}

    record OfferRow(
            String id,
            String status,
            LocalDateTime offeredAt,
            String influencerId,
            String influencerDisplayName,
            String influencerAvatar,
            String headline,
            Integer followers,
            Double avgEngagement,
            String[] categories
    ) {}
}

