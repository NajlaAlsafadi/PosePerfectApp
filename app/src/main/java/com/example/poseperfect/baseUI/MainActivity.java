package com.example.poseperfect.baseUI;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.poseperfect.R;

public class MainActivity extends AppCompatActivity {

    ImageView appLogo;
    TextView welcomeText;
    ImageView arrowImage;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appLogo = findViewById(R.id.app_logo);
        welcomeText = findViewById(R.id.welcome_text);
        arrowImage = findViewById(R.id.arrow_image);
        progressBar = findViewById(R.id.progress_bar);

        // todo- add colour from my range
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.light_purple));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_purple));

        // todo- add some animations
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        appLogo.setAnimation(topAnim);
        welcomeText.setAnimation(bottomAnim);

        arrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}