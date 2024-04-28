package com.example.poseperfect.baseUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.example.poseperfect.R;
import com.google.android.material.tabs.TabLayout;



public class LoginActivity extends AppCompatActivity {


    TabLayout tabLayout;
    ViewPager viewpager;
    Context context;

    // animation variable
    float v=0;

    // create activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // set layout
        Intent intent = getIntent(); // get intent
        tabLayout = findViewById(R.id.tab_layout); // get tab layout
        viewpager = findViewById(R.id.view_pager); // get view pager

        // add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Login"));
        tabLayout.addTab(tabLayout.newTab().setText("Signup"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // set up adapter
        final LoginAdapter adapter = new LoginAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewpager.setAdapter(adapter);
        viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewpager.setAdapter(adapter);
                viewpager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewpager.setCurrentItem(tab.getPosition());
                final LoginAdapter adapter = new LoginAdapter(getSupportFragmentManager(), context, tabLayout.getTabCount());
                viewpager.setAdapter(adapter);

            }
        });

        // animate tab layout
        tabLayout.setTranslationY(300);
        tabLayout.setAlpha(v);
        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();

    }
}