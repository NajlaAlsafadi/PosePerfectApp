package com.example.poseperfect.baseUI;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.poseperfect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextInputEditText passwordConfirmField;


    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        passwordConfirmField = findViewById(R.id.passwordConfirm);
        Button registerButton = findViewById(R.id.register);
        Button backToLoginButton = findViewById(R.id.backToLoginButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                String passwordConfirm = passwordConfirmField.getText().toString();

                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // check if the password is strong
                    String passwordStrength = isPasswordStrong(password);
                    if (!passwordStrength.equals("strong")) {
                        Toast.makeText(RegisterActivity.this, "Password is not strong. "
                                + passwordStrength, Toast.LENGTH_SHORT).show();
                    } else {
                        registerUser(email, password);
                    }
                }
            }
        });

        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private String isPasswordStrong(String password) {
        if (password.length() < 8) {
            return "Password needs to be at least 8 characters.";
        }
        if (!password.matches("(.*[A-Za-z].*)")) {
            return "Password needs to contain at least one letter.";
        }
        if (!password.matches("(.*[0-9].*)")) {
            return "Password needs to contain at least one number.";
        }
        return "strong";
    }

    private void registerUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                databaseReference.child("users").child(user.getUid())
                                        .setValue(email);
                                Toast.makeText(RegisterActivity.this,
                                        "Registration successful.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}