package com.murtaza.gram.model;

import org.json.JSONObject;

/**
 * Telegram Message model.
 */
public class Message {
    public long messageId;
    public long date;
    public long chatId;
    public String text;
    public long senderId;
    public String senderName;
    public boolean isOutgoing;
    public boolean isDeleted;
    public String originalText;
    public String translatedText;
    public boolean isPinned;
    public boolean isForwarded;
    public boolean isRestricted;

    public String getDisplayText() {
        if (isDeleted && originalText != null) {
            return "[Deleted] " + originalText;
        }
        if (text == null || text.isEmpty()) return "";
        return text;
    }

    public static Message fromJson(JSONObject json) {
        Message msg = new Message();
        msg.messageId = json.optLong("message_id");
        msg.date = json.optLong("date");
        msg.chatId = json.optLong("chat_id");
        msg.text = json.optString("text", "");
        msg.isOutgoing = json.optBoolean("is_outgoing", false);

        JSONObject from = json.optJSONObject("from");
        if (from != null) {
            msg.senderId = from.optLong("id");
            String first = from.optString("first_name", "");
            String last = from.optString("last_name", "");
            msg.senderName = (first + " " + last).trim();
            if (msg.senderName.isEmpty()) {
                msg.senderName = from.optString("username", "Unknown");
            }
        }

        JSONObject chat = json.optJSONObject("chat");
        if (chat != null) {
            msg.chatId = chat.optLong("id");
        }

        return msg;
    }
}
