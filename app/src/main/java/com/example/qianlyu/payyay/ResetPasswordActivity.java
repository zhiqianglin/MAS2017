package com.example.qianlyu.payyay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        String emailAddress = getIntent().getStringExtra("emailAddress");
        System.out.println("email in reset password: " + emailAddress);
        auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(emailAddress);
    }
}
