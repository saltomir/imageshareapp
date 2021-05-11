package com.imagesharer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imagesharer.adapters.ImageAdapter;
import com.imagesharer.models.Upload;

import java.util.ArrayList;
import java.util.List;

public class ImagesFragment extends Fragment {
    // fields
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize image list recyclerview
        recyclerView = view.findViewById(R.id.imagesRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // initialize progress tracker
        mProgressCircle = view.findViewById(R.id.progress_circle);
        // initialize image uploads list
        mUploads = new ArrayList<>();
        // initialize adapter for the recycler view of the image uploads list
        mAdapter = new ImageAdapter(getActivity(), mUploads);
        // show images
        getImages();
    }

    // method to upload user uploaded images
    private void getImages() {
        // get the reference to the realtime database path where image uploads of the current user were stored
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Images").child(mAuth.getCurrentUser().getUid());
        // listen for data entries updates
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                // iterate through all data entries
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // get object of Upload class from data snapshot
                    Upload upload = ds.getValue(Upload.class);
                    // add the upload to the list
                    mUploads.add(upload);
                }
                // set the adapter for recycler view
                recyclerView.setAdapter(mAdapter);
                // dismiss the progress circle
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Shoe error message
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }
}