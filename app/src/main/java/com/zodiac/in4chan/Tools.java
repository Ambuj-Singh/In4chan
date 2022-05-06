package com.zodiac.in4chan;

import android.annotation.SuppressLint;

import com.zodiac.in4chan.BackEnd.Models.MessageModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {

    private static String username;
    public static final String firebaseURL = "https://in4chan-default-rtdb.asia-southeast1.firebasedatabase.app/";
    public static String getTimeStamp(long timestamp){
        //converting timestamp to String in 12:00 pm format
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            return sdf.format(new Date(timestamp));
        }
        catch (Exception e){
            return "Error";
        }
    }

    public static boolean passwordValidator(String pass){
        return pass.matches("^([a-zA-z0-9_@$*!?]){8,30}$");
    }

    public static Integer booleanToInteger(boolean var){
        return var?1:0;
    }

    public static Boolean integerToBoolean(int var){
        return var == 1;
    }

    public static Integer getRead(boolean var){

        if (var)
            return R.drawable.ic_baseline_done_outline_24;
        return R.drawable.ic_baseline_check_24;
    }
    public static Integer getDelivery(boolean var){
        if (var)
            return R.drawable.ic_baseline_check_24;
        return R.drawable.ic_outline_watch_later_24;
    }

    public static String getQueryFromModel(MessageModel messageModel,String senderTable){
        return "insert into "+senderTable+" values ("+messageModel.getSender()+","+messageModel.getReceiver()+","+messageModel.getImagePath()
                +"," +messageModel.getMessage()+","+messageModel.getTimestamp()+","+messageModel.getFilePath()+","+Tools.booleanToInteger(messageModel.getRead())+","+Tools.booleanToInteger(messageModel.isDelivery())+");";
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Tools.username = username;
    }
}
