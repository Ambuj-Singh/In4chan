package com.zodiac.in4chan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zodiac.in4chan.BackEnd.FriendsList.FriendsListAdapter;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;
import com.zodiac.in4chan.BackEnd.Services.DataContext;
import com.zodiac.in4chan.BackEnd.UserStatus.Status_Offline;
import com.zodiac.in4chan.BackEnd.UserStatus.Status_Online;
import com.zodiac.in4chan.databinding.ActivityFriendsListBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FriendsList extends AppCompatActivity implements FriendsListAdapter.Interaction {

    private ActivityFriendsListBinding binding;
    private DataContext dataContext;
    private OneTimeWorkRequest oneTimeWorkRequestA, oneTimeWorkRequestB;
    private List<UserInfo> users;
    private  FirebaseFirestore db;
    private Data data_online;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        dataContext = new DataContext(this);
        data_online = new Data.Builder()
                .putString("status","UserStatus")
                .build();

        db = FirebaseFirestore.getInstance();

        setStatusOnline();

        users = dataContext.getAllUsers();
        binding.noFriendsImg.setVisibility(View.VISIBLE);
        if(users.isEmpty()){
           binding.noFriendsImg.setVisibility(View.VISIBLE);
        }
        else {
            binding.noFriendsImg.setVisibility(View.GONE);
            RecyclerView recyclerView = binding.recyclerViewFriendsList;
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            FriendsListAdapter adapter = new FriendsListAdapter(users, this);
            recyclerView.setAdapter(adapter);
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.browse_profiles_friends){
            browseProfiles();
            return true;
        }
        else if(item.getItemId() == R.id.search_menu_friends){
            //searchFriends();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    public void browseProfiles(){
        Intent i = new Intent(this,ServerUsers.class);
        startActivity(i);
    }

    //Updating user online status
    @Override
    protected void onResume() {
        super.onResume();

        if(oneTimeWorkRequestA!=null) {
            UUID getID = oneTimeWorkRequestA.getId();
            WorkManager.getInstance(getApplicationContext()).cancelWorkById(getID);
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        oneTimeWorkRequestB = new OneTimeWorkRequest.Builder(Status_Online.class)
                .setConstraints(constraints)
                .addTag("Status_Update")
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequestB); }

    @Override
    protected void onStart() {
        super.onStart();

        if(oneTimeWorkRequestA!=null) {
            UUID getID = oneTimeWorkRequestA.getId();
            WorkManager.getInstance(getApplicationContext()).cancelWorkById(getID);
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        oneTimeWorkRequestB = new OneTimeWorkRequest.Builder(Status_Online.class)
                .setConstraints(constraints)
                .addTag("Status_Update")
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequestB);
    }


    @Override
    public void onChatClicked(int position) {
        String receiver = users.get(position).getUsername();
        Log.i("result_insert_list", receiver);
        Intent i = new Intent(FriendsList.this, Conversation.class);
        String sender = dataContext.getUsername();
        i.putExtra("sender",sender);
        i.putExtra("receiver", receiver);
        Log.i("sender_server",sender);
        startActivity(i);
    }

    @Override
    public void onChatLongClicked(int position) {

    }

    public void setStatusOnline() {
        Log.i("status","running");
        CollectionReference collectionReference = db.collection("users");
        DocumentReference df = collectionReference.document(dataContext.getUID());

            Map<String, Object> status = new HashMap<>();
            status.put("UserStatus",true);
            df.update(status).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("User_status_new","online");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("User_status","auth failed");
                }
            });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(oneTimeWorkRequestB!=null) {
            UUID getID = oneTimeWorkRequestB.getId();
            WorkManager.getInstance(getApplicationContext()).cancelWorkById(getID);
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        oneTimeWorkRequestA = new OneTimeWorkRequest.Builder(Status_Offline.class)
                .setConstraints(constraints)
                .addTag("Status_Update")
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequestA);
    }


}

