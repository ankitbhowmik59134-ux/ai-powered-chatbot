package com.aichat.ai;

import com.aichat.domain.ChatMessage;
import com.aichat.persona.Persona;

import java.util.List;

public interface ReplyGenerator {
    String generate(Persona persona, List<ChatMessage> history, String userText);
}
