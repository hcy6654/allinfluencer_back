package com.allinfluencer.backend.mypage.application.query.dto;

public record JobPostOffersResult(
        JobPostRef jobPost,
        PageResult<OfferSummaryDto> page
) {
    public record JobPostRef(String id, String title) {}
}


