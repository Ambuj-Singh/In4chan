package com.zodiac.in4chan;

import static org.threeten.bp.Period.between;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zodiac.in4chan.BackEnd.ChatList.ChatListAdapter;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.zodiac.in4chan.BackEnd.Services.DataContext;
import com.zodiac.in4chan.BackEnd.UserStatus.Status_Offline;
import com.zodiac.in4chan.BackEnd.UserStatus.Status_Online;
import com.zodiac.in4chan.databinding.ActivityChatListBinding;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChatList extends AppCompatActivity {

    private LocalDate date;
    private OneTimeWorkRequest oneTimeWorkRequestA, oneTimeWorkRequestB;
    private DataContext dataContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        com.zodiac.in4chan.databinding.ActivityChatListBinding binding = ActivityChatListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        saveLatestAge(db);

        RecyclerView recyclerView = binding.recyclerViewChatList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dataContext = new DataContext(this);
        List<UserInfo> users = dataContext.getAllLatestChatUsers();
        ChatListAdapter adapter = new ChatListAdapter(users);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            signOut();
            return true;
        }
        else if(item.getItemId() == R.id.edit_profile) {
            editProfile();
            return true;
        }
        else if(item.getItemId() == R.id.browse_profiles){
            browse_profiles();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private LocalDate Today() {
        return LocalDate.now(ZoneId.systemDefault());
    }

    private void saveLatestAge(FirebaseFirestore db) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        final LocalDate today = Today();
        CollectionReference cr = db.collection("users");
        assert currentUser != null;

        DocumentReference df = cr.document(currentUser.getUid());
        getDateOfBirth(df, (date, username) -> {

            SharedPreferences sharedPreferences = ChatList.this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username",username);
            editor.putString("uid",currentUser.getUid());
            editor.apply();

            int age = between(date, today).getYears();
            Map<String, Object> Age = new HashMap<>();
            Age.put("Age", age);
            Age.put("UserStatus",true);
            Age.put("LastOnline", Timestamp.now());
            FirebaseUser currentUser1 = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
            CollectionReference cr1 = db1.collection("users");
            assert currentUser1 != null;
            DocumentReference df1 = cr1.document(currentUser1.getUid());
            df1.update(Age).addOnSuccessListener(aVoid -> Log.i("Age", "Saved")).addOnFailureListener(e -> Log.i("Age", "Not Saved"));
        });

    }

    //interface for getting DOB
    private interface gettingDate {
        void onCallback(LocalDate date, String username);
    }

    //Date Of Birth fetching from server
    private void getDateOfBirth(DocumentReference df, final gettingDate callback) {
        df.get().addOnSuccessListener(documentSnapshot -> {
            date = LocalDate.parse(Objects.requireNonNull(documentSnapshot.getString("DOB")));
            String username = documentSnapshot.getString("username");
            callback.onCallback(date,username);
        }).addOnFailureListener(e -> Log.i("Network", "interrupted"));
    }


    private void signOut(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
    }

    private void editProfile(){
        Intent i = new Intent(getApplicationContext(),Login.class);
        startActivity(i);
        finish();
    }

    private void browse_profiles() {
        Intent i = new Intent(getApplicationContext(), ServerUsers.class);
        startActivity(i);
        finish();
    }

    // Adapter class

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


}