package com.allinfluencer.backend.admin.application.query;

import com.allinfluencer.backend.admin.application.query.dto.PendingInfluencerDto;
import com.allinfluencer.backend.admin.application.query.dto.PendingAdvertiserDto;
import com.allinfluencer.backend.admin.infrastructure.mybatis.AdminApprovalReadMapper;
import com.allinfluencer.backend.mypage.application.query.dto.PageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AdminApprovalQueryService {
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AdminApprovalReadMapper mapper;

    public AdminApprovalQueryService(AdminApprovalReadMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<PendingInfluencerDto> getPendingInfluencers(String cursor, Integer limit) {
        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);

        List<AdminApprovalReadMapper.PendingInfluencerRow> rows =
                mapper.selectPendingInfluencers(cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).submittedAt().toString() : null;

        return new PageResult<>(
                pageRows.stream().map(r -> new PendingInfluencerDto(
                        r.userId(),
                        r.email(),
                        r.displayName(),
                        r.categories(),
                        r.submittedAt()
                )).toList(),
                hasMore,
                nextCursor
        );
    }

    public long getPendingInfluencerCount() {
        return mapper.countPendingInfluencers();
    }

    public PageResult<PendingAdvertiserDto> getPendingAdvertisers(String cursor, Integer limit) {
        int pageSize = normalizeLimit(limit);
        LocalDateTime cursorDt = parseCursor(cursor);

        var rows = mapper.selectPendingAdvertisers(cursorDt, pageSize + 1);

        boolean hasMore = rows.size() > pageSize;
        var pageRows = hasMore ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasMore ? pageRows.get(pageRows.size() - 1).submittedAt().toString() : null;

        return new PageResult<>(
                pageRows.stream().map(r -> new PendingAdvertiserDto(
                        r.userId(),
                        r.email(),
                        r.companyName(),
                        r.industry(),
                        r.businessRegistrationNumber(),
                        r.submittedAt()
                )).toList(),
                hasMore,
                nextCursor
        );
    }

    public long getPendingAdvertiserCount() {
        return mapper.countPendingAdvertisers();
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

