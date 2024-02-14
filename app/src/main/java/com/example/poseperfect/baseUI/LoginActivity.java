package com.example.poseperfect.baseUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.poseperfect.homeNav.HomeActivity;
import com.example.poseperfect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private FirebaseAuth mAuth;
    TabLayout tabLayout;
    ViewPager viewpager;
    Context context;

    float v=0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        tabLayout = findViewById(R.id.tab_layout);
        viewpager = findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText("Login"));
        tabLayout.addTab(tabLayout.newTab().setText("Signup"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

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

       
        tabLayout.setTranslationY(300);


        tabLayout.setAlpha(v);

        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();



    }


}
//        mAuth = FirebaseAuth.getInstance();
//        emailField = findViewById(R.id.email);
//        passwordField = findViewById(R.id.password);
//        Button loginButton = findViewById(R.id.login);
//        Button forgotPasswordButton = findViewById(R.id.forgot_password);
//        Button registerButton = findViewById(R.id.register);
//
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String email = emailField.getText().toString();
//                String password = passwordField.getText().toString();
//                loginUser(email, password);
//            }
//        });
//
//        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent resetPasswordIntent = new Intent(LoginActivity.this,
//                        ResetPassActivity.class);
//                startActivity(resetPasswordIntent);
//            }
//        });
//
//
//        registerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent registrationIntent = new Intent(LoginActivity.this,
//                        RegisterActivity.class);
//                startActivity(registrationIntent);
//            }
//        });
//    }
//
//    private void loginUser(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            Toast.makeText(LoginActivity.this, "Authentication successful.",
//                                    Toast.LENGTH_SHORT).show();
//
//
//                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                            startActivity(intent);
//                            finish();
//
//                        } else {
//                            Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
//}
