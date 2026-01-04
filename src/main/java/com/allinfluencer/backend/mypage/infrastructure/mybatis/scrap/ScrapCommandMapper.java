package com.allinfluencer.backend.mypage.infrastructure.mybatis.scrap;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface ScrapCommandMapper {
    Integer existsScrap(@Param("userId") String userId, @Param("jobPostId") String jobPostId);

    int insertScrap(
            @Param("id") String id,
            @Param("userId") String userId,
            @Param("jobPostId") String jobPostId,
            @Param("createdAt") LocalDateTime createdAt
    );

    int deleteScrap(@Param("userId") String userId, @Param("jobPostId") String jobPostId);
}

