package com.imagesharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    private EditText mNameET, mEmailET, mPasswordET;
    private Button mRegisterBtn;
    private ProgressDialog progressDialog;
    private TextView mHaveAccountTV;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //initialize views
        mNameET = findViewById(R.id.nameET);
        mEmailET = findViewById(R.id.emailET);
        mPasswordET = findViewById(R.id.passwordET);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTV = findViewById(R.id.haveAccountTV);
        // set progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering ...");
        // initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // handle register button listener
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // data from user input
                String name = mNameET.getText().toString().trim();
                String email = mEmailET.getText().toString().trim();
                String password = mPasswordET.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    // make sure input is in email format
                    mEmailET.setError("Invalid Email");
                    mEmailET.setFocusable(true);
                }
                else if(password.length() < 6){
                    // make sure password is of sufficient length
                    mPasswordET.setError("Password length should be at least 6 characters");
                    mEmailET.setFocusable(true);
                }
                else{
                    //register user with given details
                   registerUser(name, email, password);
                }
            }
        });

        // handle textview that is clicked when user already has an account
        mHaveAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start login activity
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    // method register users using Firebase Auth API
    private void registerUser(String name, String email, String password){
        // show progress dialog
        progressDialog.show();
        // firebase creates new user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // close progress dialog
                            progressDialog.dismiss();
                            // get registered user
                            FirebaseUser user = mAuth.getCurrentUser();
                            // get user details
                            String email = user.getEmail();
                            String uid = user.getUid();
                            // build a hashmap to store user details according to ModelUser class
                            HashMap<Object, String> usersData = new HashMap<>();
                            usersData.put("email", email);
                            usersData.put("uid", uid);
                            usersData.put("name", name);
                            usersData.put("image", "");
                            // get firebase instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            // get reference to database section where users details will be stored
                            DatabaseReference reference = database.getReference("Users");
                            // save user in the database refernce specified
                            reference.child(uid).setValue(usersData);
                            // notify user on task success
                            Toast.makeText(RegisterActivity.this, "Registered \n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            // start main dashboard activity
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            // finish current activity
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            // close progress dialog
                            progressDialog.dismiss();
                            // notify user on task failure
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // close progress dialog
                progressDialog.dismiss();
                // notify user on task failure and get error message
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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