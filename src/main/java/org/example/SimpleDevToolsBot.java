package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SimpleDevToolsBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "@simpledevtoolsbot"; // Имя бота
    }

    @Override
    public String getBotToken() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            return props.getProperty("bot.token");
        } catch (IOException e) {
            e.printStackTrace();
            return null; // или кидай ошибку, если хочешь
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();


        if (message.equalsIgnoreCase("/start")) {
            sendMessage(chatId, """
            👋 Привет! Я DevTools Бот.

            Я могу помочь тебе с технической инфой по сайтам:

            📌 Доступные команды:
/ip <домен> — покажу IP-адрес
/headers <ссылка> — выдам HTTP-заголовки
/ping <домен> — проверю пинг

💡 Просто напиши мне команду и я всё сделаю.

Пример: /ip google.com
            """);
            return;
        }





        if (message.startsWith("/ip ")) {
            String domain = message.substring(4).trim();
            sendMessage(chatId, getIP(domain));
        } else if (message.startsWith("/headers ")) {
            String url = message.substring(9).trim();
            sendMessage(chatId, getHeaders(url));
        } else if (message.startsWith("/ping ")) {
            String domain = message.substring(6).trim();
            sendMessage(chatId, ping(domain));
        } else {
            sendMessage(chatId, "❓ Команда не найдена. Примеры:\n" +
                    "/ip google.com\n/headers https://example.com\n/ping github.com");
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getIP(String domain) {
        try {
            InetAddress address = InetAddress.getByName(domain);
            return "🌐 IP: " + address.getHostAddress();
        } catch (Exception e) {
            return "❌ Ошибка: " + e.getMessage();
        }
    }

    private String getHeaders(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            Map<String, List<String>> headers = con.getHeaderFields();
            StringBuilder sb = new StringBuilder("📋 Заголовки:\n");

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            return "❌ Ошибка: " + e.getMessage();
        }
    }

    private String ping(String domain) {
        try {
            Process p = Runtime.getRuntime().exec("ping -c 4 " + domain); // Linux/Unix
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
            }
            input.close();

            return "📡 Результат:\n" + output.toString();
        } catch (Exception e) {
            return "❌ Ошибка: " + e.getMessage();
        }
    }
}
