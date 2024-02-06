package com.example.poseperfect.baseUI;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import com.example.poseperfect.R;
import com.google.android.material.tabs.TabLayout;

import me.relex.circleindicator.CircleIndicator;
import me.relex.circleindicator.CircleIndicator3;


public class MainActivity extends AppCompatActivity {

    ImageView appLogo;
    TextView welcomeText;

    ProgressBar progressBar;
    CircleIndicator3 indicator;
    ViewPager2 viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button nextButton = findViewById(R.id.nextButton);
       // progressBar = findViewById(R.id.progressBar);
        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.pageIndicator);

        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);


        indicator.setViewPager(viewPager);
        pagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());




        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}