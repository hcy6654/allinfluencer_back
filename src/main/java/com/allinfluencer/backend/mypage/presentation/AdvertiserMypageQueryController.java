package com.allinfluencer.backend.mypage.presentation;

import com.allinfluencer.backend.common.security.AuthenticatedUser;
import com.allinfluencer.backend.mypage.application.query.AdvertiserCandidatesQueryService;
import com.allinfluencer.backend.mypage.application.query.AdvertiserMypageQueryService;
import com.allinfluencer.backend.mypage.application.query.AdvertiserOverviewQueryService;
import com.allinfluencer.backend.mypage.application.query.dto.JobPostSummaryDto;
import com.allinfluencer.backend.mypage.application.query.dto.PageResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/my/advertiser")
@Validated
public class AdvertiserMypageQueryController {
    private final AdvertiserMypageQueryService queryService;
    private final AdvertiserCandidatesQueryService candidatesQueryService;
    private final AdvertiserOverviewQueryService overviewQueryService;

    public AdvertiserMypageQueryController(
            AdvertiserMypageQueryService queryService,
            AdvertiserCandidatesQueryService candidatesQueryService,
            AdvertiserOverviewQueryService overviewQueryService
    ) {
        this.queryService = queryService;
        this.candidatesQueryService = candidatesQueryService;
        this.overviewQueryService = overviewQueryService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADVERTISER')")
    public java.util.Map<String, Object> getOverview(@AuthenticationPrincipal AuthenticatedUser user) {
        requireUser(user);
        return overviewQueryService.getOverview(user.userId());
    }

    @GetMapping("/job-posts")
    @PreAuthorize("hasRole('ADVERTISER')")
    public PageResult<JobPostSummaryDto> getMyJobPosts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        requireUser(user);
        return queryService.getMyJobPosts(user.userId(), status, cursor, limit);
    }

    @GetMapping("/job-posts/{jobPostId}/offers")
    @PreAuthorize("hasRole('ADVERTISER')")
    public java.util.Map<String, Object> getJobPostOffers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String jobPostId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        requireUser(user);
        try {
            return candidatesQueryService.getOffers(user.userId(), jobPostId, status, cursor, limit);
        } catch (IllegalArgumentException e) {
            if ("JOB_POST_NOT_FOUND".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 공고");
            }
            throw e;
        }
    }

    @GetMapping("/job-posts/{jobPostId}/applicants")
    @PreAuthorize("hasRole('ADVERTISER')")
    public java.util.Map<String, Object> getJobPostApplicants(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String jobPostId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        requireUser(user);
        try {
            return candidatesQueryService.getApplicants(user.userId(), jobPostId, status, cursor, limit);
        } catch (IllegalArgumentException e) {
            if ("JOB_POST_NOT_FOUND".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 공고");
            }
            throw e;
        }
    }

    private static void requireUser(AuthenticatedUser user) {
        if (user == null || user.userId() == null || user.userId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
    }
}


