package com.allinfluencer.backend.mypage.application.query;

import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.advertiser.AdvertiserOverviewReadMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdvertiserOverviewQueryService {
    private final AdvertiserOverviewReadMapper mapper;

    public AdvertiserOverviewQueryService(AdvertiserOverviewReadMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getOverview(String userId) {
        var company = mapper.selectCompany(userId);
        if (company == null) {
            throw new DomainException("NOT_FOUND", "사업자 프로필을 찾을 수 없습니다.");
        }

        var counts = mapper.countJobPostsByStatus(userId);
        Map<String, Object> jobPosts = new HashMap<>();
        jobPosts.put("total", 0L);
        jobPosts.put("open", 0L);
        jobPosts.put("closed", 0L);
        jobPosts.put("completed", 0L);
        jobPosts.put("cancelled", 0L);

        long total = 0;
        for (var c : counts) {
            total += c.count();
            switch (c.status()) {
                case "OPEN" -> jobPosts.put("open", c.count());
                case "CLOSED" -> jobPosts.put("closed", c.count());
                case "COMPLETED" -> jobPosts.put("completed", c.count());
                case "CANCELLED" -> jobPosts.put("cancelled", c.count());
            }
        }
        jobPosts.put("total", total);

        long recentApplications = mapper.countRecentApplications(userId);
        long activeContracts = mapper.countActiveContracts(userId);
        Double avgRating = mapper.selectAvgRating(userId);

        return Map.of(
                "company", Map.of(
                        "name", company.companyName(),
                        "industry", company.industry(),
                        "description", company.description()
                ),
                "jobPosts", jobPosts,
                "recentStats", Map.of(
                        "recentApplications", recentApplications,
                        "activeContracts", activeContracts,
                        "avgRating", avgRating
                )
        );
    }
}

