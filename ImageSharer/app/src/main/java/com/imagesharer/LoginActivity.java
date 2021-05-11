package com.imagesharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // views
    private EditText mEmailET, mPasswordET;
    private Button mLoginBtn;
    private ProgressDialog progressDialog;
    private TextView mNoAccountTV, mforgottenPasswordTV;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Action bar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        // enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //initialise views
        mEmailET = findViewById(R.id.emailET);
        mPasswordET = findViewById(R.id.passwordET);
        mLoginBtn = findViewById(R.id.loginBtn);
        mNoAccountTV = findViewById(R.id.noAccountTV);
        mforgottenPasswordTV = findViewById(R.id.forgottenPasswordTV);

        //initialise Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        //initialize progress dialog
        progressDialog = new ProgressDialog(this);

        // Setup login button click listener
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get user details from user input
                String email = mEmailET.getText().toString().trim();
                String password = mPasswordET.getText().toString().trim();
                // make sure email is in right format
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailET.setError("Invalid Email");
                    mEmailET.setFocusable(true);
                }
                else{
                    // login user
                    loginUser(email, password);
                }
            }
        });

        // handle textview that is clicked when user is not registered yet
        mNoAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start register activity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        // handle textview that is clicked when user forgot his password
        mforgottenPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordRecoveryDialog();
            }
        });
    }

    // method gets users email in the new dialog and send password recovery email
    private void showPasswordRecoveryDialog()
    {
        // initialize dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        // set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        // initialize input view
        final EditText emailET = new EditText(this);
        emailET.setHint("Email");
        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailET.setMinEms(10);
        linearLayout.addView(emailET);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);


        // button on the right sends email to given email for password recovery
        builder.setPositiveButton("Send email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailET.getText().toString().trim();
                recoverPassword(email);
            }
        });

        // button on the left dismisses dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dismiss dialog
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    // method send email for password recovery
    private void recoverPassword(String email){
        progressDialog.setMessage("Sending password reset email ... ");
        progressDialog.show();
        // firebase auth sends email of password recovery to given email and listens for success or failure
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }else
                {
                    Toast.makeText(LoginActivity.this, "Sending password reset email failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage().toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    // method uses firebase auth to sign-in user
    private void loginUser(String email, String password){
        progressDialog.setMessage("Logging In ... ");
        progressDialog.show();
        // firebase auth signs in user with a given email and listens for success or failure
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    // start main activity
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        //go to previous activity
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}