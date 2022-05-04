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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation extends AppCompatActivity {

    private ActivityConversationBinding binding;
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase;
    private List<MessageModel> messageModelList;
    private DatabaseReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //saving writes to cache when user goes offline or due to a network interruption
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String receiver = bundle.getString("receiver");
        String sender = bundle.getString("sender");
        String msg_table = "message_table";

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        documentReference = database.getReference(sender+"/messages");

        //Initialising Chat adapter
        ConversationListAdapter adapter = new ConversationListAdapter();
        RecyclerView recyclerView = binding.conversationRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,true));
        recyclerView.setAdapter(adapter);

        //Accessing Database
        DataContext dataContext = new DataContext(this,null,null,DATABASE_VERSION);
        sqLiteDatabase = dataContext.getWritableDatabase();
        messageModelList = dataContext.getAllChatMessages(sender,receiver);

        //sending message if the message is not empty
        if (!messageModelList.isEmpty())
            adapter.setChatMessages(messageModelList,receiver);

        binding.messageInputLayout.sendMessage.setOnClickListener(view -> {
            String message = binding.messageInputLayout.inputMessageUser.toString().trim();
           if(!message.isEmpty()) {
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
                           String query = "update message_table set delivery = "+Tools.booleanToInteger(value)+" where timestamp = "+messageModel.getTimestamp()+";";
                           sqLiteDatabase.execSQL(query);
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
      /*Intent i = new Intent(this, ChatList.class);
        startActivity(i);
        finish();*/
    }

    public interface getResult{
        void onCallback(boolean value);
    }

    public void sendToServer(MessageModel messageModel, String msg, final getResult callback){
        String query = Tools.getQueryFromModel(messageModel, msg);
        Log.i("sendToServer",query);

        documentReference.push().setValue(messageModel.getTimestamp(),query).addOnSuccessListener(new OnSuccessListener<Void>() {
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



}