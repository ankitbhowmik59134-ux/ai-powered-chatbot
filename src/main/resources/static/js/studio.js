const state = {
    chats: [],
    activeId: null,
    personaId: "nova",
    sending: false
};

const $ = (id) => document.getElementById(id);

function showEmpty() {
    const box = $("transcript");
    box.innerHTML = "";
    box.appendChild($("emptyTemplate").content.cloneNode(true));
    fillStarters();
}

function fillStarters() {
    const starters = $("starters");
    if (!starters) {
        return;
    }
    const persona = (window.NOVA.personas || []).find((item) => item.id === state.personaId);
    starters.innerHTML = "";
    (persona?.starters || []).forEach((text) => {
        const chip = document.createElement("button");
        chip.type = "button";
        chip.textContent = text;
        chip.onclick = () => {
            $("input").value = text;
            sendMessage();
        };
        starters.appendChild(chip);
    });
}

function renderPersonas() {
    const row = $("personaRow");
    row.innerHTML = "";
    (window.NOVA.personas || []).forEach((persona) => {
        const button = document.createElement("button");
        button.className = "persona-chip" + (persona.id === state.personaId ? " active" : "");
        button.type = "button";
        button.textContent = persona.name + " · " + persona.title;
        button.onclick = () => selectPersona(persona.id);
        row.appendChild(button);
    });
    fillStarters();
}

async function loadChats() {
    const chats = await (await fetch("/api/chats")).json();
    state.chats = chats;
    const nav = $("history");
    nav.innerHTML = "";
    chats.forEach((chat) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = chat.id === state.activeId ? "active" : "";
        button.innerHTML = `<strong>${escapeHtml(chat.title)}</strong><br><small>${escapeHtml(chat.personaName)}</small>`;
        button.onclick = () => openChat(chat.id);
        nav.appendChild(button);
    });
}

function setHeader(chat) {
    if (!chat) {
        $("chatTitle").textContent = "Pick a mind, then talk";
        $("chatMeta").textContent = "Fast replies, saved threads, voice, and export.";
        return;
    }
    $("chatTitle").textContent = chat.title;
    $("chatMeta").textContent = chat.personaName + " · live in this window";
}

async function openChat(id) {
    state.activeId = id;
    const chat = state.chats.find((item) => item.id === id);
    if (chat) {
        state.personaId = chat.personaId;
        setHeader(chat);
    }
    renderPersonas();
    const messages = await (await fetch("/api/chats/" + id)).json();
    if (!messages.length) {
        showEmpty();
    } else {
        $("transcript").innerHTML = "";
        messages.forEach((message) => appendBubble(message.role, message.content, message.source));
    }
    await loadChats();
}

function appendBubble(role, content, source) {
    const empty = $("emptyState");
    if (empty) {
        empty.remove();
    }
    const article = document.createElement("article");
    article.className = "bubble " + role;
    if (role === "typing") {
        article.innerHTML = '<span class="dot"></span><span class="dot"></span><span class="dot"></span>';
        $("transcript").appendChild(article);
        $("transcript").scrollTop = $("transcript").scrollHeight;
        return article;
    }
    const who = role === "user" ? "You" : (source === "wikipedia" ? "Wikipedia" : source === "live" ? "Live model" : "Nova Studio");
    article.innerHTML = `
        <div class="who">
            <span>${who}</span>
            <button class="copy" type="button">Copy</button>
        </div>
        <div>${format(content)}</div>`;
    article.querySelector(".copy").onclick = () => navigator.clipboard.writeText(content);
    $("transcript").appendChild(article);
    $("transcript").scrollTop = $("transcript").scrollHeight;
    return article;
}

async function selectPersona(id) {
    state.personaId = id;
    renderPersonas();
    if (!state.activeId) {
        return;
    }
    const chat = await (await fetch("/api/chats/" + state.activeId, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ personaId: id })
    })).json();
    setHeader(chat);
    loadChats();
}

async function sendMessage() {
    const content = $("input").value.trim();
    if (!content || state.sending) {
        return;
    }
    state.sending = true;
    $("sendBtn").disabled = true;
    $("status").textContent = "Thinking…";
    $("input").value = "";
    appendBubble("user", content);
    const typing = appendBubble("typing", "");
    try {
        const response = await fetch("/api/turn", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                conversationId: state.activeId,
                personaId: state.personaId,
                content
            })
        });
        const data = await response.json();
        typing.remove();
        if (!response.ok) {
            appendBubble("assistant", data.error || "Could not reply. Try again.");
        } else {
            state.activeId = data.chat.id;
            state.personaId = data.chat.personaId;
            setHeader(data.chat);
            appendBubble("assistant", data.assistant.content, data.assistant.source);
            renderPersonas();
            loadChats();
        }
    } catch (error) {
        typing.remove();
        appendBubble("assistant", "Network blip. Send that again.");
    } finally {
        state.sending = false;
        $("sendBtn").disabled = false;
        $("status").textContent = "Ready";
        $("input").focus();
    }
}

$("composer").addEventListener("submit", (event) => {
    event.preventDefault();
    sendMessage();
});
$("input").addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});
$("newChat").onclick = () => {
    state.activeId = null;
    setHeader(null);
    showEmpty();
    renderPersonas();
};
$("renameBtn").onclick = async () => {
    if (!state.activeId) return;
    const title = prompt("Rename this chat");
    if (!title) return;
    const chat = await (await fetch("/api/chats/" + state.activeId, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title })
    })).json();
    setHeader(chat);
    loadChats();
};
$("deleteBtn").onclick = async () => {
    if (!state.activeId) return;
    if (!confirm("Delete this conversation?")) return;
    await fetch("/api/chats/" + state.activeId, { method: "DELETE" });
    state.activeId = null;
    setHeader(null);
    showEmpty();
    loadChats();
};
$("exportBtn").onclick = () => {
    if (!state.activeId) return;
    window.location = "/api/chats/" + state.activeId + "/export";
};
$("voiceBtn").onclick = () => {
    const Speech = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!Speech) {
        $("status").textContent = "Voice needs Chrome or Edge";
        return;
    }
    const rec = new Speech();
    rec.lang = "en-US";
    rec.onresult = (event) => { $("input").value = event.results[0][0].transcript; };
    rec.start();
    $("status").textContent = "Listening…";
    rec.onend = () => { $("status").textContent = "Ready"; };
};
$("menuBtn").onclick = () => $("history").closest(".rail").classList.toggle("open");

function format(text) {
    return escapeHtml(text)
        .replace(/^### (.*)$/gm, "<strong>$1</strong>")
        .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
        .replace(/\*(.*?)\*/g, "<em>$1</em>")
        .replace(/```([\s\S]*?)```/g, "<pre>$1</pre>");
}
function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

renderPersonas();
showEmpty();
loadChats();
document.addEventListener("wheel", (event) => {
    if (event.target.closest("textarea, .history, .rail")) {
        return;
    }
    const box = $("transcript");
    if (!box) {
        return;
    }
    box.scrollTop += event.deltaY;
}, { passive: true });
