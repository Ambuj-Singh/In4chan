package com.zodiac.in4chan.BackEnd.FriendsList;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.zodiac.in4chan.BackEnd.Models.UserInfo;

import com.zodiac.in4chan.R;
import com.zodiac.in4chan.Tools;

import java.util.ArrayList;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListViewModel> {

    private List<UserInfo> usersList = new ArrayList<>();
    private FirebaseFirestore db;
    private FriendsListViewModel holder;
    private int position;

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
        holder.title.setText(user.getUsername());
        boolean value = Tools.integerToBoolean(user.getUserStatus());
        //Resources.getSystem() when not extending the class to AppCompatActivity
            if(value)
                holder.onlineOffline.setVisibility(View.VISIBLE);
            else
                holder.onlineOffline.setVisibility(View.GONE);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interaction.onChatClicked(holder.getBindingAdapterPosition());
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

    private Interaction interaction;


}
