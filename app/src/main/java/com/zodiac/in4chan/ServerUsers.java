package com.zodiac.in4chan;

import static org.threeten.bp.Period.between;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;
import com.zodiac.in4chan.BackEnd.Models.UserInfoGrabber;
import com.zodiac.in4chan.BackEnd.Services.DataContext;
import com.zodiac.in4chan.BackEnd.UserStatus.Status_Offline;
import com.zodiac.in4chan.BackEnd.UserStatus.Status_Online;
import com.zodiac.in4chan.databinding.ActivityServerUsersBinding;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ServerUsers extends AppCompatActivity implements LifecycleObserver {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private ActivityServerUsersBinding binding;
    private OneTimeWorkRequest oneTimeWorkRequestA, oneTimeWorkRequestB;
    private DataContext dataContext;
    private LocalDate date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        binding = ActivityServerUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            currentUser.reload();

        dataContext = new DataContext(this);

        db = FirebaseFirestore.getInstance();

        setStatusOnline();

        saveLatestAge(db);
        //Progress Dialog
        final ProgressDialog progressDialog = new ProgressDialog(ServerUsers.this);
        progressDialog.setMessage("Fetching Users...");
        progressDialog.create();
        progressDialog.show();
        ProgressBar progressBar = progressDialog.findViewById(android.R.id.progress);
        progressBar.getIndeterminateDrawable().setTint(getResources().getColor(R.color.colorPrimary,null));

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //querying all users
        Query query = db.collection("users");



        getQuery(query, value -> {
            LinearLayout layout = binding.noWifiImg;
            if(value){
                layout.setVisibility(View.VISIBLE);
            }
            else {
                layout.setVisibility(View.GONE);

                initializeData();
                StartListener();
            }
            progressDialog.cancel();
        });

        }
        else{
            Intent i = new Intent(ServerUsers.this, Login.class);
            startActivity(i);
            finish();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.chat_list_menu) {
            try {
                 chatListSwitch();
            }
            catch (Exception e){
                Log.i("Menu",e.getMessage());
            }
            return true;
        }
        else if(item.getItemId() == R.id.search_menu) {
            //searchProfiles();
            return true;
        }
        else return super.onOptionsItemSelected(item);
    }

    private void chatListSwitch() {
        Intent i = new Intent(getApplicationContext(), FriendsList.class);
        startActivity(i);
    }

    private interface getQueryResult{
        void onCallback(boolean value);
    }

    private void getQuery(Query query, final getQueryResult callback){
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size()!=0){
                Log.i("query_size", String.valueOf(queryDocumentSnapshots.size()));
                callback.onCallback(false);
            }
            else{
                callback.onCallback(true);
            }
        });
    }

    //ViewHolder of ServerUserList
    private static class ProfileViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView materialCardView;
        ImageView profile_pic;
        TextView title, age_status, status;
        Button add_friend, chat_with_user;

        private ProfileViewHolder(View item) {
            super(item);
            materialCardView = item.findViewById(R.id.card_exp);
            profile_pic = item.findViewById(R.id.display_profile_exp);
            title = item.findViewById(R.id.user_name_exp);
            age_status = item.findViewById(R.id.age_view_user_status_exp);
            status = item.findViewById(R.id.Status);
            add_friend = item.findViewById(R.id.Add_friend);
            chat_with_user = item.findViewById(R.id.chat_with_user);
        }
    }

    //fetching data from server
    public void initializeData() {
        final String uid = dataContext.getUID();

        Query query = db.collection("users").whereNotEqualTo("UID",uid);
        FirestoreRecyclerOptions<UserInfoGrabber> response = new FirestoreRecyclerOptions.Builder<UserInfoGrabber>()
                .setQuery(query, UserInfoGrabber.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<UserInfoGrabber, ProfileViewHolder>(response) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull ProfileViewHolder holder, int position, final UserInfoGrabber model) {


                holder.title.setText(String.valueOf(model.getusername()));
                holder.age_status.setText("Age : " +model.getAge());
                //setting user status on data change
                if (model.getUserStatus()) {
                    setOnline(holder.status);
                } else {
                    setOffline(holder.status);
                }

                /*Glide.with(getContext())
                        .load(String.valueOf(model.image()))
                        .into(holder.profile_pic);
*/
               holder.add_friend.setOnClickListener(v -> {
                   UserInfo userInfo = new UserInfo();
                   userInfo.setUsername(model.getusername());
                   userInfo.setName(model.getName());
                   userInfo.setImage(model.getImage());
                   userInfo.setAge(model.getAge());
                   userInfo.setTimestamp(0);
                   String tableName = model.getusername() + "_table";
                   userInfo.setMessage(tableName);
                   userInfo.setUID(model.getUID());
                   userInfo.setUserStatus(Tools.booleanToInteger(model.getUserStatus()));

                   boolean result = dataContext.insertUser(userInfo);
                   if(result)
                       Toast.makeText(ServerUsers.this, "Friend added", Toast.LENGTH_SHORT).show();
                   else
                       Toast.makeText(ServerUsers.this, "Friend already added", Toast.LENGTH_SHORT).show();
               });

                holder.chat_with_user.setOnClickListener(view -> {
                        Intent i = new Intent(ServerUsers.this,Conversation.class);
                        i.putExtra("sender",dataContext.getUsername());
                        i.putExtra("receiver",model.getusername());
                        startActivity(i);
                });
            }

            @NonNull
            @Override
            public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_exp, parent, false);
                return new ProfileViewHolder(v);

            }

            @Override
            public void onChildChanged(@NonNull ChangeEventType type, @NonNull DocumentSnapshot snapshot, int newIndex, int oldIndex) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", Objects.requireNonNull(e.getMessage()));
            }
        };

        recyclerView.setAdapter(adapter);

    }

    //Sending userStatus to the server
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }

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

    public void StartListener(){
        adapter.startListening();
    }

    //Sending userStatus to the server
    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
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

    //Sending userStatus to the server
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

    public void setOnline(TextView tx){
        tx.setText(getResources().getString(R.string.online));
        tx.setTextColor(getResources().getColor(R.color.colorAccent,null));
    }

    public void setOffline(TextView tx){
        tx.setText(getResources().getString(R.string.offline));
        tx.setTextColor(getResources().getColor(R.color.white,null));
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
        //copy the function below and paste it in Login.
        getDateOfBirth(df, (date, username) -> {

            int age = between(date, today).getYears();
            Map<String, Object> Age = new HashMap<>();
            Age.put("Age", age);
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

    public void setStatusOnline() {
        CollectionReference collectionReference = db.collection("users");
        DocumentReference df = collectionReference.document(dataContext.getUID());

        Map<String, Object> status = new HashMap<>();
        status.put("UserStatus", true);
        df.update(status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("User_status","online");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("User_status","auth failed");
            }
        });

    }

    public void setStatusOffline() {

        CollectionReference collectionReference = db.collection("users");
        DocumentReference df = collectionReference.document(dataContext.getUID());

        Map<String, Object> status = new HashMap<>();
        status.put("UserStatus", false);
        df.update(status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("User_status","online");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("User_status","auth failed");
            }
        });
    }
}
