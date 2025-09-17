package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class AppBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final ChatModel chatModel;
    private int lastMainMenuMessageId = -1;

    public AppBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        chatModel = new ChatModel();
    }

    @Override
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println(update.getMessage().getText());
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                lastMainMenuMessageId = sendMainMenu(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();

            switch (data) {
                case "student":
                    deleteMessage(chatId, messageId);
                    sendInfo(chatId, "📚 Student Information\n\n" +
                            "• Name: Viktoriia\n" +
                            "• Student ID: 12345\n" +
                            "• Faculty: FICE\n" +
                            "• Year: 4rd year\n" +
                            "• Status: DeadInside");
                    break;
                case "it":
                    deleteMessage(chatId, messageId);
                    sendInfo(chatId, "💻 IT Technology Information\n\n" +
                            "• Programming Languages: Java, Python, JavaScript\n" +
                            "• Frameworks: Spring Boot, React, Node.js\n" +
                            "• Databases: MySQL, PostgreSQL, MongoDB\n" +
                            "• Cloud: AWS, Docker, Kubernetes\n" +
                            "• Tools: Git, Jenkins, IntelliJ IDEA\n" +
                            "• Current Focus: Microservices Architecture");
                    break;
                case "contacts":
                    deleteMessage(chatId, messageId);
                    sendInfo(chatId, "📞 Contact Information\n\n" +
                            "• Email: test.email@kpi.ua\n" +
                            "• Phone: +380 00 000 00 00\n" +
                            "• LinkedIn: linkedin.com/\n" +
                            "• GitHub: github.com/n" +
                            "• Office Hours: Mon-Fri 9:00-17:00");
                    break;
                case "chat":
                    deleteMessage(chatId, messageId);
                    String chatResponse = chatModel.getChatResponse();
                    sendInfo(chatId, "🤖 AI Chat Response:\n\n" + chatResponse);
                    break;
                case "back":
                    deleteMessage(chatId, messageId);
                    lastMainMenuMessageId = sendMainMenu(chatId);
                    break;
            }
        }
    }

    private int sendMainMenu(long chatId) {
        InlineKeyboardMarkup mainMenuMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("📚 Student")
                                .callbackData("student")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("💻 IT Technology")
                                .callbackData("it")
                                .build()
                ))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("📞 Contacts")
                                .callbackData("contacts")
                                .build(),
                        InlineKeyboardButton.builder()
                                .text("💬 AI Chat")
                                .callbackData("chat")
                                .build()
                ))
                .build();

        SendMessage message = new SendMessage(String.valueOf(chatId),
                "🎯 Welcome to the Main Menu!\n\nPlease select an option:");
        message.setReplyMarkup(mainMenuMarkup);
        return executeAndGetMessageId(message);
    }

    private void sendInfo(long chatId, String text) {
        InlineKeyboardMarkup backMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("⬅️ Back to Menu")
                                .callbackData("back")
                                .build()
                ))
                .build();

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setReplyMarkup(backMarkup);
        execute(msg);
    }

    private void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private int executeAndGetMessageId(SendMessage message) {
        try {
            return telegramClient.execute(message).getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
            telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
