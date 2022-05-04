package com.zodiac.in4chan.BackEnd.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceiverChan extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context,ServiceChan.class));
    }
}
