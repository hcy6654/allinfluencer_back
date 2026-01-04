package com.allinfluencer.backend.mypage.infrastructure.mybatis.advertiser;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdvertiserOverviewReadMapper {
    CompanyRow selectCompany(@Param("userId") String userId);

    List<JobPostStatusCountRow> countJobPostsByStatus(@Param("userId") String userId);

    long countRecentApplications(@Param("userId") String userId);

    long countActiveContracts(@Param("userId") String userId);

    Double selectAvgRating(@Param("userId") String userId);

    record CompanyRow(String companyName, String industry, String description) {}

    record JobPostStatusCountRow(String status, long count) {}
}

