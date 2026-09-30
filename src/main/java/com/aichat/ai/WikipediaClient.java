package com.aichat.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pulls live encyclopedia facts from Wikipedia so answers are not canned templates.
 */
@Component
public class WikipediaClient {

    private final RestClient restClient;
    private final boolean enabled;

    public WikipediaClient(@Value("${chat.wiki.enabled:true}") boolean enabled) {
        this.enabled = enabled;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "NovaStudio/1.0 (educational Java chatbot)")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Optional<String> lookup(String userText) {
        if (!enabled || userText == null || userText.trim().length() < 3 || !looksLikeFactQuestion(userText)) {
            return Optional.empty();
        }
        try {
            String query = clean(userText);
            Map<?, ?> search = restClient.get()
                    .uri("https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srlimit=1&srsearch={q}", query)
                    .retrieve()
                    .body(Map.class);
            if (search == null || !(search.get("query") instanceof Map<?, ?> queryMap)
                    || !(queryMap.get("search") instanceof List<?> hits) || hits.isEmpty()
                    || !(hits.get(0) instanceof Map<?, ?> hit)) {
                return Optional.empty();
            }
            String title = String.valueOf(hit.get("title"));
            Map<?, ?> summary = restClient.get()
                    .uri("https://en.wikipedia.org/api/rest_v1/page/summary/{title}", title)
                    .retrieve()
                    .body(Map.class);
            if (summary == null) {
                return Optional.empty();
            }
            Object extractObj = summary.get("extract");
            String extract = extractObj == null ? "" : String.valueOf(extractObj).trim();
            if (extract.length() < 40) {
                return Optional.empty();
            }
            String link = "https://en.wikipedia.org/wiki/" + title.replace(' ', '_');
            Object desktop = summary.get("content_urls");
            if (desktop instanceof Map<?, ?> urls && urls.get("desktop") instanceof Map<?, ?> desk
                    && desk.get("page") != null) {
                link = String.valueOf(desk.get("page"));
            }
            return Optional.of("### " + title + "\n\n" + extract + "\n\nSource: " + link);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    static boolean looksLikeFactQuestion(String userText) {
        String lower = userText.toLowerCase();
        boolean howTo = lower.matches(".*\\b(give me|create|make me|write me|help me|suggest|timetable|time table|workout|fitness plan|schedule for)\\b.*");
        boolean fact = lower.matches(".*\\b(what is|what's|who is|who's|where is|where's|when was|when is|capital of|define|meaning of|tell me about)\\b.*")
                || (lower.contains("?") && lower.split("\\s+").length <= 12 && !howTo);
        return fact && !howTo;
    }

    private static String clean(String userText) {
        return userText.replaceAll("[?!.]+", " ").replaceAll("\\s+", " ").trim();
    }
}
