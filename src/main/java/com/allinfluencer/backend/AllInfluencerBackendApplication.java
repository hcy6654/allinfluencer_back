package com.allinfluencer.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
public class AllInfluencerBackendApplication {
    public static void main(String[] args) {
        loadDotEnvLocalAndDefaults();
        SpringApplication.run(AllInfluencerBackendApplication.class, args);
    }

    /**
     * IntelliJ에서 "환경변수 설정 없이" 실행하고 싶을 때를 위한 로더.
     * - 프로젝트 루트(또는 상위 경로)에 있는 .env.local을 찾아 읽습니다.
     * - 값이 비어있는 키는 무시합니다(빈 값 때문에 기본값이 깨지는 문제 방지).
     * - DataSource가 비어있으면 기본 Postgres 설정을 채웁니다.
     */
    private static void loadDotEnvLocalAndDefaults() {
        Map<String, String> dotenv = tryLoadDotenv(".env.local");

        // .env.local 값을 system property로 주입 (이미 env/prop에 있는 값은 유지)
        for (var e : dotenv.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value == null || value.isBlank()) continue;
            if (System.getenv(key) != null) continue;
            if (System.getProperty(key) != null) continue;
            System.setProperty(key, value);
        }

        // DB 기본값 보강 (url/driver가 없거나 비어 있으면 Spring이 부팅 실패함)
        String url = firstNonBlank(
                System.getProperty("spring.datasource.url"),
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getProperty("SPRING_DATASOURCE_URL"),
                System.getenv("DATABASE_URL"),
                System.getProperty("DATABASE_URL")
        );
        if (url == null) {
            url = "jdbc:postgresql://localhost:5432/allinfluencer";
        }
        url = normalizePostgresJdbcUrl(url, "allinfluencer");

        if (isBlank(System.getProperty("spring.datasource.url"))) {
            System.setProperty("spring.datasource.url", url);
        }
        if (isBlank(System.getProperty("spring.datasource.driver-class-name"))) {
            System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
        }
        if (isBlank(System.getProperty("spring.datasource.username"))) {
            System.setProperty("spring.datasource.username",
                    firstNonBlank(System.getenv("DATABASE_USERNAME"), System.getProperty("DATABASE_USERNAME"), "allinfluencer"));
        }
        if (isBlank(System.getProperty("spring.datasource.password"))) {
            System.setProperty("spring.datasource.password",
                    firstNonBlank(System.getenv("DATABASE_PASSWORD"), System.getProperty("DATABASE_PASSWORD"), "allinfluencer"));
        }
    }

    private static Map<String, String> tryLoadDotenv(String filename) {
        Path start = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 6 && start != null; i++) {
            Path candidate = start.resolve(filename);
            if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                return parseDotenv(candidate);
            }
            start = start.getParent();
        }
        return Map.of();
    }

    private static Map<String, String> parseDotenv(Path file) {
        Map<String, String> out = new LinkedHashMap<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("#")) continue;
                int idx = trimmed.indexOf('=');
                if (idx <= 0) continue;
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                value = stripQuotes(value);
                out.put(key, value);
            }
        } catch (IOException ignored) {
            return Map.of();
        }
        return out;
    }

    private static String stripQuotes(String value) {
        if (value == null) return null;
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Postgres JDBC URL에서 DB명이 누락된 흔한 실수를 보정합니다.
     * 예) jdbc:postgresql://localhost:5432  -> jdbc:postgresql://localhost:5432/allinfluencer
     * 예) jdbc:postgresql://localhost:5432?ssl=true -> jdbc:postgresql://localhost:5432/allinfluencer?ssl=true
     */
    private static String normalizePostgresJdbcUrl(String url, String defaultDbName) {
        if (isBlank(url)) return url;
        String trimmed = url.trim();
        String prefix = "jdbc:postgresql://";
        if (!trimmed.startsWith(prefix)) return trimmed;

        int queryIdx = trimmed.indexOf('?');
        String base = (queryIdx >= 0) ? trimmed.substring(0, queryIdx) : trimmed;
        String query = (queryIdx >= 0) ? trimmed.substring(queryIdx) : "";

        // prefix 이후에 '/'가 없으면 host[:port]만 있는 형태로 간주하고 DB명을 붙입니다.
        String afterPrefix = base.substring(prefix.length());
        int slashIdx = afterPrefix.indexOf('/');
        if (slashIdx < 0 || slashIdx == afterPrefix.length() - 1) {
            if (base.endsWith("/")) {
                base = base + defaultDbName;
            } else {
                base = base + "/" + defaultDbName;
            }
        }

        return base + query;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!isBlank(v)) return v.trim();
        }
        return null;
    }
}


