package com.zodiac.in4chan.BackEnd.Services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zodiac.in4chan.BackEnd.Models.MessageModel;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;
import com.zodiac.in4chan.BackEnd.Models.UserInfoGrabber;
import com.zodiac.in4chan.Conversation;
import com.zodiac.in4chan.Tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataContext extends SQLiteOpenHelper {


    public DataContext(@Nullable Context context) {
        super(context, "Senpai.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String table_friends = "create table if not exists Friends" +
                "(id integer primary key autoincrement,Username text, Name text, Image text, message text, timestamp integer, age integer, uid text, online integer);";
        String table_messages = "create table if not exists message_table (id integer primary key autoincrement,sender text, receiver text, imagePath text, message text, timestamp integer, filePath text, read integer, delivery integer);";
        String username = "create table if not exists username(username text, uid text);";
        sqLiteDatabase.execSQL(username);
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
        SQLiteDatabase db_1 = getReadableDatabase();
    /*    String query = "insert into message_table (sender, receiver, imagePath, message, timestamp, filepath, read, delivery) " +
                "values('"+message.getSender()+"', '"+message.getReceiver()+"', '"+message.getImagePath()+"', '"+message.getMessage()+"', '"+message.getTimestamp()+"', '"+message.getFilePath()+"', "+Tools.booleanToInteger(message.getRead())+","+Tools.booleanToInteger(message.isDelivery())+");";
 */ Log.i("result_insert_1", message.getReceiver());
        ContentValues values = new ContentValues();
        values.put("sender",message.getSender());
        values.put("receiver",message.getReceiver());
        values.put("imagePath",message.getImagePath());
        values.put("message",message.getMessage());
        values.put("timestamp",message.getTimestamp());
        values.put("filePath",message.getFilePath());
        values.put("read", Tools.booleanToInteger(message.getRead()));
        values.put("delivery",Tools.booleanToInteger(message.isDelivery()));
        Log.i("result_insert_1", message.getReceiver());
        long result = db.insert("message_table",null,values);

       /* db.execSQL(query);*/
    }

    public List<MessageModel> getAllChatMessages(String sender, String receiver){


        List<MessageModel> messageModelList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        String whereCondition = "((sender = '" + sender + "' and receiver='" + receiver + "') or (receiver = '" + sender + "' and sender='" + receiver + "'))";
        String selectQuery = "select * from ( select id,* from message_table where "+whereCondition+" order by timestamp desc ) order by timestamp;";

        Log.i("query",selectQuery);
       try {
           Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
           cursor.moveToFirst();
           while (!cursor.isAfterLast()) {
               MessageModel messageModel = new MessageModel();
               messageModel.setSender(cursor.getString(2));
               messageModel.setReceiver(cursor.getString(3));
               messageModel.setImagePath(cursor.getString(4));
               messageModel.setMessage(cursor.getString(5));
               messageModel.setTimestamp(cursor.getLong(6));
               messageModel.setFilePath(cursor.getString(7));
               messageModel.setRead(Tools.integerToBoolean(cursor.getInt(8)));
               messageModel.setDelivery(Tools.integerToBoolean(cursor.getInt(9)));

              /* Log.i("query_result",cursor.getString(2)+" "
               +cursor.getString(3)+" "+cursor.getString(4)+" "+
                       cursor.getString(5)+" " +cursor.getLong(6)+
                       " "+cursor.getString(7)+" "+Tools.integerToBoolean(cursor.getInt(8))
               +" "+Tools.integerToBoolean(cursor.getInt(9)));*/

               //adding messages object to list
               messageModelList.add(messageModel);
               cursor.moveToNext();
           }
           for (MessageModel messageModel1 : messageModelList)
               Log.i("select",messageModel1.getMessage());
           cursor.close();
           return messageModelList;
       }
       catch (Exception e){
           e.printStackTrace();
           return messageModelList;
       }

    }

    public boolean insertUser(UserInfo user) {
        if (checkUserDuplication(user.getUsername()) == 0) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("Username", user.getUsername());
            values.put("Name", user.getName());
            values.put("Image", user.getImage());
            values.put("message", user.getMessage());
            values.put("timestamp", user.getTimestamp());
            values.put("age", user.getAge());
            values.put("uid", user.getUID());
            values.put("online",user.getUserStatus());

            db.insert("Friends", null, values);
            db.close();
            return true;
        }
        else
            return false;
    }

    public int checkUserDuplication(String username){
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            String query = "select count(*) from Friends where Username = '"+ username + "';";
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
                userInfo.setUsername(cursor.getString(1));
                userInfo.setName(cursor.getString(2));
                userInfo.setImage(cursor.getString(3));
                userInfo.setMessage(cursor.getString(4));
                userInfo.setTimestamp(cursor.getLong(5));
                userInfo.setAge(cursor.getInt(6));
                userInfo.setUID(cursor.getString(7));
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

        String update_timestamp_user = "update Friends set timestamp = "+timestamp+" where ( Username = '"+username+"');";

        sqLiteDatabase.execSQL(update_timestamp_user);
    }

    public void setDeliveryUpdate(MessageModel messageModel, boolean value) {
        String query = "update message_table set delivery = "+Tools.booleanToInteger(value)+" where timestamp = "+messageModel.getTimestamp()+";";
        this.getWritableDatabase().execSQL(query);
    }

    public String getUsername(){
        String query = "select username from username;";
        String username = "none";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()>0) {
            cursor.moveToFirst();

            username = cursor.getString(0);
        }
        return username;

    }

    public String getUID() {
        String query = "select uid from username;";
        String username = "none";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            username = cursor.getString(0);
        }
        return username;
        }

    public boolean updateStatus(List<UserInfoGrabber> users) {

        try {
            for (UserInfoGrabber user : users) {
                boolean status = user.getUserStatus();
                String username = user.getusername();
                String UpdateStatus = "update Friends set online = " + Tools.booleanToInteger(status) + " where (Username = '" + username + "');";
                this.getWritableDatabase().execSQL(UpdateStatus);
            }
            return true;
        } catch (Exception e){
            Log.i("updateStatus()",e.getMessage());
            return false;
        }
    }

    public void clearChat(String sender, String receiver) {

        String whereCondition = "((sender = '" + sender + "' and receiver='" + receiver + "') or (receiver = '" + sender + "' and sender='" + receiver + "'))";

        String deleteConversationQuery = "delete from message_table where "+whereCondition+";";

        this.getWritableDatabase().execSQL(deleteConversationQuery);
    }
}
