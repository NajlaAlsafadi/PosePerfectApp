package com.example.poseperfect.baseUI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import com.example.poseperfect.R;

public class SignupTabFragment  extends Fragment {
    EditText email;
    EditText password;
    EditText confirmPass;
    EditText username;
    Button signup;
    float v=0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_fragment, container, false);
        email = root.findViewById(R.id.email);
        password = root.findViewById(R.id.password);
        confirmPass = root.findViewById(R.id.confirm_password);
        username = root.findViewById(R.id.username);
        signup = root.findViewById(R.id.signup);

        email.setTranslationX(800);
        password.setTranslationX(800);
        confirmPass.setTranslationX(800);
        username.setTranslationX(800);
        signup.setTranslationX(800);

        email.setAlpha(v);
        password.setAlpha(v);
        confirmPass.setAlpha(v);
        username.setAlpha(v);
        signup.setAlpha(v);



        email.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        username.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        password.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        confirmPass.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        signup.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();

        return root;
    }

}
