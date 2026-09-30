package com.aichat.ai;

import com.aichat.domain.ChatMessage;
import com.aichat.persona.Persona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LiveModelClient {

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public LiveModelClient(
            @Value("${chat.api.url}") String apiUrl,
            @Value("${chat.api.key}") String apiKey,
            @Value("${chat.api.model}") String model
    ) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.restClient = RestClient.create();
    }

    public boolean enabled() {
        return !apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public String generate(Persona persona, List<ChatMessage> history, String userText, String research) {
        List<Map<String, String>> messages = new ArrayList<>();
        String system = persona.systemPrompt()
                + " Answer from your knowledge. If research notes are provided, prefer them for facts."
                + " Never invent citations.";
        if (research != null && !research.isBlank()) {
            system += "\n\nResearch notes:\n" + research;
        }
        messages.add(Map.of("role", "system", "content", system));
        for (ChatMessage message : history) {
            messages.add(Map.of("role", message.getRole(), "content", message.getContent()));
        }
        messages.add(Map.of("role", "user", "content", userText));

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.7,
                "messages", messages
        );

        Map<String, Object> response = restClient.post()
                .uri(apiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Empty model response");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return String.valueOf(message.get("content"));
    }
}
