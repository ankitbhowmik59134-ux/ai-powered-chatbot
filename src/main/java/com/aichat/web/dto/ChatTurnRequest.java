package com.aichat.web.dto;

public record ChatTurnRequest(Long conversationId, String personaId, String content) {
}
