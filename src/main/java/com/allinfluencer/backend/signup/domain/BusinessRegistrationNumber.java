package com.allinfluencer.backend.signup.domain;

import com.allinfluencer.backend.mypage.domain.DomainException;

public record BusinessRegistrationNumber(String value) {
    public BusinessRegistrationNumber {
        if (value == null || value.isBlank()) {
            throw new DomainException("INVALID_ARGUMENT", "사업자등록번호는 필수입니다.");
        }
        // 단순 규칙: 숫자/하이픈만 허용, 8~20자 (실검증은 외부 연동 시 강화)
        if (!value.matches("[0-9\\-]{8,20}")) {
            throw new DomainException("INVALID_ARGUMENT", "사업자등록번호 형식이 올바르지 않습니다.");
        }
    }
}

