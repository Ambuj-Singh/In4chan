package com.zodiac.in4chan.BackEnd.UserStatus;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Status_Offline extends Worker {
    FirebaseUser currentUser;
    FirebaseFirestore db;
    CollectionReference collectionReference;
    DocumentReference df;

    public Status_Offline(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
try
{
    db = FirebaseFirestore.getInstance();
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    collectionReference = db.collection("users");
    df = collectionReference.document(currentUser.getUid());
}
catch (Exception e){
    Log.i("error",e.toString());
}
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (df != null) {
                Map<String, Object> status = new HashMap<>();
                status.put("UserStatus", false);
                df.update(status).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("User_status", "offline");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("User_status", "auth failed");
                    }
                });
                return Result.success();
            } else {
                Toast.makeText(getApplicationContext(), "df is null", Toast.LENGTH_LONG).show();
                return Result.failure();
            }
        }
        catch (Exception e){
            Log.i("error_status",e.toString());
            return Result.failure();
        }
    }
}
