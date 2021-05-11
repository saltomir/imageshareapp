package com.imagesharer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //views
    private Button mRegisterBtn, mSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize views
        mRegisterBtn = findViewById(R.id.register_btn);
        mSignInBtn = findViewById(R.id.signin_btn);

        //handle register button click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start register activity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}