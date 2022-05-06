package com.zodiac.in4chan;

import com.google.firebase.database.FirebaseDatabase;

public class In4chan extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        //saving writes to cache when user goes offline or due to a network interruption
        FirebaseDatabase.getInstance(Tools.firebaseURL).setPersistenceEnabled(true);
    }
}
