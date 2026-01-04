package com.allinfluencer.backend.users.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UsersReadMapper {
    List<UserRow> selectUsers(
            @Param("role") String role,
            @Param("status") String status,
            @Param("search") String search,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countUsers(
            @Param("role") String role,
            @Param("status") String status,
            @Param("search") String search
    );

    UserRow selectUserById(@Param("id") String id);

    record UserRow(
            String id,
            String email,
            String displayName,
            String avatar,
            String role,
            String status,
            String bio,
            String website,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastLoginAt
    ) {}
}

