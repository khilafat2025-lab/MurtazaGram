package com.murtaza.gram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.murtaza.gram.R;
import com.murtaza.gram.util.MurtazaGramConfig;

/**
 * Settings screen — all MurtazaGram power features toggles.
 */
public class SettingsActivity extends Activity {

    private MurtazaGramConfig config;

    private Switch swGhostMode, swAutoTranslate, swAntiDelete, swFastDownload,
                   swHideOnline, swOriginalQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        config = MurtazaGramConfig.getInstance(this);

        initViews();
        loadSettings();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        swGhostMode = findViewById(R.id.swGhostMode);
        swAutoTranslate = findViewById(R.id.swAutoTranslate);
        swAntiDelete = findViewById(R.id.swAntiDelete);
        swFastDownload = findViewById(R.id.swFastDownload);
        swHideOnline = findViewById(R.id.swHideOnline);
        swOriginalQuality = findViewById(R.id.swOriginalQuality);

        swGhostMode.setOnCheckedChangeListener((button, checked) -> config.setGhostMode(checked));
        swAutoTranslate.setOnCheckedChangeListener((button, checked) -> config.setAutoTranslate(checked));
        swAntiDelete.setOnCheckedChangeListener((button, checked) -> config.setAntiDelete(checked));
        swFastDownload.setOnCheckedChangeListener((button, checked) -> config.setFastDownload(checked));
        swHideOnline.setOnCheckedChangeListener((button, checked) -> config.setHideOnline(checked));
        swOriginalQuality.setOnCheckedChangeListener((button, checked) -> config.setOriginalQuality(checked));

        LinearLayout rowMarkAllRead = findViewById(R.id.rowMarkAllRead);
        rowMarkAllRead.setOnClickListener(v -> Toast.makeText(this, R.string.mark_all_read_done, Toast.LENGTH_SHORT).show());

        LinearLayout rowArchiveRead = findViewById(R.id.rowArchiveRead);
        rowArchiveRead.setOnClickListener(v -> Toast.makeText(this, R.string.archive_done, Toast.LENGTH_SHORT).show());

        LinearLayout rowAbout = findViewById(R.id.rowAbout);
        rowAbout.setOnClickListener(v -> {
            new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle("MurtazaGram")
                    .setMessage("MurtazaGram v1.0.0\n\nAn enhanced Telegram client with power features:\n\n" +
                            "\u2022 Ghost Mode\n\u2022 Auto Translation\n\u2022 Anti-Delete\n\u2022 Fast Downloads (16 connections)\n" +
                            "\u2022 Pin up to 20 messages\n\u2022 Forward from restricted chats\n\u2022 Copy from restricted chats\n" +
                            "\u2022 User ID on profile\n\u2022 Filter tabs (Chats/Groups/Channels/Bots)\n\n" +
                            "Built on the Telegram Bot API.\nNot affiliated with Telegram.")
                    .setPositiveButton(R.string.ok, null)
                    .show();
        });

        LinearLayout rowLogout = findViewById(R.id.rowLogout);
        rowLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        config.clearAll();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        TextView tvBotName = findViewById(R.id.tvBotName);
        TextView tvBotUsername = findViewById(R.id.tvBotUsername);
        tvBotName.setText("MurtazaGram");
        tvBotUsername.setText("Connected via Bot API");
    }

    private void loadSettings() {
        swGhostMode.setChecked(config.isGhostModeEnabled());
        swAutoTranslate.setChecked(config.isAutoTranslateEnabled());
        swAntiDelete.setChecked(config.isAntiDeleteEnabled());
        swFastDownload.setChecked(config.isFastDownloadEnabled());
        swHideOnline.setChecked(config.isHideOnlineEnabled());
        swOriginalQuality.setChecked(config.isOriginalQualityEnabled());
    }
}
