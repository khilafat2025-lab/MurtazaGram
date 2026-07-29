package com.murtaza.gram.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;

import com.murtaza.gram.R;
import com.murtaza.gram.adapter.ChatAdapter;
import com.murtaza.gram.api.TelegramApiClient;
import com.murtaza.gram.model.Chat;
import com.murtaza.gram.util.MurtazaGramConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen — shows the chat list with filter tabs (All / Chats / Groups / Channels / Bots).
 * Polls Telegram Bot API for updates and populates the chat list.
 */
public class MainActivity extends Activity implements ChatAdapter.OnChatClickListener {

    private ListView rvChats;
    private ChatAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private TextView tabAll, tabChats, tabGroups, tabChannels, tabBots;
    private TextView[] tabs;

    private TelegramApiClient apiClient;
    private MurtazaGramConfig config;

    private List<Chat> allChats = new ArrayList<>();
    private String currentFilter = "all";
    private int lastUpdateId = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config = MurtazaGramConfig.getInstance(this);

        if (!config.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        apiClient = new TelegramApiClient(config.getBotToken());

        initViews();
        startPolling();
    }

    private void initViews() {
        rvChats = findViewById(R.id.rvChats);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        adapter = new ChatAdapter(this);
        rvChats.setAdapter(adapter);

        tabAll = findViewById(R.id.tabAll);
        tabChats = findViewById(R.id.tabChats);
        tabGroups = findViewById(R.id.tabGroups);
        tabChannels = findViewById(R.id.tabChannels);
        tabBots = findViewById(R.id.tabBots);
        tabs = new TextView[]{tabAll, tabChats, tabGroups, tabChannels, tabBots};

        tabAll.setOnClickListener(v -> setFilter("all"));
        tabChats.setOnClickListener(v -> setFilter("chat"));
        tabGroups.setOnClickListener(v -> setFilter("group"));
        tabChannels.setOnClickListener(v -> setFilter("channel"));
        tabBots.setOnClickListener(v -> setFilter("bot"));

        ImageView btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        ImageView btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> Toast.makeText(this, "Search coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabColors();
        applyFilter();
    }

    private void updateTabColors() {
        for (TextView tab : tabs) {
            tab.setTextColor(getResources().getColor(R.color.text_secondary));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        TextView activeTab;
        switch (currentFilter) {
            case "chat": activeTab = tabChats; break;
            case "group": activeTab = tabGroups; break;
            case "channel": activeTab = tabChannels; break;
            case "bot": activeTab = tabBots; break;
            default: activeTab = tabAll; break;
        }
        activeTab.setTextColor(getResources().getColor(R.color.text_accent));
        activeTab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void applyFilter() {
        List<Chat> filtered = new ArrayList<>();
        for (Chat chat : allChats) {
            if ("all".equals(currentFilter) || currentFilter.equals(chat.getChatType())) {
                filtered.add(chat);
            }
        }
        adapter.setChats(filtered);

        if (filtered.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvChats.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvChats.setVisibility(View.VISIBLE);
        }
    }

    private void startPolling() {
        if (isPolling) return;
        isPolling = true;
        progressBar.setVisibility(View.VISIBLE);
        pollUpdates();
    }

    private void pollUpdates() {
        if (!isPolling) return;

        apiClient.getUpdates(lastUpdateId, new TelegramApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    processUpdates(result);
                    handler.postDelayed(() -> pollUpdates(), 1000);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (allChats.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                    handler.postDelayed(() -> pollUpdates(), 3000);
                });
            }
        });
    }

    private void processUpdates(JSONObject result) {
        if (result == null) return;
        JSONArray updates = result.optJSONArray("updates");
        if (updates == null) return;

        for (int i = 0; i < updates.length(); i++) {
            JSONObject update = updates.optJSONObject(i);
            if (update == null) continue;

            int updateId = update.optInt("update_id");
            if (updateId >= lastUpdateId) {
                lastUpdateId = updateId + 1;
            }

            JSONObject message = update.optJSONObject("message");
            if (message == null) message = update.optJSONObject("channel_post");
            if (message == null) message = update.optJSONObject("edited_message");

            if (message != null) {
                processMessage(message);
            }
        }

        applyFilter();
    }

    private void processMessage(JSONObject messageJson) {
        JSONObject chatJson = messageJson.optJSONObject("chat");
        if (chatJson == null) return;

        Chat chat = Chat.fromJson(chatJson);

        JSONObject from = messageJson.optJSONObject("from");
        if (from != null && from.optBoolean("is_bot", false)) {
            chat.isBot = true;
        }

        chat.lastMessageText = messageJson.optString("text", "");
        if (chat.lastMessageText.isEmpty()) {
            String caption = messageJson.optString("caption", "");
            if (!caption.isEmpty()) {
                chat.lastMessageText = "[Photo] " + caption;
            } else {
                chat.lastMessageText = "[Message]";
            }
        }
        chat.lastMessageDate = messageJson.optLong("date");

        boolean found = false;
        for (int i = 0; i < allChats.size(); i++) {
            if (allChats.get(i).id == chat.id) {
                allChats.set(i, chat);
                found = true;
                break;
            }
        }
        if (!found) {
            allChats.add(0, chat);
        }
    }

    @Override
    public void onChatClick(Chat chat) {
        startActivity(new Intent(this, ChatActivity.class)
                .putExtra("chat_id", chat.id)
                .putExtra("chat_name", chat.getDisplayName())
                .putExtra("chat_type", chat.getChatType()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPolling = false;
    }
}
