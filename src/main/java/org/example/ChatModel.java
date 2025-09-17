package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ChatModel {
    private final String apiKey;
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    private final Random random;

    public ChatModel() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
        this.random = new Random();
    }

    public String getChatResponse() {
        List<String> questions = List.of(
            "How are you feeling today?",
            "What's your mood right now?",
            "Tell me something interesting",
            "How's your day going?",
            "What are you thinking about?"
        );

        String question = questions.get(random.nextInt(questions.size()));
        return sendChatRequest(question);
    }

    private String sendChatRequest(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            return getFallbackResponse();
        }

        try {
            HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");
            requestBody.addProperty("max_tokens", 100);

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", message);
            messages.add(userMessage);
            requestBody.add("messages", messages);

            request.setEntity(new StringEntity(gson.toJson(requestBody), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                if (responseJson.has("choices")) {
                    JsonArray choices = responseJson.getAsJsonArray("choices");
                    if (choices.size() > 0) {
                        JsonObject choice = choices.get(0).getAsJsonObject();
                        return choice.getAsJsonObject("message").get("content").getAsString().trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Chat API error: " + e.getMessage());
        }

        return getFallbackResponse();
    }

    private String getFallbackResponse() {
        List<String> responses = List.of(
            "I'm feeling great today! 😊",
            "Pretty good, thanks for asking! 🌟",
            "I'm in a curious mood today! 🤔",
            "Feeling energetic and ready to help! ⚡",
            "I'm doing well, how about you? 😄"
        );
        return responses.get(random.nextInt(responses.size()));
    }
}