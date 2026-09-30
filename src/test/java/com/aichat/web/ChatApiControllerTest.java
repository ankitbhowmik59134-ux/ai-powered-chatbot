package com.aichat.web;

import com.aichat.service.ChatStudioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatStudioService studio;

    @Test
    void studioHasFivePersonas() throws Exception {
        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        assertThat(studio.liveMode()).isFalse();
    }

    @Test
    void canStartAChatAndGetAReply() throws Exception {
        String body = mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"personaId\":\"luna\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personaId").value("luna"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");
        mockMvc.perform(post("/api/chats/" + id + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Name my AI chatbot product\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    @Test
    void turnCreatesChatAndRepliesInOneCall() throws Exception {
        mockMvc.perform(post("/api/turn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"personaId\":\"atlas\",\"content\":\"How do I design a REST API for chats?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chat.personaId").value("atlas"))
                .andExpect(jsonPath("$.assistant.content").isNotEmpty());
    }
}
