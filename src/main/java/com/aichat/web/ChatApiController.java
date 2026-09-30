package com.aichat.web;

import com.aichat.service.ChatStudioService;
import com.aichat.web.dto.ChatSummary;
import com.aichat.web.dto.ChatTurnRequest;
import com.aichat.web.dto.CreateChatRequest;
import com.aichat.web.dto.MessageView;
import com.aichat.web.dto.SendMessageRequest;
import com.aichat.web.dto.UpdateChatRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatApiController {

    private final ChatStudioService studio;

    public ChatApiController(ChatStudioService studio) {
        this.studio = studio;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("liveMode", studio.liveMode());
        body.put("engine", studio.liveMode() ? "live-model" : "wikipedia+studio");
        body.put("personas", studio.personas().size());
        return body;
    }

    @GetMapping("/personas")
    public Object personas() {
        return studio.personas();
    }

    @GetMapping("/chats")
    public List<ChatSummary> chats() {
        return studio.listConversations();
    }

    @PostMapping("/chats")
    public ChatSummary create(@RequestBody(required = false) CreateChatRequest request) {
        String personaId = request == null ? "nova" : request.personaId();
        return studio.create(personaId);
    }

    @GetMapping("/chats/{id}")
    public List<MessageView> history(@PathVariable Long id) {
        studio.require(id);
        return studio.history(id);
    }

    @PostMapping("/turn")
    public ResponseEntity<?> turn(@RequestBody ChatTurnRequest request) {
        try {
            String content = request == null ? null : request.content();
            String personaId = request == null || request.personaId() == null ? "nova" : request.personaId();
            Long conversationId = request == null ? null : request.conversationId();
            return ResponseEntity.ok(studio.turn(conversationId, personaId, content));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/chats/{id}/messages")
    public ResponseEntity<?> send(@PathVariable Long id, @RequestBody SendMessageRequest request) {
        try {
            return ResponseEntity.ok(studio.reply(id, request == null ? null : request.content()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PatchMapping("/chats/{id}")
    public ChatSummary update(@PathVariable Long id, @RequestBody UpdateChatRequest request) {
        if (request != null && request.personaId() != null) {
            studio.switchPersona(id, request.personaId());
        }
        if (request != null && request.title() != null) {
            studio.rename(id, request.title());
        }
        return studio.listConversations().stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @DeleteMapping("/chats/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studio.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chats/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id) {
        byte[] data = studio.exportMarkdown(id).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chat-" + id + ".md\"")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(data);
    }
}
