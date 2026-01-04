package com.allinfluencer.backend.mypage.infrastructure.adapter;

import com.allinfluencer.backend.mypage.application.port.out.OfferPort;
import com.allinfluencer.backend.mypage.domain.offer.Offer;
import com.allinfluencer.backend.mypage.domain.offer.OfferStatus;
import com.allinfluencer.backend.mypage.infrastructure.mybatis.offer.OfferMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class MyBatisOfferAdapter implements OfferPort {
    private final OfferMapper mapper;

    public MyBatisOfferAdapter(OfferMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Offer findById(String offerId) {
        var row = mapper.selectById(offerId);
        if (row == null) return null;
        return Offer.rehydrate(
                row.id(),
                row.jobPostId(),
                row.senderId(),
                row.receiverId(),
                OfferStatus.valueOf(row.status()),
                row.updatedAt()
        );
    }

    @Override
    public void updateStatus(String offerId, String status, LocalDateTime updatedAt) {
        mapper.updateStatus(offerId, status, updatedAt);
    }
}

