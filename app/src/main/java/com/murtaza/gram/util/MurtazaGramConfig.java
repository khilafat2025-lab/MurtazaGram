package com.murtaza.gram.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * MurtazaGram configuration manager.
 * Stores all power-feature settings using a separate SharedPreferences file
 * so it never conflicts with official Telegram.
 */
public class MurtazaGramConfig {
    private static final String PREFS_NAME = "murtazagram_prefs";
    private static SharedPreferences prefs;
    private static MurtazaGramConfig instance;

    private static final String KEY_BOT_TOKEN = "bot_token";
    private static final String KEY_GHOST_MODE = "ghost_mode";
    private static final String KEY_AUTO_TRANSLATE = "auto_translate";
    private static final String KEY_ANTI_DELETE = "anti_delete";
    private static final String KEY_FAST_DOWNLOAD = "fast_download";
    private static final String KEY_HIDE_ONLINE = "hide_online";
    private static final String KEY_ORIGINAL_QUALITY = "original_quality";
    private static final String KEY_CUSTOM_FONT = "custom_font";

    public static final int MAX_CONNECTIONS = 16;
    public static final int CHUNK_SIZE = 512 * 1024;
    public static final int PREFETCH_SIZE = 4 * 1024 * 1024;
    public static final int MAX_PINNED_MESSAGES = 20;

    private MurtazaGramConfig(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized MurtazaGramConfig getInstance(Context context) {
        if (instance == null) {
            instance = new MurtazaGramConfig(context);
        }
        return instance;
    }

    public String getBotToken() {
        return prefs.getString(KEY_BOT_TOKEN, null);
    }

    public void setBotToken(String token) {
        prefs.edit().putString(KEY_BOT_TOKEN, token).apply();
    }

    public boolean isGhostModeEnabled() {
        return prefs.getBoolean(KEY_GHOST_MODE, false);
    }

    public void setGhostMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_GHOST_MODE, enabled).apply();
    }

    public boolean isGhostModeForChat(long chatId) {
        return isGhostModeEnabled();
    }

    public boolean isAutoTranslateEnabled() {
        return prefs.getBoolean(KEY_AUTO_TRANSLATE, false);
    }

    public void setAutoTranslate(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, enabled).apply();
    }

    public boolean isAntiDeleteEnabled() {
        return prefs.getBoolean(KEY_ANTI_DELETE, true);
    }

    public void setAntiDelete(boolean enabled) {
        prefs.edit().putBoolean(KEY_ANTI_DELETE, enabled).apply();
    }

    public boolean isFastDownloadEnabled() {
        return prefs.getBoolean(KEY_FAST_DOWNLOAD, true);
    }

    public void setFastDownload(boolean enabled) {
        prefs.edit().putBoolean(KEY_FAST_DOWNLOAD, enabled).apply();
    }

    public boolean isHideOnlineEnabled() {
        return prefs.getBoolean(KEY_HIDE_ONLINE, false);
    }

    public void setHideOnline(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIDE_ONLINE, enabled).apply();
    }

    public boolean isOriginalQualityEnabled() {
        return prefs.getBoolean(KEY_ORIGINAL_QUALITY, false);
    }

    public void setOriginalQuality(boolean enabled) {
        prefs.edit().putBoolean(KEY_ORIGINAL_QUALITY, enabled).apply();
    }

    public boolean isCustomFontEnabled() {
        return prefs.getBoolean(KEY_CUSTOM_FONT, false);
    }

    public void setCustomFont(boolean enabled) {
        prefs.edit().putBoolean(KEY_CUSTOM_FONT, enabled).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        String token = getBotToken();
        return token != null && !token.isEmpty();
    }
}
