package com.imagesharer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    // fields
    private FirebaseAuth mAuth;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        // set up action bar
        actionBar = getSupportActionBar();
        // init firebase auth
        mAuth = FirebaseAuth.getInstance();
        // init naviagation bar
        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        //default nav bar on start
        actionBar.setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.dashboardActivityLayout, homeFragment, "");
        transaction.commit();
    }

    //get navigation bar click listener
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // three possible navigation bar items
            switch(item.getItemId()){
                case R.id.nav_home:
                    // set action bar
                    actionBar.setTitle("Home");
                    // initialize and start home activity fragment
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                    transaction1.replace(R.id.dashboardActivityLayout, homeFragment, "");
                    transaction1.commit();
                    return true;
                case R.id.nav_profile:
                    // initialize and start images activity fragment
                    actionBar.setTitle("Images");
                    ImagesFragment imagesFragment = new ImagesFragment();
                    FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                    transaction2.replace(R.id.dashboardActivityLayout, imagesFragment, "");
                    transaction2.commit();
                    return true;
                case R.id.nav_users:
                    // initialize and start users activity fragment
                    actionBar.setTitle("Users");
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
                    transaction3.replace(R.id.dashboardActivityLayout, usersFragment, "");
                    transaction3.commit();
                    return true;
            }
            return false;
        }
    };

    // method to make sure user is authenticated
    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    // check user status on start of the activity
    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
}