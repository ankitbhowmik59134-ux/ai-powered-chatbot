package com.aichat.web.dto;

public record MessageView(Long id, String role, String content, String createdAt, String source) {
}
