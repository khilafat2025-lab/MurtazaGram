package com.murtaza.gram.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.murtaza.gram.R;
import com.murtaza.gram.api.TelegramApiClient;
import com.murtaza.gram.util.MurtazaGramConfig;
import com.murtaza.gram.util.MurtazaGramUtils;

import org.json.JSONObject;

/**
 * Login screen — user enters their Telegram Bot Token (from @BotFather).
 * Validates the token by calling getMe, then saves it and proceeds to MainActivity.
 */
public class LoginActivity extends Activity {

    private EditText etBotToken;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etBotToken = findViewById(R.id.etBotToken);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String token = etBotToken.getText().toString().trim();

        if (token.isEmpty()) {
            showError(getString(R.string.login_error_empty));
            return;
        }

        if (!MurtazaGramUtils.isValidBotToken(token)) {
            showError(getString(R.string.login_error_invalid));
            return;
        }

        tvError.setVisibility(View.GONE);
        btnLogin.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        TelegramApiClient client = new TelegramApiClient(token);
        client.getMe(new TelegramApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                MurtazaGramConfig config = MurtazaGramConfig.getInstance(LoginActivity.this);
                config.setBotToken(token);

                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showError(getString(R.string.login_failed, error));
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
