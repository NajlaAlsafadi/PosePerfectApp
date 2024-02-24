package com.example.poseperfect.baseUI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import com.example.poseperfect.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

public class SignupTabFragment  extends Fragment {
    EditText email;
    EditText password;
    EditText confirmPass;
    TextView password_details;
    EditText username;
    Button signup;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    float v=0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_fragment, container, false);
        password_details = root.findViewById(R.id.password_details);
        email = root.findViewById(R.id.email);
        password = root.findViewById(R.id.password);
        confirmPass = root.findViewById(R.id.confirm_password);
        username = root.findViewById(R.id.username);
        signup = root.findViewById(R.id.signup);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        email.setTranslationX(800);
        password_details.setTranslationX(800);
        password.setTranslationX(800);
        confirmPass.setTranslationX(800);
        username.setTranslationX(800);
        signup.setTranslationX(800);

        email.setAlpha(v);
        password_details.setAlpha(v);
        password.setAlpha(v);
        confirmPass.setAlpha(v);
        username.setAlpha(v);
        signup.setAlpha(v);



        email.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();

        username.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        password.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        confirmPass.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        password_details.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(800).start();
        signup.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailValue = email.getText().toString();
                String passwordValue = password.getText().toString();
                String passwordConfirm = confirmPass.getText().toString();
                String usernameValue = username.getText().toString();

                if (!passwordValue.equals(passwordConfirm)) {
                    Toast.makeText(getActivity(), "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // check if the password is strong
                    String passwordStrength = isPasswordStrong(passwordValue);
                    if (!passwordStrength.equals("strong")) {
                        Toast.makeText(getActivity(), "Password is not strong. "
                                + passwordStrength, Toast.LENGTH_SHORT).show();
                    } else {
                        signupUser(emailValue, passwordValue, usernameValue);
                    }
                }
            }
        });

        return root;
    }
private void signupUser(String email, String password, String username) {

    firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        String userID = user.getUid();

                        User newUser = new User(username, email);
                        databaseReference.child("users").child(userID).setValue(newUser);

                        // sends verification email
                        user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Verification email sent to " + user.getEmail(),
                                                    Toast.LENGTH_SHORT).show();
                                            SignupTabFragment.this.email.setText("");
                                            SignupTabFragment.this.password.setText("");
                                            SignupTabFragment.this.confirmPass.setText("");
                                            SignupTabFragment.this.username.setText("");
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        Toast.makeText(getActivity(), "User created successfully! Please check your email for verification.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
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


}
