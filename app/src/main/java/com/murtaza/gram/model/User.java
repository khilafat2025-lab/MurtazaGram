package com.murtaza.gram.model;

import org.json.JSONObject;

/**
 * Telegram User model — represents a bot or user.
 */
public class User {
    public long id;
    public boolean isBot;
    public String firstName;
    public String lastName;
    public String username;
    public String languageCode;
    public boolean canJoinGroups;
    public boolean canReadAllGroupMessages;
    public boolean supportsInlineQueries;

    public String getDisplayName() {
        if (firstName != null && !firstName.isEmpty()) {
            if (lastName != null && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            }
            return firstName;
        }
        if (username != null && !username.isEmpty()) return "@" + username;
        return "Unknown";
    }

    public static User fromJson(JSONObject json) {
        User user = new User();
        user.id = json.optLong("id");
        user.isBot = json.optBoolean("is_bot", false);
        user.firstName = json.optString("first_name", null);
        user.lastName = json.optString("last_name", null);
        user.username = json.optString("username", null);
        user.languageCode = json.optString("language_code", null);
        user.canJoinGroups = json.optBoolean("can_join_groups", false);
        user.canReadAllGroupMessages = json.optBoolean("can_read_all_group_messages", false);
        user.supportsInlineQueries = json.optBoolean("supports_inline_queries", false);
        return user;
    }
}
