package com.allinfluencer.backend.mypage.infrastructure.mybatis.offer;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface OfferMapper {

    OfferRow selectById(@Param("offerId") String offerId);

    int updateStatus(
            @Param("offerId") String offerId,
            @Param("status") String status,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    record OfferRow(
            String id,
            String jobPostId,
            String senderId,
            String receiverId,
            String status,
            LocalDateTime updatedAt
    ) {
    }
}

