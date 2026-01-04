package com.allinfluencer.backend.mypage.application.command;

import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.influencer.InfluencerResumeCommandMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InfluencerResumeCommandService {
    private final InfluencerResumeCommandMapper mapper;
    private final ObjectMapper objectMapper;

    public InfluencerResumeCommandService(InfluencerResumeCommandMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void update(
            String userId,
            String headline,
            String bio,
            String[] skills,
            String[] portfolioUrls,
            Object resumeJson
    ) {
        String resumeJsonStr = null;
        if (resumeJson != null) {
            try {
                resumeJsonStr = objectMapper.writeValueAsString(resumeJson);
            } catch (Exception e) {
                throw new DomainException("INVALID_ARGUMENT", "resumeJson 직렬화에 실패했습니다.");
            }
        }

        int updated = mapper.updateResume(
                userId,
                headline,
                bio,
                skills,
                portfolioUrls,
                resumeJsonStr,
                LocalDateTime.now()
        );
        if (updated == 0) {
            throw new DomainException("NOT_FOUND", "인플루언서 프로필을 찾을 수 없습니다.");
        }
    }
}

