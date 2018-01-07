package org.anirudh.redquark.chatterbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* We do not have setContentView() for this SplashActivity.
        View is displaying from the theme and this way it is faster than creating a layout. */

        /* Starting LoginActivity.java */
        startActivity(new Intent(this, LoginActivity.class));
    }
}
