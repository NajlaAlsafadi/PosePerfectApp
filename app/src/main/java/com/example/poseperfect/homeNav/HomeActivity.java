package com.example.poseperfect.homeNav;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.poseperfect.ExerciseFragment;
import com.example.poseperfect.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvWelcomeBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        tvWelcomeBack = findViewById(R.id.tvWelcomeBack);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        updateWelcomeText();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (itemId == R.id.nav_progress) {
                    selectedFragment = new ProgressFragment();
                } else if (itemId == R.id.nav_exercise) {
                    selectedFragment = new ExerciseFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });


        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void updateWelcomeText() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String welcomeText = getString(R.string.welcome_back_with_name, currentUser.getDisplayName());
            tvWelcomeBack.setText(welcomeText);
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back press in relation to your fragments or bottom navigation view
        super.onBackPressed();
    }
}