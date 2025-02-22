package com.example.gamesphere;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * The class represents an activity which performs a task which displays Splash window,
 * on app's launching.
 * Corresponding content view: activity_splash.xml
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity
{
    private final Handler handler = new Handler();
    private Runnable runnable; // a task to be executed by a thread.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // This code will execute after a delay
        runnable = () -> {
            // Transitions from SplashActivity to MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        };
        handler.postDelayed(runnable,7000);
    }

    /**
     * The function is called when an activity is being destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // ensures that any pending tasks associated with the Handler are removed.
    }
}