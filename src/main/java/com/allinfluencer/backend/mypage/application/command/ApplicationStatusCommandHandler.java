package com.allinfluencer.backend.mypage.application.command;

import com.allinfluencer.backend.mypage.application.port.out.ApplicationPort;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.mypage.domain.application.Application;
import com.allinfluencer.backend.mypage.domain.application.ApplicationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApplicationStatusCommandHandler {
    private final ApplicationPort applicationPort;

    public ApplicationStatusCommandHandler(ApplicationPort applicationPort) {
        this.applicationPort = applicationPort;
    }

    @Transactional
    public void changeStatusByAdvertiser(String actorUserId, String applicationId, String status) {
        Application application = applicationPort.findById(applicationId);
        if (application == null) {
            throw new DomainException("NOT_FOUND", "존재하지 않는 지원서");
        }

        ApplicationStatus next;
        try {
            next = ApplicationStatus.valueOf(status);
        } catch (Exception e) {
            throw new DomainException("INVALID_ARGUMENT", "유효하지 않은 지원서 상태입니다.");
        }

        application.changeStatusByAdvertiser(actorUserId, next, LocalDateTime.now());
        applicationPort.updateStatus(application.id(), application.status().name(), application.updatedAt());
    }
}

