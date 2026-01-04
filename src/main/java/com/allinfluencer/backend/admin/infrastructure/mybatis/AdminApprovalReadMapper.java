package com.allinfluencer.backend.admin.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminApprovalReadMapper {
    List<PendingInfluencerRow> selectPendingInfluencers(
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    long countPendingInfluencers();

    List<PendingAdvertiserRow> selectPendingAdvertisers(
            @Param("cursor") LocalDateTime cursor,
            @Param("limitPlusOne") int limitPlusOne
    );

    long countPendingAdvertisers();

    record PendingInfluencerRow(
            String userId,
            String email,
            String displayName,
            String[] categories,
            LocalDateTime submittedAt
    ) {}

    record PendingAdvertiserRow(
            String userId,
            String email,
            String companyName,
            String industry,
            String businessRegistrationNumber,
            LocalDateTime submittedAt
    ) {}
}

