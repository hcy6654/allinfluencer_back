package com.allinfluencer.backend.mypage.domain.application;

import com.allinfluencer.backend.mypage.domain.DomainException;

import java.time.LocalDateTime;

/**
 * Application Aggregate (간단 버전)
 * - 광고주가 PENDING -> (ACCEPTED/REJECTED)로 전환하는 최소 규칙만 반영
 */
public final class Application {
    private final String id;
    private final String jobPostId;
    private final String applicantUserId;
    private final String advertiserUserId;
    private ApplicationStatus status;
    private LocalDateTime updatedAt;

    private Application(
            String id,
            String jobPostId,
            String applicantUserId,
            String advertiserUserId,
            ApplicationStatus status,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.jobPostId = jobPostId;
        this.applicantUserId = applicantUserId;
        this.advertiserUserId = advertiserUserId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static Application rehydrate(
            String id,
            String jobPostId,
            String applicantUserId,
            String advertiserUserId,
            ApplicationStatus status,
            LocalDateTime updatedAt
    ) {
        return new Application(id, jobPostId, applicantUserId, advertiserUserId, status, updatedAt);
    }

    public void changeStatusByAdvertiser(String actorUserId, ApplicationStatus next, LocalDateTime now) {
        if (!advertiserUserId.equals(actorUserId)) {
            throw new DomainException("FORBIDDEN", "지원서 상태 변경 권한이 없습니다.");
        }
        if (status == ApplicationStatus.WITHDRAWN) {
            throw new DomainException("INVALID_STATE", "철회된 지원서는 상태 변경할 수 없습니다.");
        }
        // 최소 정책: PENDING만 결정 가능
        if (status != ApplicationStatus.PENDING) {
            throw new DomainException("INVALID_STATE", "PENDING 상태의 지원서만 상태 변경할 수 있습니다.");
        }
        if (next != ApplicationStatus.ACCEPTED && next != ApplicationStatus.REJECTED) {
            throw new DomainException("INVALID_ARGUMENT", "지원서 상태는 ACCEPTED 또는 REJECTED만 설정할 수 있습니다.");
        }
        this.status = next;
        this.updatedAt = now;
    }

    public String id() { return id; }
    public String jobPostId() { return jobPostId; }
    public String applicantUserId() { return applicantUserId; }
    public String advertiserUserId() { return advertiserUserId; }
    public ApplicationStatus status() { return status; }
    public LocalDateTime updatedAt() { return updatedAt; }
}

