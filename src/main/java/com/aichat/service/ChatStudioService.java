package com.aichat.service;

import com.aichat.ai.LiveModelClient;
import com.aichat.ai.StudioReplyGenerator;
import com.aichat.ai.WikipediaClient;
import com.aichat.domain.ChatMessage;
import com.aichat.domain.Conversation;
import com.aichat.persona.Persona;
import com.aichat.persona.PersonaCatalog;
import com.aichat.repo.ChatMessageRepository;
import com.aichat.repo.ConversationRepository;
import com.aichat.web.dto.ChatSummary;
import com.aichat.web.dto.ChatTurnResponse;
import com.aichat.web.dto.MessageView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatStudioService {

    private final ConversationRepository conversations;
    private final ChatMessageRepository messages;
    private final PersonaCatalog personas;
    private final StudioReplyGenerator studioEngine;
    private final LiveModelClient liveModel;
    private final WikipediaClient wikipedia;

    public ChatStudioService(
            ConversationRepository conversations,
            ChatMessageRepository messages,
            PersonaCatalog personas,
            StudioReplyGenerator studioEngine,
            LiveModelClient liveModel,
            WikipediaClient wikipedia
    ) {
        this.conversations = conversations;
        this.messages = messages;
        this.personas = personas;
        this.studioEngine = studioEngine;
        this.liveModel = liveModel;
        this.wikipedia = wikipedia;
    }

    public List<Persona> personas() {
        return personas.all();
    }

    public boolean liveMode() {
        return liveModel.enabled();
    }

    public List<ChatSummary> listConversations() {
        return conversations.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public ChatSummary create(String personaId) {
        Persona persona = personas.require(personaId);
        Conversation conversation = new Conversation();
        conversation.setPersonaId(persona.id());
        conversation.setTitle("Chat with " + persona.name());
        return toSummary(conversations.save(conversation));
    }

    public Conversation require(Long id) {
        return conversations.findById(id).orElseThrow(() -> new IllegalArgumentException("Chat not found"));
    }

    public List<MessageView> history(Long conversationId) {
        return messages.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public Conversation rename(Long id, String title) {
        Conversation conversation = require(id);
        if (title != null && !title.isBlank()) {
            conversation.setTitle(title.trim());
            conversation.touch();
        }
        return conversations.save(conversation);
    }

    @Transactional
    public Conversation switchPersona(Long id, String personaId) {
        Conversation conversation = require(id);
        conversation.setPersonaId(personas.require(personaId).id());
        conversation.touch();
        return conversations.save(conversation);
    }

    @Transactional
    public void delete(Long id) {
        conversations.deleteById(id);
    }

    @Transactional
    public MessageView reply(Long conversationId, String userText) {
        if (userText == null || userText.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        Conversation conversation = require(conversationId);
        Persona persona = personas.require(conversation.getPersonaId());
        List<ChatMessage> prior = messages.findByConversationIdOrderByCreatedAtAsc(conversationId);

        ChatMessage user = messages.save(new ChatMessage(conversation, "user", userText.trim()));
        String research = wikipedia.lookup(userText.trim()).orElse(null);

        String answer;
        String source;
        if (liveModel.enabled()) {
            try {
                answer = liveModel.generate(persona, prior, userText.trim(), research);
                source = "live";
            } catch (Exception ex) {
                answer = research != null ? research : studioEngine.generate(persona, prior, userText.trim());
                source = research != null ? "wikipedia" : "studio";
            }
        } else if (research != null) {
            answer = research;
            source = "wikipedia";
        } else {
            answer = studioEngine.generate(persona, prior, userText.trim());
            source = "studio";
        }

        ChatMessage assistant = messages.save(new ChatMessage(conversation, "assistant", answer));
        if (prior.isEmpty()) {
            conversation.setTitle(titleFrom(userText));
        }
        conversation.touch();
        conversations.save(conversation);
        return new MessageView(assistant.getId(), assistant.getRole(), assistant.getContent(), assistant.getCreatedAt().toString(), source);
    }

    @Transactional
    public ChatTurnResponse turn(Long conversationId, String personaId, String content) {
        ChatSummary chat = conversationId == null ? create(personaId) : toSummary(require(conversationId));
        MessageView assistant = reply(chat.id(), content);
        return new ChatTurnResponse(toSummary(require(chat.id())), assistant);
    }

    public String exportMarkdown(Long conversationId) {
        Conversation conversation = require(conversationId);
        StringBuilder out = new StringBuilder("# ").append(conversation.getTitle()).append("\n\n");
        for (ChatMessage message : messages.findByConversationIdOrderByCreatedAtAsc(conversationId)) {
            out.append("**").append(message.getRole()).append(":**\n")
                    .append(message.getContent()).append("\n\n");
        }
        return out.toString();
    }

    private ChatSummary toSummary(Conversation conversation) {
        Persona persona = personas.require(conversation.getPersonaId());
        return new ChatSummary(
                conversation.getId(),
                conversation.getTitle(),
                persona.id(),
                persona.name(),
                conversation.getUpdatedAt().toString()
        );
    }

    private MessageView toView(ChatMessage message) {
        return new MessageView(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt().toString(),
                null
        );
    }

    private String titleFrom(String userText) {
        String compact = userText.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 42) {
            return compact;
        }
        return compact.substring(0, 39).trim() + "…";
    }
}
