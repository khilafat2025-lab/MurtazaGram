package com.murtaza.gram.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.murtaza.gram.R;
import com.murtaza.gram.adapter.MessageAdapter;
import com.murtaza.gram.api.TelegramApiClient;
import com.murtaza.gram.model.Message;
import com.murtaza.gram.util.MurtazaGramConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat screen — shows messages for a conversation and allows sending.
 * Features: ghost mode toggle, message context menu (copy/forward/delete/translate/pin),
 * anti-delete, restricted chat bypass.
 */
public class ChatActivity extends Activity implements MessageAdapter.OnMessageLongClickListener {

    private ListView rvMessages;
    private MessageAdapter adapter;
    private EditText etMessage;
    private ImageView btnSend, btnBack, btnGhostMode, btnChatMenu;
    private TextView tvChatName, tvChatStatus;
    private LinearLayout pinnedBar;
    private TextView tvPinnedText, tvPinnedCount;

    private long chatId;
    private String chatName;
    private String chatType;

    private TelegramApiClient apiClient;
    private MurtazaGramConfig config;

    private List<Message> messages = new ArrayList<>();
    private int lastUpdateId = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPolling = false;
    private int pinnedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getLongExtra("chat_id", 0);
        chatName = getIntent().getStringExtra("chat_name");
        chatType = getIntent().getStringExtra("chat_type");

        config = MurtazaGramConfig.getInstance(this);
        apiClient = new TelegramApiClient(config.getBotToken());

        initViews();
        startPolling();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnGhostMode = findViewById(R.id.btnGhostMode);
        btnChatMenu = findViewById(R.id.btnChatMenu);
        tvChatName = findViewById(R.id.tvChatName);
        tvChatStatus = findViewById(R.id.tvChatStatus);
        pinnedBar = findViewById(R.id.pinnedBar);
        tvPinnedText = findViewById(R.id.tvPinnedText);
        tvPinnedCount = findViewById(R.id.tvPinnedCount);

        tvChatName.setText(chatName != null ? chatName : "Chat");

        switch (chatType != null ? chatType : "chat") {
            case "group":
                tvChatStatus.setText("group");
                tvChatStatus.setTextColor(getResources().getColor(R.color.text_secondary));
                break;
            case "channel":
                tvChatStatus.setText("channel");
                tvChatStatus.setTextColor(getResources().getColor(R.color.text_secondary));
                break;
            case "bot":
                tvChatStatus.setText("bot");
                tvChatStatus.setTextColor(getResources().getColor(R.color.text_accent));
                break;
            default:
                tvChatStatus.setText("online");
                tvChatStatus.setTextColor(getResources().getColor(R.color.online));
                break;
        }

        adapter = new MessageAdapter(this);
        rvMessages.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        updateGhostModeIcon();
        btnGhostMode.setOnClickListener(v -> {
            boolean newState = !config.isGhostModeForChat(chatId);
            config.setGhostMode(newState);
            updateGhostModeIcon();
            Toast.makeText(this, newState ? R.string.ghost_mode_on : R.string.ghost_mode_off,
                    Toast.LENGTH_SHORT).show();
        });

