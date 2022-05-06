package com.zodiac.in4chan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
import com.zodiac.in4chan.BackEnd.ConversationMessageList.ConversationListAdapter;
import com.zodiac.in4chan.BackEnd.Models.MessageModel;
import com.zodiac.in4chan.BackEnd.Services.DataContext;
import com.zodiac.in4chan.databinding.ActivityConversationBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation extends AppCompatActivity {

    private ActivityConversationBinding binding;
    private static final int DATABASE_VERSION = 2;
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Bundle bundle = getIntent().getExtras();
        String receiver = bundle.getString("receiver");
        String sender = bundle.getString("sender");
        String msg_table = "message_table";

        FirebaseDatabase database = FirebaseDatabase.getInstance(Tools.firebaseURL);
        documentReference = database.getReference(sender+"/messages");

        //Accessing Database
        DataContext dataContext = new DataContext(this,null,null,DATABASE_VERSION);
        sqLiteDatabase = dataContext.getWritableDatabase();
        List<MessageModel> messageModelList = dataContext.getAllChatMessages(sender, receiver);

        //Initialising Chat adapter
        ConversationListAdapter adapter = new ConversationListAdapter();
        RecyclerView recyclerView = binding.conversationRecyclerView;

        //sending message if the message is not empty
        if (!messageModelList.isEmpty()) {
            binding.noChatImg.setVisibility(View.GONE);
            Collections.reverse(messageModelList);
            adapter.setChatMessages(messageModelList, receiver);
            recyclerView.setAdapter(adapter);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,true));
        recyclerView.setAdapter(adapter);


        binding.messageInputLayout.sendMessage.setOnClickListener(view -> {
            String message = binding.messageInputLayout.inputMessageUser.getText().toString().trim();
            binding.messageInputLayout.inputMessageUser.getText().clear();
            if(!message.isEmpty()) {
                binding.noChatImg.setVisibility(View.GONE);
               MessageModel messageModel = new MessageModel();
               messageModel.setMessage(message);
               messageModel.setDelivery(false);
               messageModel.setRead(false);
               messageModel.setFilePath("None");
               messageModel.setReceiver(receiver);
               messageModel.setSender(sender);
               messageModel.setImagePath("none");
               messageModel.setTimestamp(System.currentTimeMillis());
               //adding to the view
               adapter.addMessage(messageModel);
               dataContext.insertMessage(messageModel);
               dataContext.updateTimestampOfUser(sender,messageModel.getTimestamp());

               //sending message to the server
               sendToServer(messageModel, msg_table, new getResult() {
                   @Override
                   public void onCallback(boolean value) {
                       if(value){
                           dataContext.setDeliveryUpdate(messageModel,value);
                           adapter.notifyItemChanged(messageModelList.size()-1);
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
        documentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed()
    {
      Intent i = new Intent(this, FriendsList.class);
        startActivity(i);
        finish();
    }

    public interface getResult{
        void onCallback(boolean value);
    }

    public void sendToServer(MessageModel messageModel, String msg, final getResult callback){
        String query = Tools.getQueryFromModel(messageModel, msg);
        Log.i("sendToServer",query);

        documentReference.push().setValue(query).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        return super.onOptionsItemSelected(item);
    }
}