package com.aichat.ai;

import com.aichat.persona.PersonaCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudioReplyGeneratorTest {

    private final StudioReplyGenerator generator = new StudioReplyGenerator();
    private final PersonaCatalog catalog = new PersonaCatalog();

    @Test
    void atlasTalksAboutApis() {
        String reply = generator.generate(catalog.require("atlas"), List.of(), "How do I design a REST API for chats?");
        assertThat(reply).contains("POST /api/chats");
        assertThat(reply).contains("Conversation");
    }

    @Test
    void fitnessRequestGetsAWeeklyTimetable() {
        String reply = generator.generate(catalog.require("nova"), List.of(), "give me a normal fitness time table");
        assertThat(reply).containsIgnoringCase("Monday");
        assertThat(reply).containsIgnoringCase("squat");
        assertThat(reply).doesNotContain("feedback loop");
    }
}
