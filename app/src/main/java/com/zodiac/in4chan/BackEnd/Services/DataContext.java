package com.zodiac.in4chan.BackEnd.Services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.zodiac.in4chan.BackEnd.Models.MessageModel;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;
import com.zodiac.in4chan.Tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataContext extends SQLiteOpenHelper {


    public DataContext(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "Senpai.db", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String table_friends = "create table if not exists Friends" +
                " (Username text, Name text, Image text, message text, timestamp integer, age integer, uid text);";
        String table_messages = "create table if not exists message_table (sender text, receiver text, imagePath text, message text, timestamp integer, filePath text, read integer, delivery integer);";
        sqLiteDatabase.execSQL(table_friends);
        sqLiteDatabase.execSQL(table_messages);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String dropFriends = "drop table if exists Friends; ";
        String dropMessages = "drop table if exists message_table";

        sqLiteDatabase.execSQL(dropMessages);
        sqLiteDatabase.execSQL(dropFriends);
        onCreate(sqLiteDatabase);
    }

    public void insertMessage(MessageModel message){
            SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("sender",message.getSender());
        values.put("receiver",message.getReceiver());
        values.put("imagePath",message.getImagePath());
        values.put("message",message.getMessage());
        values.put("timpestamp",message.getTimestamp());
        values.put("filePath",message.getFilePath());
        values.put("read", Tools.booleanToInteger(message.getRead()));
        values.put("delivery",Tools.booleanToInteger(message.isDelivery()));

        db.insert("message_table",null,values);
    }

    public List<MessageModel> getAllChatMessages(String sender, String receiver){

        MessageModel messageModel = new MessageModel();
        List<MessageModel> messageModelList = new ArrayList<>();
        String whereCondition = "((sender = '" + sender + "' and receiver='" + receiver + "') or (receiver = '" + sender + "' and sender='" + receiver + "'))";
        String selectQuery = "select * from (select rowid, * from message_table where "+whereCondition+" order by rowid desc) order by rowid;";

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
       try {
           Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

           cursor.moveToFirst();
           while (!cursor.isAfterLast()) {
               messageModel.setSender(cursor.getString(0));
               messageModel.setReceiver(cursor.getString(1));
               messageModel.setImagePath(cursor.getString(2));
               messageModel.setMessage(cursor.getString(3));
               messageModel.setTimestamp(cursor.getLong(4));
               messageModel.setFilePath(cursor.getString(5));
               messageModel.setRead(Tools.integerToBoolean(cursor.getInt(6)));
               messageModel.setDelivery(Tools.integerToBoolean(cursor.getInt(7)));
               //adding messages object to list
               messageModelList.add(messageModel);
               cursor.moveToNext();
           }
           cursor.close();
           return messageModelList;
       }
       catch (Exception e){
           e.printStackTrace();
           return messageModelList;
       }

    }

    public void insertUser(UserInfo user){
        if(checkUserDuplication(user.getUsername()) == 0) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("Username", user.getUsername());
            values.put("Name", user.getName());
            values.put("Image", user.getImage());
            values.put("message", user.getMessage());
            values.put("timestamp", user.getTimestamp());
            values.put("age", user.getAge());
            values.put("uid", user.getUID());

            db.insert("Friends", null, values);
        }
    }

    public int checkUserDuplication(String username){
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            String query = "select count(*) from Friends where Username = " +"'"+ username + "';";
            cursor = db.rawQuery(query, null);

            if(cursor.moveToFirst()){
                Log.i("cursor", String.valueOf(cursor.getInt(0)));
                return cursor.getInt(0);}
            return 0;
        }
        finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
        }
    }

    public UserInfo getUserInfo(String username){
        UserInfo userInfo = new UserInfo();

        String selectUser = "select * from Friends where ( Username = " + username + ");";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectUser,null);

       cursor.moveToFirst();
       if(cursor.getCount()>0) {
           userInfo.setUsername(cursor.getString(0));
           userInfo.setName(cursor.getString(1));
           userInfo.setImage(cursor.getString(2));
           userInfo.setMessage(cursor.getString(3));
           userInfo.setTimestamp(cursor.getLong(4));
           userInfo.setAge(cursor.getInt(5));
           userInfo.setUID(cursor.getString(6));
       }

        return userInfo;
    }

    public List<UserInfo> getAllUsers() {

        String selectUsers = "select * from Friends;";
        List<UserInfo> users = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectUsers,null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            try {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(cursor.getString(0));
                userInfo.setName(cursor.getString(1));
                userInfo.setImage(cursor.getString(2));
                userInfo.setMessage(cursor.getString(3));
                userInfo.setTimestamp(cursor.getLong(4));
                userInfo.setAge(cursor.getInt(5));
                userInfo.setUID(cursor.getString(6));
                //Adding userModel to the list of users
                users.add(userInfo);
                cursor.moveToNext();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        cursor.close();

        Collections.sort(users, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo userInfo, UserInfo t1) {
                return userInfo.getUsername().compareTo(t1.getUsername());
            }
        });

        return users;
    }

    public List<UserInfo> getAllLatestChatUsers(){
        UserInfo userInfo = new UserInfo();

        String selectUsers = "select * from Friends order by timestamp desc;";

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectUsers,null);

        List<UserInfo> users = new ArrayList<>();
        if (cursor.moveToFirst()){
            do {
                if(!cursor.getString(3).isEmpty()) {
                    userInfo.setUsername(cursor.getString(0));
                    userInfo.setName(cursor.getString(1));
                    userInfo.setImage(cursor.getString(2));
                    userInfo.setMessage(cursor.getString(3));
                    userInfo.setTimestamp(cursor.getLong(4));
                    userInfo.setAge(cursor.getInt(5));
                    userInfo.setUID(cursor.getString(6));
                    //Adding userModel to the list of users
                    users.add(userInfo);
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        sqLiteDatabase.close();

        return users;
    }

    public void updateTimestampOfUser(String username, long timestamp){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        String update_timestamp_user = "update Friends set timestamp = "+timestamp+" where ( Username = "+"'"+username+"');";

        sqLiteDatabase.execSQL(update_timestamp_user);
    }

}
