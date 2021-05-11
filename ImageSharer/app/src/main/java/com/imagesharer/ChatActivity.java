package com.imagesharer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.imagesharer.adapters.ChatAdapter;
import com.imagesharer.models.ModelMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    // iviews
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView nameTV;
    private EditText messageET;
    private ImageButton sendBtn, sendImageBtn;

    // users data
    private String receiverUid, receiverName, senderUid;

    // firebase api
     private FirebaseAuth mAuth;
     private FirebaseDatabase firebaseDatabase;
     private DatabaseReference usersDatabaseReference;

    private ChatAdapter chatAdapter;
    private List<ModelMessage> mChat;
    private String saveCurrentDateTime;
    // fieds used for image sending
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageTask mUploadTask;
    private  String imageUrl;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // auhenticate user
        mAuth = FirebaseAuth.getInstance();
        senderUid = mAuth.getCurrentUser().getUid();
        //set top toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // initialize views
        nameTV = findViewById(R.id.nameTV);
        messageET = findViewById(R.id.messageET);
        sendBtn = findViewById(R.id.sendBtn);
        sendImageBtn = findViewById(R.id.sendImageBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image ...");
        //Get receiver users id and name, since we passed them using intent form UsersFragment
        receiverName = getIntent().getExtras().get("userName").toString();
        receiverUid = getIntent().getExtras().get("userUid").toString();
        // initialize list containing messages objects
        mChat = new ArrayList<>();
        // initialize adapter of messages objects
        chatAdapter = new ChatAdapter(ChatActivity.this, mChat);
        // setup layout
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //initialize recycler view that holds list of messages
        recyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        // set adapter of the messages on the recyclerview
        recyclerView.setAdapter(chatAdapter);
        // initialize firebase database instance of this app
        firebaseDatabase = FirebaseDatabase.getInstance();
        // get current time to timestamp messages
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateTime = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        saveCurrentDateTime = currentDateTime.format(calendar.getTime());
        // Get the receivers name from database
        usersDatabaseReference = firebaseDatabase.getReference("Users");
        Query userQuery = usersDatabaseReference.orderByChild("uid").equalTo(receiverUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check until required info is received
                for (DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    //set data in view
                    nameTV.setText(name);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // listen to send button clicks
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  get text from message text input view
                String message = messageET.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(message!=null)
                    {
                        sendMessage(message);
                    }
                }
            }
        });

        readMessages();

        // listen for image button click
        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(ChatActivity.this, "Sending image is in progress", Toast.LENGTH_SHORT).show();
                } else {
                    // create intent and that opens local image data  and start the activity
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            }
        });
    }

    // when image was picked from local storage
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            // get resource uri
            mImageUri = data.getData();
            // send image
            sendImageMessage();
        }
    }

    // method tp send an image
    private void sendImageMessage() {
        if(mImageUri != null)
        {
            //create storage reference
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ChatImages");
            // create database paths for boh sender and receiver
            final String messageSenderRef = "Chats/" + senderUid + "/" + receiverUid;
            final String messageReceiverRef = "Chats/" + receiverUid + "/" + senderUid;
            // create data entry key for the currently signed in user which is the sender
            DatabaseReference messageKeyDatabaseReference = FirebaseDatabase.getInstance()
                    .getReference().child("Chats")
                    .child(senderUid).child(receiverUid).push();
            // get key of the data entry
            final String messagePushID = messageKeyDatabaseReference.getKey();
            // create path to the image in storage
            StorageReference filePath = storageReference.child(messagePushID+"." +"jpg");
            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();
            progressDialog.show();
            // upload the image with given uri and metadata
            mUploadTask = filePath.putFile(mImageUri, metadata);
            // get download url of the upload
            Task<Uri> urlTask = mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // get the download url
                        Uri downloadUri = task.getResult();
                        imageUrl = downloadUri.toString();
                        // create hashmap that holds image message data
                        final Map messageImageBody = new HashMap();
                        messageImageBody.put("message", imageUrl);
                        messageImageBody.put("type", "image");
                        messageImageBody.put("sender", senderUid);
                        messageImageBody.put("timestamp", saveCurrentDateTime);
                        // upload message data entry for both sender and receiver
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                        childUpdates.put(messageReceiverRef + "/" + messagePushID, messageImageBody);
                        firebaseDatabase.getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if (task.isSuccessful())
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        // Handle failures
                        Toast.makeText(ChatActivity.this, "Error getting image Url", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // method to send text message
    private void sendMessage(String message){
        // create database paths for boh sender and receiver
        String messageSenderRef = "Chats/" + senderUid + "/" + receiverUid;
        String messageReceiverRef = "Chats/" + receiverUid + "/" + senderUid;
        DatabaseReference messageKeyDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Chats")
                .child(senderUid).child(receiverUid).push();
        String messagePushID = messageKeyDatabaseReference.getKey();

        if(!message.isEmpty()) {
            final Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("sender", senderUid);
            messageTextBody.put("timestamp", saveCurrentDateTime);
            // upload message data entry for both sender and receiver
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            childUpdates.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
            firebaseDatabase.getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
            // reset message text input viewer
            messageET.setText("");
    }

    // read messages from database
    private void readMessages() {
        // get reference to database where messages are stored
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Chats");
        //listen for childs updates
        databaseReference.child(mAuth.getCurrentUser().getUid()).child(receiverUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // get object of image from database
                ModelMessage chat = snapshot.getValue(ModelMessage.class);
                // update list of messages and notifu adapter
                mChat.add(chat);
                linearLayoutManager.scrollToPosition(mChat.size()-1);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }// end method readMessages

    // // method to make sure user is authenticated
    private void checkUserStatus(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //go to previous activity
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}