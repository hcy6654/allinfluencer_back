package com.allinfluencer.backend.mypage.infrastructure.mybatis.influencer;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InfluencerMypageReadMapper {
    OverviewRow selectOverview(@Param("userId") String userId);

    ResumeRow selectResume(@Param("userId") String userId);

    List<ChannelRow> selectChannels(@Param("userId") String userId);

    List<ApplicationRow> selectMyApplications(
            @Param("userId") String userId,
            @Param("status") String status,
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    List<ScrapRow> selectMyScraps(
            @Param("userId") String userId,
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    record OverviewRow(
            String headline,
            Integer totalFollowers,
            Double avgEngagement,
            Integer ratePerPost,
            Integer channelCount,
            Integer applicationTotal,
            Integer applicationPending,
            Integer applicationAccepted,
            Integer applicationRejected,
            Integer applicationWithdrawn,
            Integer scrapCount,
            Integer completedContracts,
            Double avgRating
    ) {}

    record ResumeRow(
            String id,
            String userId,
            String[] categories,
            Integer followers,
            Double avgEngagement,
            Integer ratePerPost,
            String location,
            String[] languages,
            String headline,
            String bio,
            String[] skills,
            String[] portfolioUrls,
            String resumeJson
    ) {}

    record ChannelRow(
            String id,
            String platform,
            String channelUrl,
            String channelHandle,
            Integer followers,
            Integer avgViews,
            Integer avgLikes
    ) {}

    record ApplicationRow(
            String id,
            String status,
            LocalDateTime appliedAt,
            String jobPostId,
            String jobTitle,
            String advertiserId,
            String advertiserCompanyName
    ) {}

    record ScrapRow(
            String jobPostId,
            String jobTitle,
            LocalDateTime scrappedAt
    ) {}
}

