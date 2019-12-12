package paps.bookman.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import paps.bookman.R;
import paps.bookman.util.HotelUserPrefs;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final HotelUserPrefs prefs = new HotelUserPrefs(this);
        final Intent intent = new Intent();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (prefs.isLoggedIn()) {
                    intent.setClass(SplashActivity.this, HomeActivity.class);
                } else {
                    intent.setClass(SplashActivity.this, AuthActivity.class);
                }
                startActivity(intent);
                finishAfterTransition();
            }
        }, 2000);
    }
}
