package com.allinfluencer.backend.mypage.application.command;

import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.scrap.ScrapCommandMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InfluencerMypageCommandService {
    private final ScrapCommandMapper scrapCommandMapper;

    public InfluencerMypageCommandService(ScrapCommandMapper scrapCommandMapper) {
        this.scrapCommandMapper = scrapCommandMapper;
    }

    @Transactional
    public void createScrap(String userId, String jobPostId) {
        Integer exists = scrapCommandMapper.existsScrap(userId, jobPostId);
        if (exists != null && exists == 1) {
            throw new DomainException("INVALID_STATE", "이미 스크랩한 공고입니다.");
        }
        scrapCommandMapper.insertScrap(UUID.randomUUID().toString(), userId, jobPostId, LocalDateTime.now());
    }

    @Transactional
    public void deleteScrap(String userId, String jobPostId) {
        int deleted = scrapCommandMapper.deleteScrap(userId, jobPostId);
        if (deleted == 0) {
            throw new DomainException("NOT_FOUND", "스크랩을 찾을 수 없습니다.");
        }
    }
}

