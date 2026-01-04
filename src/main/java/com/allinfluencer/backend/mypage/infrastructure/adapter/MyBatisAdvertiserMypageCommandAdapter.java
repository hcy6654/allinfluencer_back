package com.allinfluencer.backend.mypage.infrastructure.adapter;

import com.allinfluencer.backend.mypage.application.port.out.AdvertiserMypageCommandPort;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.AdvertiserMypageCommandMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class MyBatisAdvertiserMypageCommandAdapter implements AdvertiserMypageCommandPort {
    private final AdvertiserMypageCommandMapper mapper;

    public MyBatisAdvertiserMypageCommandAdapter(AdvertiserMypageCommandMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void insertJobPost(
            String id,
            String userId,
            String companyId,
            String title,
            String description,
            String requirements,
            Integer budget,
            String[] categories,
            String[] platforms,
            LocalDateTime deadline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        mapper.insertJobPost(
                id,
                userId,
                companyId,
                title,
                description,
                requirements,
                budget,
                categories,
                platforms,
                deadline,
                createdAt,
                updatedAt
        );
    }

    @Override
    public void insertOffer(
            String id,
            String jobPostId,
            String senderId,
            String receiverId,
            Integer amount,
            String description,
            LocalDateTime deadline,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        mapper.insertOffer(
                id,
                jobPostId,
                senderId,
                receiverId,
                amount,
                description,
                deadline,
                createdAt,
                updatedAt
        );
    }
}


