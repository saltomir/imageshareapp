package com.imagesharer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.imagesharer.R;
import com.imagesharer.models.ModelMessage;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ChatAdapter extends  RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    // fields
    Context context;
    private List<ModelMessage> chatList;
    private FirebaseAuth mAuth;

    // constructor
    public ChatAdapter(Context context, List<ModelMessage> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    // nested class that holds recyclerview views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //views
        TextView senderMessageTV,senderMessageTimeTV, receiverMessageTV, receiverMessageTimeTV;
        ImageView senderImageMessageIV, receiverImageMessageIV;
        // constructor
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            // init views
            senderMessageTV = itemView.findViewById(R.id.senderMessageTV);
            senderMessageTimeTV = itemView.findViewById(R.id.senderMessageTimeTV);
            senderImageMessageIV = itemView.findViewById(R.id.senderImageMessageIV);
            receiverMessageTV = itemView.findViewById(R.id.receiverMessageTV);
            receiverMessageTimeTV = itemView.findViewById(R.id.receiverMessageTimeTV);
            receiverImageMessageIV = itemView.findViewById(R.id.receiverImageMessageIV);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout of the chat messages entry
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_messages, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get currently signed in user
        String currentUid = mAuth.getCurrentUser().getUid();
        // get an element from given list of messages
        ModelMessage chat = chatList.get(position);
        // initialize values of the message object
        String senderUid = chat.getSender();
        String message = chat.getMessage();
        String timeStamp = chat.getTimestamp();
        String messageType = chat.getType();

        // remove all views elements
        holder.receiverImageMessageIV.setVisibility(View.GONE);
        holder.receiverMessageTV.setVisibility(View.GONE);
        holder.receiverMessageTimeTV.setVisibility(View.GONE);
        holder.senderImageMessageIV.setVisibility(View.GONE);
        holder.senderMessageTV.setVisibility(View.GONE);
        holder.senderMessageTimeTV.setVisibility(View.GONE);

        // if message type is text
        if (messageType.equals("text")){
            // if text sender is currently signed in user
            if(senderUid.equals(currentUid)){
                //remove image views of both sender and receiver
                holder.senderImageMessageIV.setVisibility(View.GONE);
                holder.receiverImageMessageIV.setVisibility(View.GONE);
                // set sender message views
                holder.senderMessageTV.setVisibility(View.VISIBLE);
                holder.senderMessageTimeTV.setVisibility(View.VISIBLE);
                holder.senderMessageTV.setText(message);
                holder.senderMessageTimeTV.setText(timeStamp);
                // remove receiver message views
                holder.receiverMessageTV.setVisibility(View.GONE);
                holder.receiverMessageTimeTV.setVisibility(View.GONE);
            }
            else {
                // remove text views of both sender and receiver
                holder.senderImageMessageIV.setVisibility(View.GONE);
                holder.receiverImageMessageIV.setVisibility(View.GONE);
                //make sure sender's messages are gone
                holder.senderMessageTV.setVisibility(View.GONE);
                holder.senderMessageTimeTV.setVisibility(View.GONE);
                //set the receiver user's message text, timestamp and make visible
                holder.receiverMessageTV.setVisibility(View.VISIBLE);
                holder.receiverMessageTimeTV.setVisibility(View.VISIBLE);
                holder.receiverMessageTV.setText(message);
                holder.receiverMessageTimeTV.setText(timeStamp);
            }
        } else if (messageType.equals("image"))// if message is image
        {
            if(senderUid.equals(currentUid))
            {
                // make sender image view visible
                holder.senderImageMessageIV.setVisibility(View.VISIBLE);
                holder.senderMessageTimeTV.setText(timeStamp);
                // remove receiver views
                holder.receiverImageMessageIV.setVisibility(View.GONE);
                holder.receiverMessageTV.setVisibility(View.GONE);
                holder.receiverMessageTimeTV.setVisibility(View.GONE);
                // draw picture using Picasso
                Picasso.get().load(message).into(holder.senderImageMessageIV);
            }else
            {
                // make receiver image view visible
                holder.receiverImageMessageIV.setVisibility(View.VISIBLE);
                holder.receiverMessageTimeTV.setText(timeStamp);
                // remove sender views
                holder.senderImageMessageIV.setVisibility(View.GONE);
                holder.senderMessageTV.setVisibility(View.GONE);
                holder.senderMessageTimeTV.setVisibility(View.GONE);
                // draw picture using Picasso
                Picasso.get().load(message).into(holder.receiverImageMessageIV);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

}
