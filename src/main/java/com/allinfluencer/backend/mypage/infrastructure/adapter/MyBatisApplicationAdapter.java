package com.allinfluencer.backend.mypage.infrastructure.adapter;

import com.allinfluencer.backend.mypage.application.port.out.ApplicationPort;
import com.allinfluencer.backend.mypage.domain.application.Application;
import com.allinfluencer.backend.mypage.domain.application.ApplicationStatus;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.application.ApplicationMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class MyBatisApplicationAdapter implements ApplicationPort {
    private final ApplicationMapper mapper;

    public MyBatisApplicationAdapter(ApplicationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Application findById(String applicationId) {
        var row = mapper.selectById(applicationId);
        if (row == null) return null;
        return Application.rehydrate(
                row.id(),
                row.jobPostId(),
                row.userId(),
                row.advertiserUserId(),
                ApplicationStatus.valueOf(row.status()),
                row.updatedAt()
        );
    }

    @Override
    public void updateStatus(String applicationId, String status, LocalDateTime updatedAt) {
        mapper.updateStatus(applicationId, status, updatedAt);
    }
}

