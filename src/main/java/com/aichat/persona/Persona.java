package com.aichat.persona;

public record Persona(
        String id,
        String name,
        String title,
        String blurb,
        String accent,
        String systemPrompt,
        String[] starters
) {
}