        btnChatMenu.setOnClickListener(v -> showChatMenu());
        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setAlpha(s.length() > 0 ? 1.0f : 0.5f);
                btnSend.setEnabled(s.length() > 0);
            }
        });
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.5f);
    }

    private void updateGhostModeIcon() {
        if (config.isGhostModeForChat(chatId)) {
            btnGhostMode.setAlpha(1.0f);
            btnGhostMode.setColorFilter(getResources().getColor(R.color.accent));
        } else {
            btnGhostMode.setAlpha(0.5f);
            btnGhostMode.setColorFilter(null);
        }
    }

    private void showChatMenu() {
        PopupMenu popup = new PopupMenu(this, btnChatMenu);
        popup.getMenu().add(0, 1, 0, "View Profile");
        popup.getMenu().add(0, 2, 0, "Mark All as Read");
        popup.getMenu().add(0, 3, 0, "Pinned Messages");
        popup.getMenu().add(0, 4, 0, "Search");
        popup.getMenu().add(0, 5, 0, "Clear Chat");
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    startActivity(new Intent(this, ProfileActivity.class)
                            .putExtra("chat_id", chatId)
                            .putExtra("chat_name", chatName));
                    return true;
                case 2:
                    Toast.makeText(this, R.string.mark_all_read_done, Toast.LENGTH_SHORT).show();
                    return true;
                case 3:
                    Toast.makeText(this, "Pinned: " + pinnedCount + "/20", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");

        Message msg = new Message();
        msg.messageId = System.currentTimeMillis();
        msg.date = System.currentTimeMillis() / 1000;
        msg.chatId = chatId;
        msg.text = text;
        msg.isOutgoing = true;
        msg.senderName = "You";
        messages.add(msg);
        adapter.addMessage(msg);
        rvMessages.setSelection(messages.size() - 1);

        apiClient.sendMessage(chatId, text, new TelegramApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {}

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Send failed: " + error, Toast.LENGTH_SHORT).show();
                    msg.text = "[Failed] " + text;
                    adapter.updateMessage(msg);
                });
            }
        });
    }

    private void startPolling() {
        if (isPolling) return;
        isPolling = true;
        pollUpdates();
    }

    private void pollUpdates() {
        if (!isPolling) return;

        apiClient.getUpdates(lastUpdateId, new TelegramApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                runOnUiThread(() -> {
                    processUpdates(result);
                    handler.postDelayed(() -> pollUpdates(), 1000);
                });
            }

            @Override
            public void onError(String error) {
                handler.postDelayed(() -> pollUpdates(), 3000);
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

            JSONObject messageJson = update.optJSONObject("message");
            if (messageJson == null) messageJson = update.optJSONObject("channel_post");
            if (messageJson == null) messageJson = update.optJSONObject("edited_message");

            if (messageJson != null) {
                JSONObject chatJson = messageJson.optJSONObject("chat");
                if (chatJson != null && chatJson.optLong("id") == chatId) {
                    Message msg = Message.fromJson(messageJson);
                    if (config.isAntiDeleteEnabled()) {
                        msg.originalText = msg.text;
                    }
                    if (config.isAutoTranslateEnabled() && msg.text != null && !msg.text.isEmpty()) {
                        msg.translatedText = "[Auto-translated] " + msg.text;
                    }
                    messages.add(0, msg);
                    adapter.addMessage(msg);
                }
            }
        }

        if (!messages.isEmpty()) {
            rvMessages.setSelection(0);
        }
    }

    @Override
    public void onMessageLongClick(Message message, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(0, 1, 0, R.string.copy);
        popup.getMenu().add(0, 2, 0, R.string.forward);
        popup.getMenu().add(0, 3, 0, R.string.translate);
        popup.getMenu().add(0, 4, 0, R.string.pin);
        popup.getMenu().add(0, 5, 0, R.string.delete);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    copyText(message.text);
                    return true;
                case 2:
                    Toast.makeText(this, R.string.forwarding_allowed, Toast.LENGTH_SHORT).show();
                    return true;
                case 3:
                    if (message.translatedText == null) {
                        message.translatedText = "[Translated] " + message.text;
                        adapter.updateMessage(message);
                    }
                    Toast.makeText(this, R.string.translating, Toast.LENGTH_SHORT).show();
                    return true;
                case 4:
                    if (pinnedCount < MurtazaGramConfig.MAX_PINNED_MESSAGES) {
                        pinnedCount++;
                        pinnedBar.setVisibility(View.VISIBLE);
                        tvPinnedText.setText(message.text != null ? message.text : "[Message]");
                        tvPinnedCount.setText(pinnedCount + "/20");
                        Toast.makeText(this, "Pinned (" + pinnedCount + "/20)", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Max 20 pinned messages reached", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case 5:
                    messages.remove(message);
                    adapter.setMessages(messages);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && text != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("message", text));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPolling = false;
    }
}
