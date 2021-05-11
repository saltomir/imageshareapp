package com.imagesharer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imagesharer.ChatActivity;
import com.imagesharer.R;
import com.imagesharer.models.ModelUser;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    //fields
    Context context;
    private List<ModelUser> modelUserList;

    //constructor
    public UsersAdapter(Context context, List<ModelUser> modelUserList)
    {
        this.context = context;
        this.modelUserList = modelUserList;
    }

    //nested class that holds recyclerview views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //fields
        TextView mNameTV;
        TextView mEmailTV;
        //constructor
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            // init views
            mNameTV = itemView.findViewById(R.id.rowNameTV);
            mEmailTV = itemView.findViewById(R.id.rowEmailTV);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate view
         View view = LayoutInflater.from(parent.getContext()
         ).inflate(R.layout.row_users, parent, false);
         return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data of the object from the list
        String userUid = modelUserList.get(position).getUid();
        String userEmail = modelUserList.get(position).getEmail();
        String userName = modelUserList.get(position).getName();
        // set views with the fetched data
        holder.mEmailTV.setText(userEmail);
        holder.mNameTV.setText(userName);

        // listen to clicks of on a user item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // initialize intent
                Intent intent = new Intent(context, ChatActivity.class);
                // provide user some data for the chat activity
                intent.putExtra("userName", userName);
                intent.putExtra("userUid", userUid);
                // start chat activity
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return modelUserList.size();
    }
}
