package com.allinfluencer.backend.mypage.infrastructure.adapter;

import com.allinfluencer.backend.mypage.application.port.out.AdvertiserMypageReadPort;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.AdvertiserMypageReadMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MyBatisAdvertiserMypageReadAdapter implements AdvertiserMypageReadPort {
    private final AdvertiserMypageReadMapper mapper;

    public MyBatisAdvertiserMypageReadAdapter(AdvertiserMypageReadMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<JobPostSummaryProjection> findMyJobPosts(
            String userId,
            String status,
            LocalDateTime cursor,
            int limitPlusOne
    ) {
        return mapper.selectMyJobPosts(userId, status, cursor, limitPlusOne).stream()
                .map(r -> new JobPostSummaryProjection(
                        r.id(),
                        r.title(),
                        r.description(),
                        r.budget(),
                        r.categories(),
                        r.platforms(),
                        r.deadline(),
                        r.status(),
                        r.createdAt(),
                        r.updatedAt(),
                        r.applicationCount(),
                        r.offerCount()
                ))
                .toList();
    }

    @Override
    public JobPostRefProjection findJobPostRef(String userId, String jobPostId) {
        var row = mapper.selectJobPostRef(userId, jobPostId);
        if (row == null) return null;
        return new JobPostRefProjection(row.id(), row.title());
    }

    @Override
    public List<OfferSummaryProjection> findJobPostOffers(
            String userId,
            String jobPostId,
            String status,
            LocalDateTime cursor,
            int limitPlusOne
    ) {
        return mapper.selectJobPostOffers(userId, jobPostId, status, cursor, limitPlusOne).stream()
                .map(r -> new OfferSummaryProjection(
                        r.id(),
                        r.jobPostId(),
                        r.amount(),
                        r.description(),
                        r.deadline(),
                        r.status(),
                        r.createdAt(),
                        r.updatedAt(),
                        r.receiverId(),
                        r.receiverDisplayName(),
                        r.receiverAvatar()
                ))
                .toList();
    }
}


