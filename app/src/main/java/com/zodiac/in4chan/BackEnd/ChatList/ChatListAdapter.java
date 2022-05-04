package com.zodiac.in4chan.BackEnd.ChatList;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;

import com.zodiac.in4chan.Tools;
import com.zodiac.in4chan.R;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListViewHolder> {

    List<UserInfo> usersList = new ArrayList<>();
    FirebaseFirestore db;

    public ChatListAdapter(List<UserInfo> usersList){
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_view_model, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        final UserInfo user = usersList.get(position);

        //holder items
        holder.title.setText(user.getUsername());
        holder.message_view.setText(user.getMessage());
        holder.time.setText(Tools.getTimeStamp(user.getTimestamp()));
        db = FirebaseFirestore.getInstance();
        String uid = user.getUID();
        Query query = db.collection("users").whereEqualTo("UID",uid).whereEqualTo("UserStatus",true);

        //Resources.getSystem() when not extending the class to AppCompatActivity
        getQuery(query, value -> {
            if(value)
                holder.onlineOffline.setBackground(ResourcesCompat.getDrawable(Resources.getSystem(),R.drawable.profile_online,null));
            else
                holder.onlineOffline.setBackground(ResourcesCompat.getDrawable(Resources.getSystem(),R.drawable.profile_offline,null));
        });



    }
    private interface getQueryResult{
        void onCallback(boolean value);
    }

    private void getQuery(Query query, final getQueryResult callback){
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size()!=0){
                    Log.i("query_size", String.valueOf(queryDocumentSnapshots.size()));
                    callback.onCallback(false);
                }
                else{
                    callback.onCallback(true);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public ChatListAdapter(Interaction interaction){

    }

    public interface Interaction {
        void onChatClicked(int position,UserInfo user);
        void onChatLongClicked(int position,UserInfo user);
    }

    Interaction interaction;


}
