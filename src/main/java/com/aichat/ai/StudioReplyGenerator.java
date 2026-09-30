package com.aichat.ai;

import com.aichat.domain.ChatMessage;
import com.aichat.persona.Persona;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class StudioReplyGenerator implements ReplyGenerator {

    @Override
    public String generate(Persona persona, List<ChatMessage> history, String userText) {
        String text = userText == null ? "" : userText.trim();
        String lower = text.toLowerCase(Locale.ROOT);
        int turns = (int) history.stream().filter(m -> "user".equals(m.getRole())).count();

        if (isFitness(lower)) {
            return fitnessTimetable(text);
        }
        if (isDiet(lower)) {
            return dietOutline(text);
        }
        if (isStudy(lower)) {
            return studyTimetable(text);
        }
        if (isSleep(lower)) {
            return sleepRoutine(text);
        }

        return switch (persona.id()) {
            case "atlas" -> atlas(text, lower, turns);
            case "sage" -> sage(text, lower, turns);
            case "luna" -> luna(text, lower, turns);
            case "pulse" -> pulse(text, lower, turns);
            default -> nova(text, lower, turns);
        };
    }

    private static boolean isFitness(String lower) {
        return containsAny(lower, "fitness", "workout", "gym", "exercise", "time table", "timetable")
                && containsAny(lower, "fitness", "workout", "gym", "exercise", "health", "body", "cardio", "strength");
    }

    private static boolean isDiet(String lower) {
        return containsAny(lower, "diet", "meal plan", "what to eat", "calories", "protein");
    }

    private static boolean isStudy(String lower) {
        return containsAny(lower, "study plan", "study timetable", "exam", "revision");
    }

    private static boolean isSleep(String lower) {
        return containsAny(lower, "sleep", "insomnia", "bedtime");
    }

    private String fitnessTimetable(String text) {
        boolean beginner = text.toLowerCase(Locale.ROOT).contains("beginner")
                || text.toLowerCase(Locale.ROOT).contains("normal")
                || text.toLowerCase(Locale.ROOT).contains("easy");
        String level = beginner ? "beginner-friendly" : "general";
        return heading("Weekly fitness timetable")
                + "You asked for: **" + clip(text) + "**\n"
                + "This is a " + level + " 7-day layout. Adjust times to your job/college. Not medical advice.\n\n"
                + "**Daily rhythm (pick morning *or* evening and keep it consistent)**\n"
                + "- Wake + water + 5 min stretch\n"
                + "- Workout block below\n"
                + "- Protein + carbs within 1–2 hours after training\n\n"
                + "| Day | Session (45–60 min) |\n"
                + "|---|---|\n"
                + "| Monday | Full body: squats, push-ups, rows, plank |\n"
                + "| Tuesday | Walk or easy jog 30–40 min + mobility |\n"
                + "| Wednesday | Upper body: push-ups/dips, band rows, shoulder raises |\n"
                + "| Thursday | Rest or 20 min easy walk |\n"
                + "| Friday | Lower body: lunges, glute bridges, calf raises |\n"
                + "| Saturday | Mix: 20 min cardio + core |\n"
                + "| Sunday | Full rest |\n\n"
                + "**How to do it**\n"
                + "1. Warm up 5 minutes (arm circles, bodyweight squats).\n"
                + "2. 3 rounds of the day’s moves, 8–12 reps, rest 60 seconds.\n"
                + "3. If you miss a day, skip it — do not stack two hard days.\n"
                + "4. Progress next week by adding 2 reps or 1 extra round.\n\n"
                + "If you tell me your wake time and whether you have a gym, I will pin exact clock times.";
    }

    private String dietOutline(String text) {
        return heading("Simple daily eating pattern")
                + "For: **" + clip(text) + "**\n\n"
                + "- **Breakfast:** eggs/dahi + fruit or oats, plus water\n"
                + "- **Lunch:** rice/roti + dal/chicken + vegetables\n"
                + "- **Snack:** nuts, fruit, or curd — not chips every day\n"
                + "- **Dinner:** lighter than lunch, finish 2–3 hours before sleep\n"
                + "- **Water:** sip through the day; don’t drown yourself at night\n\n"
                + "Keep 80% of meals like this. This is a pattern, not a crash diet.";
    }

    private String studyTimetable(String text) {
        return heading("Study timetable that actually fits a day")
                + "For: **" + clip(text) + "**\n\n"
                + "- **6:30–7:00** Review yesterday’s notes (no new topics)\n"
                + "- **9:00–10:30** Hardest subject while the brain is fresh\n"
                + "- **10:30–10:45** Break, walk, no reels\n"
                + "- **16:00–17:00** Practice questions / coding / past papers\n"
                + "- **21:00–21:20** Write 5 lines: what clicked, what’s still fuzzy\n\n"
                + "One main subject per morning block. Switching every 10 minutes is why it feels like you studied but remember nothing.";
    }

    private String sleepRoutine(String text) {
        return heading("A calmer night")
                + "For: **" + clip(text) + "**\n\n"
                + "- Same bedtime ± 30 minutes, including weekends\n"
                + "- Screens dim 45 minutes before bed\n"
                + "- Room cooler and darker than you think you need\n"
                + "- If you can’t sleep in 20 minutes, get up and read paper, then return\n";
    }

    private String nova(String text, String lower, int turns) {
        if (containsAny(lower, "day", "plan", "schedule") && !isFitness(lower)) {
            return heading("A focused day")
                    + "Built around: **" + clip(text) + "**\n\n"
                    + "- **Morning (90 min):** the one task that actually moves the week\n"
                    + "- **Midday (20 min):** messages and admin in one batch\n"
                    + "- **Afternoon (45 min):** finish something you can show\n"
                    + "- **Evening:** shutdown note — tomorrow’s first task only\n";
        }
        if (containsAny(lower, "how to", "how do i", "steps", "guide")) {
            return heading("A practical way to do this")
                    + "Request: **" + clip(text) + "**\n\n"
                    + "1. Write the finish line in one sentence.\n"
                    + "2. List only the next three actions, not the whole project.\n"
                    + "3. Do the smallest action today (under 30 minutes).\n"
                    + "4. Check what blocked you; change that one thing tomorrow.\n"
                    + "5. Repeat for a week before adding tools or apps.\n";
        }
        return heading("Direct take")
                + "You asked: **" + clip(text) + "**\n\n"
                + "I would treat this as a *request for a plan*, not a riddle:\n"
                + "- Decide the outcome (what “done” looks like tonight).\n"
                + "- Pick one constraint (time, money, or energy).\n"
                + "- Do the smallest version that still counts.\n\n"
                + followUp(turns, "Add your time budget (e.g. 30 minutes a day) and I will tighten this.");
    }

    private String atlas(String text, String lower, int turns) {
        if (containsAny(lower, "api", "rest", "controller")) {
            return heading("A clean chat API")
                    + "For a Java chatbot: keep HTTP boring and the domain sharp.\n\n"
                    + "```http\nPOST /api/chats\nGET  /api/chats/{id}\nPOST /api/chats/{id}/messages\n```\n\n"
                    + "- One Conversation owns many messages\n"
                    + "- Persona is a field, not a new table for v1\n"
                    + "- Return DTOs, never JPA entities\n";
        }
        if (containsAny(lower, "jpa", "jdbc", "database")) {
            return heading("JPA vs JDBC")
                    + "**JDBC:** you write SQL and map rows. More control, more glue.\n"
                    + "**JPA:** you map objects; Hibernate writes most SQL. Faster CRUD, easier lazy-load bugs.\n\n"
                    + "For chats and users, JPA is the default. Use JDBC when a report query gets ugly.";
        }
        return heading("Engineer’s take")
                + "On: **" + clip(text) + "**\n\n"
                + "Split it as: validate input → change domain state → return a DTO.\n"
                + "Keep slow I/O (HTTP, Wikipedia, models) behind a small interface so you can test without the network.\n"
                + followUp(turns, "Paste the class and I will review it like a PR.");
    }

    private String sage(String text, String lower, int turns) {
        if (containsAny(lower, "resume", "bullet", "linkedin")) {
            return heading("Resume-ready lines")
                    + "- Built a Java Spring Boot chatbot with saved chats, personas, and optional live Wikipedia answers.\n"
                    + "- Designed a chat UI with voice, export, and conversation management.\n"
                    + "- Separated how-to planners from encyclopedia lookup so “make me a timetable” does not return a random wiki page.\n";
        }
        return heading("Career framing")
                + "You said: **" + clip(text) + "**\n\n"
                + "Tell it as: who it helps, what was hard, what you chose, what you would do next.\n"
                + followUp(turns, "Name the role and I will turn this into interview answers.");
    }

    private String luna(String text, String lower, int turns) {
        if (containsAny(lower, "name", "brand", "headline")) {
            return heading("Name board")
                    + "1. **Lumen Thread** — calm studio\n"
                    + "2. **North Reply** — confident copilot\n"
                    + "3. **Glass Hearth** — warm, not sci-fi cold\n\n"
                    + "**Pick:** Glass Hearth. Headline: *A quieter place to think out loud.*";
        }
        return heading("Creative pass")
                + "For: **" + clip(text) + "**\n\n"
                + "Give me the audience (student, recruiter, friend) and the format (caption, story, landing line) and I will write three options plus a recommended one.";
    }

    private String pulse(String text, String lower, int turns) {
        if (isFitness(lower)) {
            return fitnessTimetable(text);
        }
        return heading("One focused hour")
                + "For: **" + clip(text) + "**\n\n"
                + "- 2 min: close extra tabs, water, write the one outcome\n"
                + "- 25 min: only that outcome\n"
                + "- 5 min: stand up\n"
                + "- 25 min: finish something you can screenshot\n";
    }

    private static boolean containsAny(String lower, String... parts) {
        for (String part : parts) {
            if (lower.contains(part)) {
                return true;
            }
        }
        return false;
    }

    private static String heading(String title) {
        return "### " + title + "\n\n";
    }

    private static String followUp(int turns, String question) {
        return turns > 2 ? "We already have a thread. " + question : question;
    }

    private static String clip(String text) {
        String cleaned = text.replace('\n', ' ').trim();
        if (cleaned.length() > 160) {
            return cleaned.substring(0, 157) + "...";
        }
        return cleaned.isEmpty() ? "your request" : cleaned;
    }
}
