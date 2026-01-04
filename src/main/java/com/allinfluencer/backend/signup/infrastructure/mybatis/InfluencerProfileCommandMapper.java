package com.allinfluencer.backend.signup.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface InfluencerProfileCommandMapper {
    int insertInfluencerProfile(
            @Param("id") String id,
            @Param("userId") String userId,
            @Param("categories") String[] categories,
            @Param("headline") String headline,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    int insertChannel(
            @Param("id") String id,
            @Param("influencerProfileId") String influencerProfileId,
            @Param("platform") String platform,
            @Param("channelUrl") String channelUrl,
            @Param("channelHandle") String channelHandle,
            @Param("followers") Integer followers,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}

