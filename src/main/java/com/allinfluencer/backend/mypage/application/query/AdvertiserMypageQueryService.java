package com.allinfluencer.backend.mypage.application.query;

import com.allinfluencer.backend.mypage.application.query.dto.JobPostOffersResult;
import com.allinfluencer.backend.mypage.application.query.dto.JobPostSummaryDto;
import com.allinfluencer.backend.mypage.application.query.dto.PageResult;
import com.allinfluencer.backend.mypage.application.query.dto.OfferSummaryDto;
import com.allinfluencer.backend.mypage.application.port.out.AdvertiserMypageReadPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * CQRS - Query side
 * - DB 조회는 MyBatis로 수행 (복잡한 조인/필터/페이지네이션에 유리)
 * - 반환은 Read Model DTO로 고정
 */
@Service
public class AdvertiserMypageQueryService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AdvertiserMypageReadPort readPort;

    public AdvertiserMypageQueryService(AdvertiserMypageReadPort readPort) {
        this.readPort = readPort;
    }

    public PageResult<JobPostSummaryDto> getMyJobPosts(String userId, String status, String cursor, Integer limit) {
        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);

        List<AdvertiserMypageReadPort.JobPostSummaryProjection> rows =
                readPort.findMyJobPosts(userId, status, cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).createdAt().toString() : null;

        return new PageResult<>(
                pageRows.stream().map(p -> new JobPostSummaryDto(
                        p.id(),
                        p.title(),
                        p.description(),
                        p.budget(),
                        p.categories(),
                        p.platforms(),
                        p.deadline(),
                        p.status(),
                        p.createdAt(),
                        p.updatedAt(),
                        new JobPostSummaryDto.Stats(p.applicationCount(), p.offerCount())
                )).toList(),
                hasMore,
                nextCursor
        );
    }

    public JobPostOffersResult getJobPostOffers(
            String userId,
            String jobPostId,
            String status,
            String cursor,
            Integer limit
    ) {
        var jobPostRef = readPort.findJobPostRef(userId, jobPostId);
        if (jobPostRef == null) {
            // NestJS 쪽은 404/403을 구분하지만, 여기서는 우선 404로 통일 (추후 권한 검증 강화 가능)
            throw new IllegalArgumentException("JOB_POST_NOT_FOUND");
        }

        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);

        List<AdvertiserMypageReadPort.OfferSummaryProjection> rows =
                readPort.findJobPostOffers(userId, jobPostId, status, cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).createdAt().toString() : null;

        PageResult<OfferSummaryDto> page = new PageResult<>(
                pageRows.stream().map(p -> new OfferSummaryDto(
                        p.id(),
                        p.jobPostId(),
                        p.amount(),
                        p.description(),
                        p.deadline(),
                        p.status(),
                        p.createdAt(),
                        p.updatedAt(),
                        new OfferSummaryDto.Receiver(p.receiverId(), p.receiverDisplayName(), p.receiverAvatar())
                )).toList(),
                hasMore,
                nextCursor
        );

        return new JobPostOffersResult(
                new JobPostOffersResult.JobPostRef(jobPostRef.id(), jobPostRef.title()),
                page
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


