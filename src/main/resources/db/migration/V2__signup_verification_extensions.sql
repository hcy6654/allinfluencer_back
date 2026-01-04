-- 신규 회원가입 요구사항 확장:
-- - 사업자: 사업자등록번호 + 인증 상태/요청 정보
-- - 인플루언서: 인증 상태/요청 정보
-- 기존 Prisma 스키마를 변경하지 않도록, 가능한 한 "추가 테이블"을 사용합니다.

-- 1) advertiser_companies에 사업자등록번호 컬럼 추가 (없으면)
ALTER TABLE advertiser_companies
    ADD COLUMN IF NOT EXISTS "businessRegistrationNumber" TEXT;

-- 2) 인플루언서 인증 요청 테이블
CREATE TABLE IF NOT EXISTS influencer_verifications (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING', -- PENDING | VERIFIED | REJECTED
    method TEXT, -- 예: KYC, SNS, DOCUMENT
    submittedAt TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verifiedAt TIMESTAMP(3),
    meta JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS influencer_verifications_userId_idx ON influencer_verifications ("userId");
CREATE INDEX IF NOT EXISTS influencer_verifications_status_idx ON influencer_verifications (status);

-- 3) 사업자 인증 요청 테이블
CREATE TABLE IF NOT EXISTS advertiser_verifications (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING', -- PENDING | VERIFIED | REJECTED
    method TEXT,
    submittedAt TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verifiedAt TIMESTAMP(3),
    meta JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS advertiser_verifications_userId_idx ON advertiser_verifications ("userId");
CREATE INDEX IF NOT EXISTS advertiser_verifications_status_idx ON advertiser_verifications (status);

