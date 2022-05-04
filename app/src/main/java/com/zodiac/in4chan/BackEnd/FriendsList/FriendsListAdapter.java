package com.zodiac.in4chan.BackEnd.FriendsList;


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

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListViewModel> {

    List<UserInfo> usersList = new ArrayList<>();
    FirebaseFirestore db;

    public FriendsListAdapter(List<UserInfo> usersList, Interaction interaction){
        this.usersList = usersList;
        this.interaction = interaction;
    }

    @NonNull
    @Override
    public FriendsListViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_view_model, parent, false);
        return new FriendsListViewModel(view, interaction);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsListViewModel holder,  int position) {
        final UserInfo user = usersList.get(position);

        //holder items
        holder.title.setText(user.getName());
        db = FirebaseFirestore.getInstance();
        String uid = user.getUID();
        Query query = db.collection("users").whereEqualTo("UID",uid).whereEqualTo("UserStatus",true);

        //Resources.getSystem() when not extending the class to AppCompatActivity
        getQuery(query, value -> {
            if(value)
                holder.onlineOffline.setBackgroundResource(R.drawable.profile_online);
            else
                holder.onlineOffline.setBackgroundResource(R.drawable.profile_offline);
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interaction.onChatClicked(position);
            }
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


    public interface Interaction {
        void onChatClicked(int position);
        void onChatLongClicked(int position);
    }

    Interaction interaction;


}
