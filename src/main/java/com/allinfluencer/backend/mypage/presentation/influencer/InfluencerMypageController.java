package com.allinfluencer.backend.mypage.presentation.influencer;

import com.allinfluencer.backend.common.security.AuthenticatedUser;
import com.allinfluencer.backend.mypage.application.command.InfluencerMypageCommandService;
import com.allinfluencer.backend.mypage.application.command.InfluencerResumeCommandService;
import com.allinfluencer.backend.mypage.application.query.InfluencerMypageQueryService;
import com.allinfluencer.backend.mypage.domain.DomainException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * NestJS InfluencerMyPageController 호환:
 * - GET    /my/influencer/overview
 * - GET    /my/influencer/resume
 * - PUT    /my/influencer/resume
 * - GET    /my/influencer/applications
 * - GET    /my/influencer/scraps
 * - POST   /my/influencer/scraps
 * - DELETE /my/influencer/scraps/:jobPostId
 *
 * TODO: CQRS/DDD로 실제 구현 연결.
 */
@RestController
@RequestMapping("/my/influencer")
@PreAuthorize("hasRole('INFLUENCER')")
public class InfluencerMypageController {
    private final InfluencerMypageQueryService queryService;
    private final InfluencerMypageCommandService commandService;
    private final InfluencerResumeCommandService resumeCommandService;

    public InfluencerMypageController(
            InfluencerMypageQueryService queryService,
            InfluencerMypageCommandService commandService,
            InfluencerResumeCommandService resumeCommandService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.resumeCommandService = resumeCommandService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview(@AuthenticationPrincipal AuthenticatedUser user) {
        require(user);
        return queryService.getOverview(user.userId());
    }

    @GetMapping("/resume")
    public Map<String, Object> getResume(@AuthenticationPrincipal AuthenticatedUser user) {
        require(user);
        return queryService.getResume(user.userId());
    }

    @PutMapping("/resume")
    public Map<String, Object> updateResume(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody UpdateResumeRequest body) {
        require(user);
        resumeCommandService.update(
                user.userId(),
                body.headline(),
                body.bio(),
                body.skills(),
                body.portfolioUrls(),
                body.resumeJson()
        );
        return queryService.getResume(user.userId());
    }

    @GetMapping("/applications")
    public Map<String, Object> myApplications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        require(user);
        var page = queryService.getMyApplications(user.userId(), status, cursor, limit);
        return Map.of("items", page.items(), "hasMore", page.hasMore(), "nextCursor", page.nextCursor());
    }

    @GetMapping("/scraps")
    public Map<String, Object> myScraps(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit
    ) {
        require(user);
        var page = queryService.getMyScraps(user.userId(), cursor, limit);
        return Map.of("items", page.items(), "hasMore", page.hasMore(), "nextCursor", page.nextCursor());
    }

    @PostMapping("/scraps")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createScrap(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreateScrapRequest body) {
        require(user);
        commandService.createScrap(user.userId(), body.jobPostId());
        return Map.of("success", true);
    }

    @DeleteMapping("/scraps/{jobPostId}")
    public Map<String, Object> deleteScrap(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String jobPostId) {
        require(user);
        commandService.deleteScrap(user.userId(), jobPostId);
        return Map.of("success", true);
    }

    private static void require(AuthenticatedUser user) {
        if (user == null || user.userId() == null || user.userId().isBlank()) {
            throw new DomainException("INVALID_ARGUMENT", "Unauthorized");
        }
    }

    public record CreateScrapRequest(@NotBlank String jobPostId) {}

    public record UpdateResumeRequest(
            String headline,
            String bio,
            String[] skills,
            String[] portfolioUrls,
            Object resumeJson
    ) {}
}

