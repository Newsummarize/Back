package com.newsummarize.backend.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword
) {}
