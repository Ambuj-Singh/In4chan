package com.zodiac.in4chan.BackEnd.Services;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zodiac.in4chan.Tools;


import java.util.ArrayList;
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
    public int onStartCommand(Intent intent, int flags, int startId) {

        FirebaseDatabase database = FirebaseDatabase.getInstance(Tools.firebaseURL);
        DatabaseReference reference = database.getReference(Tools.getUsername()+"/messages");
        DataContext dataContext = new DataContext(null,null,null,1);
        SQLiteDatabase sqLiteDatabase = dataContext.getWritableDatabase();

        reference.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               List<String> messages = new ArrayList<>();

               Map map = snapshot.getValue(Map.class);

               List<String> list = new ArrayList<>(map.keySet());
               for (int i = list.size()-1; i>=0; i--) {
                   messages.add(String.valueOf(map.get(list.get(i))));
               }

               for(Object o : list)
                   map.put(o,1);

               reference.updateChildren(map).addOnSuccessListener(new OnSuccessListener() {
                   @Override
                   public void onSuccess(Object o) {
                        Log.i("Update","success");
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Log.i("Update","failure");
                   }
               });

               for(String s : messages) {
                   sqLiteDatabase.execSQL(s, null);
                   Log.i("Receiver_Service",s);

               }
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
