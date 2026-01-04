package com.allinfluencer.backend.mypage.application.port.out;

import com.allinfluencer.backend.mypage.domain.application.Application;

import java.time.LocalDateTime;

public interface ApplicationPort {
    Application findById(String applicationId);
    void updateStatus(String applicationId, String status, LocalDateTime updatedAt);
}

