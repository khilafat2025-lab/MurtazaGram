package com.murtaza.gram.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

/**
 * Telegram Bot API client.
 * All calls run on background threads; results are delivered on the main thread.
 */
public class TelegramApiClient {
    private static final String BASE_URL = "https://api.telegram.org/bot";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    public interface ApiCallbackArray {
        void onSuccess(JSONArray response);
        void onError(String error);
    }

    private final String botToken;

    public TelegramApiClient(String botToken) {
        this.botToken = botToken;
    }

    public void callApi(String method, java.util.Map<String, String> params, ApiCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject result = doApiCall(method, params);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (ApiException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void callApi(String method, ApiCallback callback) {
        callApi(method, null, callback);
    }

    private JSONObject doApiCall(String method, java.util.Map<String, String> params) throws IOException, ApiException {
        String urlString = BASE_URL + botToken + "/" + method;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoInput(true);

        try {
            if (params != null && !params.isEmpty()) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                StringBuilder postData = new StringBuilder();
                for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
                    if (postData.length() > 0) postData.append("&");
                    postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    postData.append("=");
                    postData.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postDataBytes);
                }
            }

            int responseCode = conn.getResponseCode();
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json;
            try {
                json = new JSONObject(response.toString());
            } catch (org.json.JSONException e) {
                throw new ApiException("Invalid response: " + e.getMessage());
            }
            if (!json.optBoolean("ok", false)) {
                String description = json.optString("description", "Unknown error");
                throw new ApiException(description);
            }
            return json.optJSONObject("result");
        } finally {
            conn.disconnect();
        }
    }

    public void getMe(ApiCallback callback) {
        callApi("getMe", callback);
    }

    public void getUpdates(int offset, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("offset", String.valueOf(offset));
        params.put("timeout", "0");
        params.put("limit", "100");
        callApi("getUpdates", params, callback);
    }

    public void sendMessage(long chatId, String text, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("chat_id", String.valueOf(chatId));
        params.put("text", text);
        params.put("parse_mode", "HTML");
        callApi("sendMessage", params, callback);
    }

    public void getChat(long chatId, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("chat_id", String.valueOf(chatId));
        callApi("getChat", params, callback);
    }

    public void getChatMemberCount(long chatId, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("chat_id", String.valueOf(chatId));
        callApi("getChatMemberCount", params, callback);
    }

    public void pinChatMessage(long chatId, long messageId, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("chat_id", String.valueOf(chatId));
        params.put("message_id", String.valueOf(messageId));
        callApi("pinChatMessage", params, callback);
    }

    public void deleteMessage(long chatId, long messageId, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("chat_id", String.valueOf(chatId));
        params.put("message_id", String.valueOf(messageId));
        callApi("deleteMessage", params, callback);
    }

    public void forwardMessage(long fromChatId, long messageId, long toChatId, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("chat_id", String.valueOf(toChatId));
        params.put("from_chat_id", String.valueOf(fromChatId));
        params.put("message_id", String.valueOf(messageId));
        callApi("forwardMessage", params, callback);
    }

    public void setMyCommands(String commandsJson, ApiCallback callback) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("commands", commandsJson);
        callApi("setMyCommands", params, callback);
    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}
