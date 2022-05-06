package com.zodiac.in4chan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.zodiac.in4chan.BackEnd.ChatList.ChatListAdapter;
import com.zodiac.in4chan.BackEnd.FriendsList.FriendsListAdapter;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;
import com.zodiac.in4chan.BackEnd.Services.DataContext;
import com.zodiac.in4chan.BackEnd.Status_Offline;
import com.zodiac.in4chan.BackEnd.Status_Online;
import com.zodiac.in4chan.databinding.ActivityFriendsListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsList extends AppCompatActivity implements FriendsListAdapter.Interaction {

    private ActivityFriendsListBinding binding;
    private DataContext dataContext;
    private OneTimeWorkRequest oneTimeWorkRequestA, oneTimeWorkRequestB;
    private List<UserInfo> users;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        dataContext = new DataContext(this,null,null,2);
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
        finish();
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
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequestB);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    protected void onDestroy() {
        super.onDestroy();
        if (oneTimeWorkRequestB != null) {
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

    @Override
    public void onChatClicked(int position) {
        String receiver = users.get(position).getUsername();
        Intent i = new Intent(FriendsList.this, Conversation.class);

        i.putExtra("sender","Overlord");
        i.putExtra("receiver", receiver);
        Log.i("prefrences","Overlord"+" "+receiver);
        startActivity(i);
        finish();
    }

    @Override
    public void onChatLongClicked(int position) {

    }
}
