package com.allinfluencer.backend.mypage.infrastructure.mybatis.influencer;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface InfluencerResumeCommandMapper {
    int updateResume(
            @Param("userId") String userId,
            @Param("headline") String headline,
            @Param("bio") String bio,
            @Param("skills") String[] skills,
            @Param("portfolioUrls") String[] portfolioUrls,
            @Param("resumeJson") String resumeJson,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}

