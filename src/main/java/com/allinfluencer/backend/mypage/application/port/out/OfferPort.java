package com.allinfluencer.backend.mypage.application.port.out;

import com.allinfluencer.backend.mypage.domain.offer.Offer;

import java.time.LocalDateTime;

public interface OfferPort {
    Offer findById(String offerId);
    void updateStatus(String offerId, String status, LocalDateTime updatedAt);
}

