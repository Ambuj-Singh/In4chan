package com.zodiac.in4chan.BackEnd.FriendsList;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zodiac.in4chan.R;

//ViewHolder of FriendsList
public class FriendsListViewModel extends RecyclerView.ViewHolder implements View.OnClickListener{
    MaterialCardView materialCardView;
    ImageView profile_pic;
    TextView title, message_view, time, messageCounter, readStatus;
    ImageView onlineOffline;
    FriendsListAdapter.Interaction interaction;

    FriendsListViewModel(View item, FriendsListAdapter.Interaction interaction) {
        super(item);
        materialCardView = item.findViewById(R.id.friends_list_card);
        profile_pic = item.findViewById(R.id.profile_display_friend_list);
        title = item.findViewById(R.id.user_name_friend_list);
        onlineOffline = item.findViewById(R.id.online_offline_friend_user);
        this.interaction = interaction;
    }

    @Override
    public void onClick(View view) {
        interaction.onChatClicked(getBindingAdapterPosition());
    }
}
