package com.murtaza.gram.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Telegram Chat model — represents a conversation (private chat, group, channel, or bot).
 */
public class Chat {
    public long id;
    public String type;
    public String title;
    public String username;
    public String firstName;
    public String lastName;
    public long lastMessageDate;
    public String lastMessageText;
    public int unreadCount;
    public boolean isOnline;
    public boolean isBot;
    public boolean isRestricted;
    public boolean noForwards;

    public String getDisplayName() {
        if (title != null && !title.isEmpty()) return title;
        if (firstName != null && !firstName.isEmpty()) {
            if (lastName != null && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            }
            return firstName;
        }
        if (username != null && !username.isEmpty()) return "@" + username;
        return "Unknown";
    }

    public String getChatType() {
        if (isBot) return "bot";
        if ("channel".equals(type)) return "channel";
        if ("group".equals(type) || "supergroup".equals(type)) return "group";
        return "chat";
    }

    public boolean isGroup() {
        return "group".equals(type) || "supergroup".equals(type);
    }

    public boolean isChannel() {
        return "channel".equals(type);
    }

    public boolean isPrivateChat() {
        return "private".equals(type);
    }

    public static Chat fromJson(JSONObject json) {
        Chat chat = new Chat();
        chat.id = json.optLong("id");
        chat.type = json.optString("type", "private");
        chat.title = json.optString("title", null);
        chat.username = json.optString("username", null);
        chat.firstName = json.optString("first_name", null);
        chat.lastName = json.optString("last_name", null);
        return chat;
    }

    public static List<Chat> fromJsonArray(JSONArray array) {
        List<Chat> chats = new ArrayList<>();
        if (array == null) return chats;
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) {
                chats.add(fromJson(obj));
            }
        }
        return chats;
    }
}
