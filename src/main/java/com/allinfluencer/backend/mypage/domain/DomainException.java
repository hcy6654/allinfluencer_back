package com.allinfluencer.backend.mypage.domain;

/**
 * 도메인 규칙 위반을 표현하는 예외.
 * - presentation/application에서는 이 code를 보고 HTTP 상태로 매핑 가능
 */
public class DomainException extends RuntimeException {
    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}

