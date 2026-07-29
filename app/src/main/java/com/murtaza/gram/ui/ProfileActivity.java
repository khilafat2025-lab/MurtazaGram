package com.murtaza.gram.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.murtaza.gram.R;
import com.murtaza.gram.api.TelegramApiClient;
import com.murtaza.gram.util.MurtazaGramConfig;
import com.murtaza.gram.util.MurtazaGramUtils;

import org.json.JSONObject;

/**
 * Profile screen — shows user/bot info with User ID, username, phone, join date.
 * All fields have one-tap copy.
 */
public class ProfileActivity extends Activity {

    private TelegramApiClient apiClient;
    private long chatId;

    private TextView tvProfileName, tvProfileStatus, tvUserId, tvUsername, tvPhone, tvJoinDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        chatId = getIntent().getLongExtra("chat_id", 0);
        String chatName = getIntent().getStringExtra("chat_name");

        MurtazaGramConfig config = MurtazaGramConfig.getInstance(this);
        apiClient = new TelegramApiClient(config.getBotToken());

        initViews(chatName);
        loadProfile();
    }

    private void initViews(String chatName) {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);
        tvUserId = findViewById(R.id.tvUserId);
        tvUsername = findViewById(R.id.tvUsername);
        tvPhone = findViewById(R.id.tvPhone);
        tvJoinDate = findViewById(R.id.tvJoinDate);

        if (chatName != null) {
            tvProfileName.setText(chatName);
        }

        LinearLayout rowUserId = findViewById(R.id.rowUserId);
        rowUserId.setOnClickListener(v -> MurtazaGramUtils.copyToClipboard(this, "User ID", tvUserId.getText().toString()));

        LinearLayout rowUsername = findViewById(R.id.rowUsername);
        rowUsername.setOnClickListener(v -> MurtazaGramUtils.copyToClipboard(this, "Username", tvUsername.getText().toString()));

        LinearLayout rowPhone = findViewById(R.id.rowPhone);
        rowPhone.setOnClickListener(v -> MurtazaGramUtils.copyToClipboard(this, "Phone", tvPhone.getText().toString()));
    }

    private void loadProfile() {
        if (chatId == 0) {
            apiClient.getMe(new TelegramApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    runOnUiThread(() -> {
                        tvProfileName.setText(result.optString("first_name", "MurtazaGram Bot"));
                        tvUserId.setText(String.valueOf(result.optLong("id")));
                        String username = result.optString("username", "");
                        tvUsername.setText(username.isEmpty() ? "N/A" : "@" + username);
                        tvPhone.setText("N/A (Bot)");
                        tvJoinDate.setText("Bot account");
                        tvProfileStatus.setText(result.optBoolean("is_bot") ? "bot" : "online");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        tvProfileName.setText("MurtazaGram");
                        tvUserId.setText("N/A");
                        tvUsername.setText("N/A");
                        tvPhone.setText("N/A");
                        tvJoinDate.setText("N/A");
                    });
                }
            });
        } else {
            apiClient.getChat(chatId, new TelegramApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    runOnUiThread(() -> {
                        String title = result.optString("title", "");
                        String firstName = result.optString("first_name", "");
                        String lastName = result.optString("last_name", "");
                        String name = !title.isEmpty() ? title :
                                (firstName + (lastName.isEmpty() ? "" : " " + lastName)).trim();
                        if (name.isEmpty()) name = "Unknown";

                        tvProfileName.setText(name);
                        tvUserId.setText(String.valueOf(result.optLong("id")));
                        String username = result.optString("username", "");
                        tvUsername.setText(username.isEmpty() ? "N/A" : "@" + username);

                        String phone = result.optString("phone_number", "");
                        tvPhone.setText(phone.isEmpty() ? "N/A" : "+" + phone);

                        String bio = result.optString("bio", "");
                        tvProfileStatus.setText(bio.isEmpty() ? "online" : bio);

                        tvJoinDate.setText("Not available via Bot API");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        tvProfileName.setText("Unknown");
                        tvUserId.setText(String.valueOf(chatId));
                        tvUsername.setText("N/A");
                        tvPhone.setText("N/A");
                        tvJoinDate.setText("N/A");
                    });
                }
            });
        }
    }
}
