package com.zodiac.in4chan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.zodiac.in4chan.BackEnd.ConversationMessageList.ConversationListAdapter;
import com.zodiac.in4chan.BackEnd.Models.MessageModel;
import com.zodiac.in4chan.BackEnd.Services.DataContext;
import com.zodiac.in4chan.BackEnd.Services.Encryption;
import com.zodiac.in4chan.databinding.ActivityConversationBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation extends AppCompatActivity {

    private ActivityConversationBinding binding;
    private static final int DATABASE_VERSION = 2;
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseReference documentReference;
    private Data data;
    private String receiver, sender;
    private DataContext dataContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        data = new Data.Builder()
                .putString("status", "UserStatusCon")
                .build();

        Bundle bundle = getIntent().getExtras();
        receiver = bundle.getString("receiver");
        Log.i("result_insert_bundle", receiver);
        sender = bundle.getString("sender");
        String msg_table = "message_table";

        FirebaseDatabase database = FirebaseDatabase.getInstance(Tools.firebaseURL);
        documentReference = database.getReference(receiver + "/messages");
        DatabaseReference reference = database.getReference(sender + "/messages");

        //Accessing Database
        dataContext = new DataContext(this);
        sqLiteDatabase = dataContext.getWritableDatabase();
        List<MessageModel> messageModelList = dataContext.getAllChatMessages(sender, receiver);
//        Log.i("Messages",messageModelList.get(0).getMessage()+","+messageModelList.get(1).getMessage());
        //Initialising Chat adapter
        ConversationListAdapter adapter = new ConversationListAdapter();
        RecyclerView recyclerView = binding.conversationRecyclerView;

        //sending message if the message is not empty
        if (!messageModelList.isEmpty()) {
            binding.noChatImg.setVisibility(View.GONE);
            adapter.setChatMessages(messageModelList, receiver);
            recyclerView.setAdapter(adapter);
            recyclerView.smoothScrollToPosition(messageModelList.size()-1);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        binding.messageInputLayout.sendMessage.setOnClickListener(view -> {
            String message = binding.messageInputLayout.inputMessageUser.getText().toString().trim();
            binding.messageInputLayout.inputMessageUser.getText().clear();
            if (!message.isEmpty()) {
                binding.noChatImg.setVisibility(View.GONE);
                MessageModel messageModel = new MessageModel();
                messageModel.setMessage(message);
                messageModel.setDelivery(false);
                messageModel.setRead(false);
                messageModel.setFilePath("None");
                messageModel.setReceiver(receiver);
                Log.i("result_insert_chat", receiver);
                messageModel.setSender(sender);
                messageModel.setImagePath("none");
                messageModel.setTimestamp(System.currentTimeMillis());
                //adding to the view
                adapter.addMessage(messageModel);
                recyclerView.smoothScrollToPosition(messageModelList.size());
                dataContext.insertMessage(messageModel);
                dataContext.updateTimestampOfUser(sender, messageModel.getTimestamp());

                //sending message to the server
                sendToServer(messageModel, msg_table, new getResult() {
                    @Override
                    public void onCallback(boolean value) {
                        if (value) {
                            dataContext.setDeliveryUpdate(messageModel, value);
                            List<MessageModel> list = dataContext.getAllChatMessages(sender, receiver);
                            adapter.setChatMessages(list, receiver);
                            recyclerView.smoothScrollToPosition(list.size()-1);
                        }
                    }
                });
            }
        });


   /*     binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });*/

        //mark read
       /* documentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                GenericTypeIndicator<String> genericTypeIndicator = new GenericTypeIndicator<String>() {
                };

                /* Map<String,String> map = snapshot.getValue(genericTypeIndicator);*/

                String message = snapshot.getValue(genericTypeIndicator);


                reference.child(snapshot.getKey()).removeValue();

                Encryption encryption = new Encryption();

                String decryptedMessage = encryption.decrypt(message,Tools.secret);

                sqLiteDatabase.execSQL(decryptedMessage);
                List<MessageModel> list = dataContext.getAllChatMessages(sender, receiver);
                binding.noChatImg.setVisibility(View.GONE);
                adapter.setChatMessages(list, receiver);

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

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, FriendsList.class);
        startActivity(i);
        finish();
    }

    public interface getResult {
        void onCallback(boolean value);
    }

    public void sendToServer(MessageModel messageModel, String msg, final getResult callback) {
        String query = Tools.getQueryFromModel(messageModel, msg);
        String encryptedQuery = new Encryption().encrypt(query,Tools.secret);
        Log.i("sendToServer", query);
        Map<Long,String> map = new HashMap<>();
        map.put(messageModel.getTimestamp(),encryptedQuery);
        documentReference.push().setValue(encryptedQuery).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callback.onCallback(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onCallback(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.toolbar_delete) {
            dataContext.clearChat(sender, receiver);
            Toast.makeText(this, "Chat Cleared", Toast.LENGTH_SHORT).show();
            return true;
        } else return super.onOptionsItemSelected(item);
    }
}
