package com.zodiac.in4chan.BackEnd.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.zodiac.in4chan.BackEnd.Models.UserInfoGrabber;
import com.zodiac.in4chan.Tools;

import java.util.List;
import java.util.Map;

public class ServiceChan extends Service {

    public ServiceChan(){

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("serviceChan","start");
        DataContext dataContext = new DataContext(this);

        String sender = dataContext.getUsername();

        FirebaseDatabase database = FirebaseDatabase.getInstance(Tools.firebaseURL);
        DatabaseReference reference = database.getReference("/messages");


        Query query = FirebaseFirestore.getInstance().collection("users").whereEqualTo("UserStatus",true);


        query.addSnapshotListener((value, error) -> {
            if(error != null){
                Log.i("status_set","Listening failed : "+error.getMessage());
                return;
            }
            if (value != null){
                List<UserInfoGrabber> users = value.toObjects(UserInfoGrabber.class);
                boolean check = dataContext.updateStatus(users);
                if(check) Log.i("status_set","updated");
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map map = snapshot.getValue(Map.class);

                String message = "";
                for (Object s : map.keySet())
                    message = String.valueOf(map.get(s));
                Log.i("message_service",message);
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
       return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(FirebaseAuth.getInstance().getUid() != null)
            sendBroadcast(new Intent(this,ServiceChan.class));
    }

}
