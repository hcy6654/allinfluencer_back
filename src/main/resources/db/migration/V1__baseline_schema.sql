-- Flyway baseline schema (Spring Boot backend expected tables)
-- Notes:
-- - Prisma(NestJS)에서 쓰던 camelCase 컬럼명을 그대로 유지하기 위해 컬럼은 반드시 쌍따옴표로 생성합니다.
-- - id는 기존 프로젝트 흐름과 호환을 위해 TEXT(주로 uuid 문자열)로 둡니다.

-- Extensions (필요 시)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Enums
DO $$ BEGIN
    CREATE TYPE "UserRole" AS ENUM ('INFLUENCER', 'ADVERTISER', 'ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE "UserStatus" AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE "Platform" AS ENUM ('YOUTUBE', 'INSTAGRAM', 'TIKTOK', 'NAVER', 'BLOG', 'ETC');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE "JobPostStatus" AS ENUM ('DRAFT', 'OPEN', 'CLOSED', 'COMPLETED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE "ApplicationStatus" AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE "OfferStatus" AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    "email" TEXT,
    "username" TEXT,
    "passwordHash" TEXT,
    "displayName" TEXT NOT NULL,
    "avatar" TEXT,
    "role" "UserRole" NOT NULL,
    "status" "UserStatus" NOT NULL DEFAULT 'ACTIVE',
    "bio" TEXT,
    "website" TEXT,
    "lastLoginAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_key ON users ("email");
CREATE UNIQUE INDEX IF NOT EXISTS users_username_key ON users ("username");
CREATE INDEX IF NOT EXISTS users_role_status_idx ON users ("role", "status");
CREATE INDEX IF NOT EXISTS users_createdAt_idx ON users ("createdAt");

-- INFLUENCER PROFILES
CREATE TABLE IF NOT EXISTS influencer_profiles (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    "categories" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "followers" BIGINT DEFAULT 0,
    "avgEngagement" DOUBLE PRECISION DEFAULT 0.0,
    "ratePerPost" BIGINT DEFAULT 0,
    "location" TEXT,
    "languages" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "headline" TEXT,
    "bio" TEXT,
    "skills" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "portfolioUrls" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "resumeJson" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS influencer_profiles_userId_idx ON influencer_profiles ("userId");

-- ADVERTISER COMPANIES
CREATE TABLE IF NOT EXISTS advertiser_companies (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    "companyName" TEXT NOT NULL,
    "industry" TEXT NOT NULL,
    "description" TEXT,
    "businessRegistrationNumber" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS advertiser_companies_userId_idx ON advertiser_companies ("userId");

-- CHANNELS
CREATE TABLE IF NOT EXISTS channels (
    id TEXT PRIMARY KEY,
    "influencerProfileId" TEXT NOT NULL REFERENCES influencer_profiles(id) ON DELETE CASCADE,
    "platform" "Platform" NOT NULL,
    "channelUrl" TEXT,
    "channelHandle" TEXT,
    "followers" BIGINT DEFAULT 0,
    "avgViews" BIGINT DEFAULT 0,
    "avgLikes" BIGINT DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS channels_influencerProfileId_idx ON channels ("influencerProfileId");

-- JOB POSTS
CREATE TABLE IF NOT EXISTS job_posts (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "companyId" TEXT REFERENCES advertiser_companies(id) ON DELETE SET NULL,
    "title" TEXT NOT NULL,
    "description" TEXT,
    "requirements" TEXT,
    "budget" BIGINT DEFAULT 0,
    "categories" TEXT[] DEFAULT ARRAY[]::TEXT[],
    "platforms" "Platform"[] DEFAULT ARRAY[]::"Platform"[],
    "deadline" TIMESTAMP(3),
    "status" "JobPostStatus" NOT NULL DEFAULT 'OPEN',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS job_posts_userId_createdAt_idx ON job_posts ("userId", "createdAt");
CREATE INDEX IF NOT EXISTS job_posts_status_idx ON job_posts ("status");

-- APPLICATIONS
CREATE TABLE IF NOT EXISTS applications (
    id TEXT PRIMARY KEY,
    "jobPostId" TEXT NOT NULL REFERENCES job_posts(id) ON DELETE CASCADE,
    "userId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "status" "ApplicationStatus" NOT NULL DEFAULT 'PENDING',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS applications_jobPostId_idx ON applications ("jobPostId");
CREATE INDEX IF NOT EXISTS applications_userId_idx ON applications ("userId");
CREATE INDEX IF NOT EXISTS applications_status_idx ON applications ("status");

-- OFFERS
CREATE TABLE IF NOT EXISTS offers (
    id TEXT PRIMARY KEY,
    "jobPostId" TEXT NOT NULL REFERENCES job_posts(id) ON DELETE CASCADE,
    "senderId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "receiverId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "amount" BIGINT DEFAULT 0,
    "description" TEXT,
    "deadline" TIMESTAMP(3),
    "status" "OfferStatus" NOT NULL DEFAULT 'PENDING',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS offers_jobPostId_idx ON offers ("jobPostId");
CREATE INDEX IF NOT EXISTS offers_senderId_idx ON offers ("senderId");
CREATE INDEX IF NOT EXISTS offers_receiverId_idx ON offers ("receiverId");
CREATE INDEX IF NOT EXISTS offers_status_idx ON offers ("status");

-- CONTRACTS (간단 형태)
CREATE TABLE IF NOT EXISTS contracts (
    id TEXT PRIMARY KEY,
    "offerId" TEXT REFERENCES offers(id) ON DELETE SET NULL,
    "userId" TEXT REFERENCES users(id) ON DELETE SET NULL,
    "status" TEXT NOT NULL DEFAULT 'ACTIVE',
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS contracts_offerId_idx ON contracts ("offerId");
CREATE INDEX IF NOT EXISTS contracts_userId_idx ON contracts ("userId");
CREATE INDEX IF NOT EXISTS contracts_status_idx ON contracts ("status");

-- REVIEWS
CREATE TABLE IF NOT EXISTS reviews (
    id TEXT PRIMARY KEY,
    "senderId" TEXT REFERENCES users(id) ON DELETE SET NULL,
    "receiverId" TEXT REFERENCES users(id) ON DELETE SET NULL,
    "rating" INTEGER NOT NULL DEFAULT 0,
    "comment" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS reviews_receiverId_idx ON reviews ("receiverId");

-- SCRAPS
CREATE TABLE IF NOT EXISTS scraps (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "jobPostId" TEXT NOT NULL REFERENCES job_posts(id) ON DELETE CASCADE,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS scraps_user_job_unique ON scraps ("userId", "jobPostId");
CREATE INDEX IF NOT EXISTS scraps_userId_createdAt_idx ON scraps ("userId", "createdAt");

-- USER IDENTITIES (OAuth)
CREATE TABLE IF NOT EXISTS user_identities (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "provider" TEXT NOT NULL,
    "providerUserId" TEXT NOT NULL,
    "email" TEXT,
    "linkedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS user_identities_provider_user_unique ON user_identities ("provider", "providerUserId");
CREATE INDEX IF NOT EXISTS user_identities_userId_idx ON user_identities ("userId");

-- REFRESH SESSIONS
CREATE TABLE IF NOT EXISTS refresh_sessions (
    id TEXT PRIMARY KEY,
    "userId" TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    "jti" TEXT NOT NULL,
    "uaHash" TEXT,
    "ipHash" TEXT,
    "expiresAt" TIMESTAMP(3) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS refresh_sessions_jti_unique ON refresh_sessions ("jti");
CREATE INDEX IF NOT EXISTS refresh_sessions_userId_idx ON refresh_sessions ("userId");

