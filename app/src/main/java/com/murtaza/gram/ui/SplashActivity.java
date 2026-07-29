package com.murtaza.gram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;

import com.murtaza.gram.R;
import com.murtaza.gram.util.MurtazaGramConfig;

/**
 * Splash screen — shows MurtazaGram branding for 2 seconds, then routes to Login or Main.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            MurtazaGramConfig config = MurtazaGramConfig.getInstance(this);
            Intent intent;
            if (config.isLoggedIn()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }
}
