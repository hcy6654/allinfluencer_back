package com.allinfluencer.backend.mypage.application.query;

import com.allinfluencer.backend.mypage.application.query.dto.PageResult;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.influencer.InfluencerMypageReadMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class InfluencerMypageQueryService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final InfluencerMypageReadMapper mapper;

    public InfluencerMypageQueryService(InfluencerMypageReadMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> getOverview(String userId) {
        var row = mapper.selectOverview(userId);
        if (row == null) {
            // 프로필이 아직 없을 수 있음
            return Map.of(
                    "profile", Map.of(
                            "headline", null,
                            "totalFollowers", 0,
                            "avgEngagement", 0,
                            "ratePerPost", null,
                            "channelCount", 0
                    ),
                    "applications", Map.of(
                            "total", 0,
                            "pending", 0,
                            "accepted", 0,
                            "rejected", 0,
                            "withdrawn", 0
                    ),
                    "scrapCount", 0,
                    "stats", Map.of("completedContracts", 0, "avgRating", 0)
            );
        }

        return Map.of(
                "profile", Map.of(
                        "headline", row.headline(),
                        "totalFollowers", row.totalFollowers(),
                        "avgEngagement", row.avgEngagement(),
                        "ratePerPost", row.ratePerPost(),
                        "channelCount", row.channelCount()
                ),
                "applications", Map.of(
                        "total", row.applicationTotal(),
                        "pending", row.applicationPending(),
                        "accepted", row.applicationAccepted(),
                        "rejected", row.applicationRejected(),
                        "withdrawn", row.applicationWithdrawn()
                ),
                "scrapCount", row.scrapCount(),
                "stats", Map.of(
                        "completedContracts", row.completedContracts(),
                        "avgRating", row.avgRating()
                )
        );
    }

    public Map<String, Object> getResume(String userId) {
        var resume = mapper.selectResume(userId);
        var channels = mapper.selectChannels(userId);
        return Map.of(
                "resume", resume == null ? null : Map.<String, Object>ofEntries(
                        Map.entry("headline", resume.headline()),
                        Map.entry("bio", resume.bio()),
                        Map.entry("skills", resume.skills()),
                        Map.entry("portfolioUrls", resume.portfolioUrls()),
                        Map.entry("resumeJson", resume.resumeJson()),
                        Map.entry("categories", resume.categories()),
                        Map.entry("followers", resume.followers()),
                        Map.entry("avgEngagement", resume.avgEngagement()),
                        Map.entry("ratePerPost", resume.ratePerPost()),
                        Map.entry("location", resume.location()),
                        Map.entry("languages", resume.languages())
                ),
                "channels", channels.stream().map(c -> Map.of(
                        "id", c.id(),
                        "platform", c.platform(),
                        "channelUrl", c.channelUrl(),
                        "channelHandle", c.channelHandle(),
                        "followers", c.followers(),
                        "avgViews", c.avgViews(),
                        "avgLikes", c.avgLikes()
                )).toList()
        );
    }

    public PageResult<Map<String, Object>> getMyApplications(String userId, String status, String cursor, Integer limit) {
        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);
        var rows = mapper.selectMyApplications(userId, status, cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).appliedAt().toString() : null;

        var items = pageRows.stream().map(r -> Map.<String, Object>ofEntries(
                Map.entry("id", r.id()),
                Map.entry("status", r.status()),
                Map.entry("appliedAt", r.appliedAt().toString()),
                Map.entry("jobPost", Map.of(
                        "id", r.jobPostId(),
                        "title", r.jobTitle()
                )),
                Map.entry("advertiser", Map.of(
                        "id", r.advertiserId(),
                        "companyName", r.advertiserCompanyName()
                ))
        )).toList();

        return new PageResult<>(items, hasMore, nextCursor);
    }

    public PageResult<Map<String, Object>> getMyScraps(String userId, String cursor, Integer limit) {
        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);
        var rows = mapper.selectMyScraps(userId, cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).scrappedAt().toString() : null;

        var items = pageRows.stream().map(r -> Map.<String, Object>ofEntries(
                Map.entry("jobPost", Map.of(
                        "id", r.jobPostId(),
                        "title", r.jobTitle()
                )),
                Map.entry("scrappedAt", r.scrappedAt().toString())
        )).toList();

        return new PageResult<>(items, hasMore, nextCursor);
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

