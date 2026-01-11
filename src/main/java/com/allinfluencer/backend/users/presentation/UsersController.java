package com.allinfluencer.backend.users.presentation;

import com.allinfluencer.backend.auth.infrastructure.jpa.UserEntity;
import com.allinfluencer.backend.auth.infrastructure.jpa.UserRepository;
import com.allinfluencer.backend.mypage.domain.DomainException;
import com.allinfluencer.backend.users.infrastructure.mybatis.UsersReadMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * NestJS UsersController 호환:
 * - POST   /users
 * - GET    /users (public)
 * - GET    /users/:id
 * - PATCH  /users/:id
 * - DELETE /users/:id
 *
 * TODO: CQRS 구현(조회=MyBatis, 쓰기=JPA)로 서비스 연결.
 */
@RestController
@RequestMapping("/users")
public class UsersController {
    private final UsersReadMapper readMapper;
    private final UserRepository userRepository;

    public UsersController(
            UsersReadMapper readMapper,
            UserRepository userRepository
    ) {
        this.readMapper = readMapper;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody CreateUserRequest body) {
        if (body.email() == null || body.email().isBlank()) {
            throw new DomainException("INVALID_ARGUMENT", "email is required");
        }
        if (userRepository.findByEmail(body.email()).isPresent()) {
            throw new DomainException("INVALID_ARGUMENT", "이미 존재하는 사용자입니다.");
        }

        String role = body.role() == null || body.role().isBlank() ? "INFLUENCER" : body.role().trim().toUpperCase();
        UserEntity user = new UserEntity(
                java.util.UUID.randomUUID().toString(),
                body.email(),
                null,
                body.displayName(),
                role,
                "ACTIVE"
        );
        userRepository.save(user);

        return Map.of("data", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "avatar", user.getAvatar(),
                "role", user.getRole(),
                "status", user.getStatus()
        ));
    }

    @GetMapping
    public Map<String, Object> findAll(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        int safePage = Math.max(1, page);
        int safeLimit = Math.min(100, Math.max(1, limit));
        int offset = (safePage - 1) * safeLimit;

        var items = readMapper.selectUsers(
                role == null ? null : role.trim().toUpperCase(),
                status == null ? null : status.trim().toUpperCase(),
                (search == null || search.isBlank()) ? null : search.trim(),
                offset,
                safeLimit
        );
        long total = readMapper.countUsers(
                role == null ? null : role.trim().toUpperCase(),
                status == null ? null : status.trim().toUpperCase(),
                (search == null || search.isBlank()) ? null : search.trim()
        );

        long totalPages = (long) Math.ceil(total / (double) safeLimit);
        boolean hasPrev = safePage > 1;
        boolean hasNext = safePage < totalPages;

        var data = items.stream().map(u -> Map.<String, Object>ofEntries(
                Map.entry("id", u.id()),
                Map.entry("email", u.email()),
                Map.entry("displayName", u.displayName()),
                Map.entry("avatar", u.avatar()),
                Map.entry("role", u.role()),
                Map.entry("status", u.status()),
                Map.entry("bio", u.bio()),
                Map.entry("website", u.website()),
                Map.entry("createdAt", u.createdAt() == null ? null : u.createdAt().toString()),
                Map.entry("updatedAt", u.updatedAt() == null ? null : u.updatedAt().toString()),
                Map.entry("lastLoginAt", u.lastLoginAt() == null ? null : u.lastLoginAt().toString())
        )).toList();

        return Map.of("data", Map.of(
                "data", data,
                "meta", Map.of(
                        "page", safePage,
                        "limit", safeLimit,
                        "total", total,
                        "totalPages", totalPages,
                        "hasNext", hasNext,
                        "hasPrev", hasPrev
                )
        ));
    }

    @GetMapping("/{id}")
    public Map<String, Object> findOne(@PathVariable String id) {
        var u = readMapper.selectUserById(id);
        if (u == null) {
            throw new DomainException("NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }

        return Map.of("data", Map.<String, Object>ofEntries(
                Map.entry("id", u.id()),
                Map.entry("email", u.email()),
                Map.entry("displayName", u.displayName()),
                Map.entry("avatar", u.avatar()),
                Map.entry("role", u.role()),
                Map.entry("status", u.status()),
                Map.entry("bio", u.bio()),
                Map.entry("website", u.website()),
                Map.entry("createdAt", u.createdAt() == null ? null : u.createdAt().toString()),
                Map.entry("updatedAt", u.updatedAt() == null ? null : u.updatedAt().toString()),
                Map.entry("lastLoginAt", u.lastLoginAt() == null ? null : u.lastLoginAt().toString())
        ));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest body) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new DomainException("NOT_FOUND", "사용자를 찾을 수 없습니다."));

        if (body.displayName() != null) user.setDisplayName(body.displayName());
        if (body.status() != null && !body.status().isBlank()) user.setStatus(body.status().trim().toUpperCase());
        // avatar/bio/website 등은 현재 UserEntity에 setter가 없어서 추후 확장
        userRepository.save(user);

        return findOne(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            throw new DomainException("NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(id);
    }

    public record CreateUserRequest(
            @Email @NotBlank String email,
            String displayName,
            String role
    ) {}

    public record UpdateUserRequest(
            String displayName,
            String avatar,
            String status
    ) {}
}

