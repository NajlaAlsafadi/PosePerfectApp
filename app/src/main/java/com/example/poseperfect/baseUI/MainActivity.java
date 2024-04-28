package com.example.poseperfect.baseUI;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.poseperfect.R;
import me.relex.circleindicator.CircleIndicator3;


public class MainActivity extends AppCompatActivity {

    CircleIndicator3 indicator;
    ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button nextButton = findViewById(R.id.nextButton);
        viewPager = findViewById(R.id.viewPager);
        indicator = findViewById(R.id.pageIndicator);
        // create notification channel for app notifications
        createNotificationChannel();

        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);


        indicator.setViewPager(viewPager);
        // connect indicator with viewpager
        pagerAdapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

        // add item decoration to viewpager for spacing between items
        viewPager.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());

                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.left = space;
                }
            }
        });
        // handle next button click to navigate to the login activity
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    // creates a notification channel if SDK version is Oreo or higher
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("YogaReminder", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}