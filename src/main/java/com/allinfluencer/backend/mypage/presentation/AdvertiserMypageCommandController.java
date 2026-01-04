package com.allinfluencer.backend.mypage.presentation;

import com.allinfluencer.backend.common.security.AuthenticatedUser;
import com.allinfluencer.backend.common.web.dto.IdResponse;
import com.allinfluencer.backend.mypage.application.command.AdvertiserMypageCommandService;
import com.allinfluencer.backend.mypage.application.command.ApplicationStatusCommandHandler;
import com.allinfluencer.backend.mypage.application.command.OfferStatusCommandHandler;
import com.allinfluencer.backend.mypage.application.command.model.CreateJobPostCommand;
import com.allinfluencer.backend.mypage.application.command.model.CreateOfferCommand;
import com.allinfluencer.backend.mypage.presentation.dto.CreateJobPostRequest;
import com.allinfluencer.backend.mypage.presentation.dto.CreateOfferRequest;
import com.allinfluencer.backend.mypage.presentation.dto.UpdateApplicationStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/my/advertiser")
@Validated
public class AdvertiserMypageCommandController {
    private final AdvertiserMypageCommandService commandService;
    private final OfferStatusCommandHandler offerStatusCommandHandler;
    private final ApplicationStatusCommandHandler applicationStatusCommandHandler;

    public AdvertiserMypageCommandController(
            AdvertiserMypageCommandService commandService,
            OfferStatusCommandHandler offerStatusCommandHandler,
            ApplicationStatusCommandHandler applicationStatusCommandHandler
    ) {
        this.commandService = commandService;
        this.offerStatusCommandHandler = offerStatusCommandHandler;
        this.applicationStatusCommandHandler = applicationStatusCommandHandler;
    }

    @PostMapping("/job-posts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADVERTISER')")
    public IdResponse createJobPost(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateJobPostRequest body
    ) {
        requireUser(user);
        String id = commandService.createJobPost(
                user.userId(),
                new CreateJobPostCommand(
                        body.title(),
                        body.description(),
                        body.requirements(),
                        body.budget(),
                        body.categories(),
                        body.platforms(),
                        body.deadline()
                )
        );
        return new IdResponse(id);
    }

    @PostMapping("/job-posts/{jobPostId}/offers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADVERTISER')")
    public IdResponse createOffer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String jobPostId,
            @Valid @RequestBody CreateOfferRequest body
    ) {
        requireUser(user);
        try {
            String id = commandService.createOffer(
                    user.userId(),
                    jobPostId,
                    new CreateOfferCommand(
                            body.receiverId(),
                            body.amount(),
                            body.description(),
                            body.deadline()
                    )
            );
            return new IdResponse(id);
        } catch (IllegalArgumentException e) {
            if ("JOB_POST_NOT_FOUND".equals(e.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 공고");
            }
            throw e;
        }
    }

    /**
     * 광고주가 보낸 오퍼를 만료 처리 (PENDING만 가능)
     * - PATCH /my/advertiser/offers/{offerId}/expire
     */
    @PatchMapping("/offers/{offerId}/expire")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADVERTISER')")
    public void expireOffer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String offerId
    ) {
        requireUser(user);
        offerStatusCommandHandler.expireOfferByAdvertiser(user.userId(), offerId);
    }

    /**
     * 광고주가 지원서 상태 변경 (PENDING -> ACCEPTED/REJECTED)
     * - PATCH /my/advertiser/applications/{applicationId}/status
     */
    @PatchMapping("/applications/{applicationId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADVERTISER')")
    public void updateApplicationStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest body
    ) {
        requireUser(user);
        applicationStatusCommandHandler.changeStatusByAdvertiser(user.userId(), applicationId, body.status());
    }

    private static void requireUser(AuthenticatedUser user) {
        if (user == null || user.userId() == null || user.userId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
    }
}


