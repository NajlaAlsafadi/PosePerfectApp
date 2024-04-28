package com.example.poseperfect.homeNav;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.poseperfect.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // get instance of firebase auth
        mAuth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        // set item selected listener to bottom navigation view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                // depending on the selected item, instantiate the corresponding fragment
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (itemId == R.id.nav_progress) {
                    selectedFragment = new ProgressFragment();
                } else if (itemId == R.id.nav_exercise) {
                    selectedFragment = new ExerciseFragment();
                }

                if (selectedFragment != null) {// if a fragment was selected, replace current fragment with the selected one
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });

        // if there is no saved instance state, set home as the selected item
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
    // method to select exercise tab
    public void selectExerciseTab() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_exercise);
    }
    // overriding back button press
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        // if current fragment is not home, replace it with home fragment
        if (!(currentFragment instanceof HomeFragment)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        } else {

            //DO NOTHING
        }
    }
}