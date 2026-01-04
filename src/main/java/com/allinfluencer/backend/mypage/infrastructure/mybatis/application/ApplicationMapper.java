package com.allinfluencer.backend.mypage.infrastructure.mybatis.application;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface ApplicationMapper {

    ApplicationRow selectById(@Param("applicationId") String applicationId);

    int updateStatus(
            @Param("applicationId") String applicationId,
            @Param("status") String status,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    record ApplicationRow(
            String id,
            String jobPostId,
            String userId,
            String advertiserUserId,
            String status,
            LocalDateTime updatedAt
    ) {
    }
}

