package com.zodiac.in4chan.BackEnd.ChatList;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zodiac.in4chan.R;

//ViewHolder of FriendsList
public class ChatListViewHolder extends RecyclerView.ViewHolder {
    MaterialCardView materialCardView;
    ImageView profile_pic;
    TextView title, message_view, time, messageCounter, readStatus;
    FrameLayout onlineOffline;

    ChatListViewHolder(View item) {
        super(item);
        materialCardView = item.findViewById(R.id.chat_list_card);
        profile_pic = item.findViewById(R.id.profile_display_list);
        title = item.findViewById(R.id.user_name_list);
        message_view = item.findViewById(R.id.message_view_list);
        time = item.findViewById(R.id.last_message_time_list);
        messageCounter = item.findViewById(R.id.message_counter_list);
        readStatus = item.findViewById(R.id.message_delivery_status_list);
        onlineOffline = item.findViewById(R.id.online_offline_user);
    }
}
