package com.allinfluencer.backend.admin.presentation;

import com.allinfluencer.backend.admin.application.query.AdminApprovalQueryService;
import com.allinfluencer.backend.admin.application.query.dto.PendingInfluencerDto;
import com.allinfluencer.backend.mypage.application.query.dto.PageResult;
import com.allinfluencer.backend.signup.application.InfluencerApprovalCommandService;
import com.allinfluencer.backend.signup.application.AdvertiserVerificationCommandService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인플루언서 가입(승인) 관리 API
 * - 인플루언서는 ADMIN 승인 전까지 로그인 불가
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInfluencerApprovalController {
    private final InfluencerApprovalCommandService commandService;
    private final AdminApprovalQueryService queryService;
    private final AdvertiserVerificationCommandService advertiserCommandService;

    public AdminInfluencerApprovalController(
            InfluencerApprovalCommandService commandService,
            AdminApprovalQueryService queryService,
            AdvertiserVerificationCommandService advertiserCommandService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.advertiserCommandService = advertiserCommandService;
    }

    @GetMapping("/influencers/pending")
    public PageResult<PendingInfluencerDto> pendingInfluencers(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        return queryService.getPendingInfluencers(cursor, limit);
    }

    @GetMapping("/influencers/pending/count")
    public java.util.Map<String, Object> pendingInfluencersCount() {
        return java.util.Map.of("count", queryService.getPendingInfluencerCount());
    }

    @GetMapping("/advertisers/pending")
    public com.allinfluencer.backend.mypage.application.query.dto.PageResult<com.allinfluencer.backend.admin.application.query.dto.PendingAdvertiserDto> pendingAdvertisers(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        return queryService.getPendingAdvertisers(cursor, limit);
    }

    @GetMapping("/advertisers/pending/count")
    public java.util.Map<String, Object> pendingAdvertisersCount() {
        return java.util.Map.of("count", queryService.getPendingAdvertiserCount());
    }

    @PatchMapping("/influencers/{userId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable String userId) {
        commandService.approve(userId);
    }

    @PatchMapping("/influencers/{userId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable String userId) {
        commandService.reject(userId);
    }

    @PatchMapping("/advertisers/{userId}/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyAdvertiser(@PathVariable String userId) {
        advertiserCommandService.verify(userId);
    }

    @PatchMapping("/advertisers/{userId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectAdvertiser(@PathVariable String userId) {
        advertiserCommandService.reject(userId);
    }
}

