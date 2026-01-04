package com.allinfluencer.backend.mypage.application.command;

import com.allinfluencer.backend.mypage.application.port.out.OfferPort;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.mypage.domain.offer.Offer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Command Handler (use-case)
 * - domain aggregate를 로드 -> 도메인 규칙으로 상태 전이 -> 저장
 */
@Service
public class OfferStatusCommandHandler {
    private final OfferPort offerPort;

    public OfferStatusCommandHandler(OfferPort offerPort) {
        this.offerPort = offerPort;
    }

    @Transactional
    public void expireOfferByAdvertiser(String actorUserId, String offerId) {
        Offer offer = offerPort.findById(offerId);
        if (offer == null) {
            throw new DomainException("NOT_FOUND", "존재하지 않는 오퍼");
        }

        offer.expireBySender(actorUserId, LocalDateTime.now());
        offerPort.updateStatus(offer.id(), offer.status().name(), offer.updatedAt());
    }
}

