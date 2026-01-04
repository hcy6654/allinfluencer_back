package com.allinfluencer.backend.mypage.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AdvertiserMypageCommandMapper {

    int insertJobPost(
            @Param("id") String id,
            @Param("userId") String userId,
            @Param("companyId") String companyId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("requirements") String requirements,
            @Param("budget") Integer budget,
            @Param("categories") String[] categories,
            @Param("platforms") String[] platforms,
            @Param("deadline") LocalDateTime deadline,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    int insertOffer(
            @Param("id") String id,
            @Param("jobPostId") String jobPostId,
            @Param("senderId") String senderId,
            @Param("receiverId") String receiverId,
            @Param("amount") Integer amount,
            @Param("description") String description,
            @Param("deadline") LocalDateTime deadline,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}


