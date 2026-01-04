package com.allinfluencer.backend.mypage.application.command;

import com.allinfluencer.backend.mypage.application.command.model.CreateJobPostCommand;
import com.allinfluencer.backend.mypage.application.command.model.CreateOfferCommand;
import com.allinfluencer.backend.mypage.application.port.out.AdvertiserMypageCommandPort;
import com.allinfluencer.backend.mypage.application.port.out.AdvertiserMypageReadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CQRS - Command side
 * - 현재 스키마는 enum/array가 많고, 기존 DB와 정확히 맞추는 것이 중요해서
 *   Command도 MyBatis 기반으로 시작합니다. (추후 도메인 규칙이 커지면 JPA로 옮기기 쉬움)
 */
@Service
public class AdvertiserMypageCommandService {
    private final AdvertiserMypageReadPort readPort;
    private final AdvertiserMypageCommandPort commandPort;

    public AdvertiserMypageCommandService(
            AdvertiserMypageReadPort readPort,
            AdvertiserMypageCommandPort commandPort
    ) {
        this.readPort = readPort;
        this.commandPort = commandPort;
    }

    @Transactional
    public String createJobPost(String userId, CreateJobPostCommand cmd) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // companyId는 기존 스키마상 선택이며, 이관 단계에서는 userId 기반으로 조회/설정 가능
        commandPort.insertJobPost(
                id,
                userId,
                null,
                cmd.title(),
                cmd.description(),
                cmd.requirements(),
                cmd.budget(),
                cmd.categories(),
                cmd.platforms(),
                cmd.deadline(),
                now,
                now
        );

        return id;
    }

    @Transactional
    public String createOffer(String userId, String jobPostId, CreateOfferCommand cmd) {
        // "내 공고"인지 확인 (권한)
        var jobPostRef = readPort.findJobPostRef(userId, jobPostId);
        if (jobPostRef == null) {
            throw new IllegalArgumentException("JOB_POST_NOT_FOUND");
        }

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        commandPort.insertOffer(
                id,
                jobPostId,
                userId,
                cmd.receiverId(),
                cmd.amount(),
                cmd.description(),
                cmd.deadline(),
                now,
                now
        );

        return id;
    }
}


