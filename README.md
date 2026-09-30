# Nova Studio — AI-Powered Chatbot

Independent Java project (not part of the multiple-disease-prediction app).

## Deploy

Vercel cannot host this Java server. Use Render:

1. After the code is on GitHub, open
   [https://render.com/deploy?repo=https://github.com/ankitbhowmik59134-ux/ai-powered-chatbot](https://render.com/deploy?repo=https://github.com/ankitbhowmik59134-ux/ai-powered-chatbot)
2. Blueprint name: `ai-powered-chatbot`
3. Branch: `main`, path: `render.yaml`
4. Wait for the Docker build, then copy the `onrender.com` URL

## Run as an app (Windows)

In File Explorer open:

`C:\Users\ankit\IdeaProjects\ai-powered-chatbot`

Double-click **`NovaStudio.bat`**.

Or in a terminal:

```bat
cd C:\Users\ankit\IdeaProjects\ai-powered-chatbot
mvnw.cmd javafx:run
```

A **Nova Studio** window should open. That is the app.

## Features

- Five personas: Nova (copilot), Atlas (code), Sage (career), Luna (creative), Pulse (focus)
- Saved conversations
- Rename, delete, switch persona
- Suggested starters
- Voice input
- Export Markdown
- Works without an API key

## Browser (optional)

```bat
mvnw.cmd spring-boot:run
```

Then http://localhost:8080 — only if you still want the website version.

It cannot copy Google. Google Search is a paid/private API. This app now does the next honest thing:

1. **Wikipedia (on by default)** — factual questions pull a live encyclopedia extract, not a canned template. Try: `What is Java?` or `capital of India`.
2. **A real LLM (optional, like ChatGPT)** — free Groq key. Then answers are generated, not pre-written.

## Real AI (Groq, free)

1. Create a key at [https://console.groq.com/keys](https://console.groq.com/keys)
2. In PowerShell, then start the app:

```bat
cd C:\Users\ankit\IdeaProjects\ai-powered-chatbot
set CHAT_API_KEY=paste_your_groq_key_here
mvnw.cmd spring-boot:run
```

The header should say **Live model connected**.

## Wikipedia facts (already enabled)

No key. Restart the server after this update, then ask a real-world question.

## Optional live model

```bat
set CHAT_API_KEY=your_key
set CHAT_API_URL=https://api.groq.com/openai/v1/chat/completions
set CHAT_API_MODEL=llama-3.1-8b-instant
```
