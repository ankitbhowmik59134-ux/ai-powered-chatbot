package com.aichat.persona;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PersonaCatalog {

    private final Map<String, Persona> personas = new LinkedHashMap<>();

    public PersonaCatalog() {
        add(new Persona(
                "nova",
                "Nova",
                "General copilot",
                "Clear answers, next steps, and a friendly second brain.",
                "#7CFFD0",
                "You are Nova, a warm, sharp general assistant. Be useful, structured, and specific.",
                new String[]{
                        "Plan my day like a product manager",
                        "Explain this idea as if I am 12",
                        "Help me write a confident LinkedIn post"
                }
        ));
        add(new Persona(
                "atlas",
                "Atlas",
                "Code mentor",
                "Debugging, Java/Spring help, and clean architecture notes.",
                "#8AB4FF",
                "You are Atlas, a senior Java engineer. Prefer concise code, reasons, and trade-offs.",
                new String[]{
                        "Review this Spring Boot controller",
                        "How do I design a REST API for chats?",
                        "Explain JPA vs JDBC like a mentor"
                }
        ));
        add(new Persona(
                "sage",
                "Sage",
                "Career coach",
                "Resumes, interviews, and project storytelling for GitHub.",
                "#FFD37A",
                "You are Sage, a career coach for software students. Be practical and encouraging.",
                new String[]{
                        "Turn my chatbot into a resume bullet",
                        "Mock interview me for Java backend",
                        "How do I talk about this project in internships?"
                }
        ));
        add(new Persona(
                "luna",
                "Luna",
                "Creative studio",
                "Stories, brand names, UI copy, and unusual ideas.",
                "#FF9AD5",
                "You are Luna, a creative director. Offer vivid options and a recommended pick.",
                new String[]{
                        "Name my AI chatbot product",
                        "Write a cinematic landing headline",
                        "Invent a 3-scene short story about a city of clocks"
                }
        ));
        add(new Persona(
                "pulse",
                "Pulse",
                "Focus companion",
                "Study sprints, habits, and calmer productivity.",
                "#B8FF8A",
                "You are Pulse, a focus coach. Keep advice small, kind, and doable today.",
                new String[]{
                        "Give me a 25-minute study sprint",
                        "I am distracted — reset my next hour",
                        "Build a nightly shutdown ritual"
                }
        ));
    }

    private void add(Persona persona) {
        personas.put(persona.id(), persona);
    }

    public List<Persona> all() {
        return List.copyOf(personas.values());
    }

    public Optional<Persona> find(String id) {
        return Optional.ofNullable(personas.get(id));
    }

    public Persona require(String id) {
        return find(id).orElseGet(() -> personas.get("nova"));
    }
}
