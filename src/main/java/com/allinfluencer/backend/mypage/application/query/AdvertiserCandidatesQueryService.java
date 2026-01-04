package com.allinfluencer.backend.mypage.application.query;

import com.allinfluencer.backend.mypage.infrastructure.mybatis.advertiser.AdvertiserCandidatesReadMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
public class AdvertiserCandidatesQueryService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AdvertiserCandidatesReadMapper mapper;

    public AdvertiserCandidatesQueryService(AdvertiserCandidatesReadMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getApplicants(String userId, String jobPostId, String status, String cursor, Integer limit) {
        var jobPost = mapper.selectJobPostRef(userId, jobPostId);
        if (jobPost == null) {
            throw new IllegalArgumentException("JOB_POST_NOT_FOUND");
        }

        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);

        List<AdvertiserCandidatesReadMapper.ApplicantRow> rows =
                mapper.selectApplicants(userId, jobPostId, status, cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).appliedAt().toString() : null;

        var items = pageRows.stream().map(r -> Map.of(
                "id", r.id(),
                "status", r.status(),
                "appliedAt", r.appliedAt().toString(),
                "influencer", Map.of(
                        "id", r.influencerId(),
                        "displayName", r.influencerDisplayName(),
                        "avatar", r.influencerAvatar(),
                        "profile", Map.of(
                                "headline", r.headline(),
                                "followers", r.followers(),
                                "avgEngagement", r.avgEngagement(),
                                "categories", r.categories()
                        )
                )
        )).toList();

        return Map.of(
                "jobPost", Map.of("id", jobPost.id(), "title", jobPost.title()),
                "items", items,
                "hasMore", hasMore,
                "nextCursor", nextCursor
        );
    }

    public Map<String, Object> getOffers(String userId, String jobPostId, String status, String cursor, Integer limit) {
        var jobPost = mapper.selectJobPostRef(userId, jobPostId);
        if (jobPost == null) {
            throw new IllegalArgumentException("JOB_POST_NOT_FOUND");
        }

        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);

        List<AdvertiserCandidatesReadMapper.OfferRow> rows =
                mapper.selectOffers(userId, jobPostId, status, cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).offeredAt().toString() : null;

        var items = pageRows.stream().map(r -> Map.of(
                "id", r.id(),
                "status", r.status(),
                "offeredAt", r.offeredAt().toString(),
                "influencer", Map.of(
                        "id", r.influencerId(),
                        "displayName", r.influencerDisplayName(),
                        "avatar", r.influencerAvatar(),
                        "profile", Map.of(
                                "headline", r.headline(),
                                "followers", r.followers(),
                                "avgEngagement", r.avgEngagement(),
                                "categories", r.categories()
                        )
                )
        )).toList();

        return Map.of(
                "jobPost", Map.of("id", jobPost.id(), "title", jobPost.title()),
                "items", items,
                "hasMore", hasMore,
                "nextCursor", nextCursor
        );
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null) return DEFAULT_LIMIT;
        if (limit < 1) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private static LocalDateTime parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            return LocalDateTime.parse(cursor);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}

