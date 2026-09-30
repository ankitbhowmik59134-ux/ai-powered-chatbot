package com.aichat.desktop;

import com.aichat.AiChatbotApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Desktop window for Nova Studio. Starts the chatbot server if needed, then
 * shows the UI in an application frame instead of a browser tab.
 */
public class NovaStudioApp extends Application {

    private static final int PORT = 8080;
    private static final String APP_URL = "http://127.0.0.1:" + PORT + "/";
    private ConfigurableApplicationContext spring;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Nova Studio");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);

        WebView view = new WebView();
        view.setContextMenuEnabled(false);
        Scene scene = new Scene(view, 1280, 800, Color.web("#0c1014"));
        stage.setScene(scene);
        stage.show();
        view.getEngine().loadContent(splashHtml(), "text/html");

        Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "nova-studio-boot");
            thread.setDaemon(true);
            return thread;
        }).execute(() -> {
            try {
                ensureServer();
                waitUntilUp();
                Platform.runLater(() -> {
                    view.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
                        if (state == Worker.State.SUCCEEDED) {
                            stage.setTitle("Nova Studio — AI Chatbot");
                        }
                    });
                    view.getEngine().load(APP_URL);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> view.getEngine().loadContent(
                        errorHtml(ex.getMessage()), "text/html"));
            }
        });
    }

    @Override
    public void stop() {
        if (spring != null) {
            spring.close();
        }
        Platform.exit();
    }

    private void ensureServer() {
        if (isUp()) {
            return;
        }
        spring = SpringApplication.run(AiChatbotApplication.class);
    }

    private static boolean isUp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", PORT), 400);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void waitUntilUp() throws InterruptedException {
        for (int i = 0; i < 80; i++) {
            if (isUp()) {
                return;
            }
            Thread.sleep(250);
        }
        throw new IllegalStateException("Chatbot server did not start on port " + PORT);
    }

    private static String splashHtml() {
        return """
                <html><body style="margin:0;background:#0c1014;color:#f3eee4;font-family:Segoe UI,sans-serif;display:grid;place-items:center;height:100vh;">
                <div style="text-align:center"><div style="width:56px;height:56px;border-radius:16px;background:linear-gradient(135deg,#e7c588,#8a5a2b);margin:0 auto 16px;"></div>
                <h1>Nova Studio</h1><p style="color:#9aa7b4">Opening your chatbot app…</p></div></body></html>
                """;
    }

    private static String errorHtml(String message) {
        return "<html><body style='background:#0c1014;color:#ffb4a8;font-family:Segoe UI;padding:40px'><h2>Could not start Nova Studio</h2><p>"
                + message + "</p></body></html>";
    }
}
