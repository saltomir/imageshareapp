package com.imagesharer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imagesharer.adapters.UsersAdapter;

import com.imagesharer.models.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    // fields
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<ModelUser> userList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        // authenticate the user
        mAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize user list recyclerview
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // initialize user objects list
        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(getActivity(), userList);
        // get all registered users
         getAllUsers();
    }

    // method get and display them
    private void getAllUsers(){
        // get currently authenticated user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // get database reference to the users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        // listen to changes within the users database path updates
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                // iterate through all data entries
                for (DataSnapshot ds: snapshot.getChildren()){
                    //and get users as ModelUser object
                     ModelUser modelUser = ds.getValue(ModelUser.class);
                     if (!modelUser.getUid().equals(user.getUid())){
                         // add all users except the currently logged in user
                         userList.add(modelUser);
                     }
                     // set adapter for the recycler view
                     recyclerView.setAdapter(usersAdapter);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // method to search a specific user by given query string
    private void searchUsers(String query){
        // get currently logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // get database path to the users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        // listen for data entries updates
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                // iterate through entries
                for (DataSnapshot ds: snapshot.getChildren()){
                    //and get users as ModelUser object
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    // make sure not searching for ourselves
                    if (!modelUser.getUid().equals(user.getUid())){
                        // see if query satisfies any of the entries
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                            modelUser.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            // add user object to the list
                            userList.add(modelUser);
                        }
                    }
                    // notify with change of user list
                    usersAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // show top left corner menu with search and logout options
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate menu
        inflater.inflate(R.menu.menu_main, menu);
        // get search action
        MenuItem item = menu.findItem(R.id.action_searchUsers);
        // initialize search view
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        // listen for input queries
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())){
                    // get query and search users
                    searchUsers(query);
                }
                else {
                    // query is empty, fetch all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())){
                    //get updated query and search users
                    searchUsers(newText);
                }
                else {
                    // query is empty, fetch all users
                    getAllUsers();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // handle logout action from the menu
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // sign-out current user
            mAuth.signOut();
            // check user status
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    // method to make sure user is authenticated
    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}