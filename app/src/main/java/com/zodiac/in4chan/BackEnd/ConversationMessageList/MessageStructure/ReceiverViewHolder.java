package com.zodiac.in4chan.BackEnd.ConversationMessageList.MessageStructure;

import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zodiac.in4chan.BackEnd.ConversationMessageList.ConversationViewHolder;
import com.zodiac.in4chan.R;

public class ReceiverViewHolder extends ConversationViewHolder {
    public ImageView receiver_image;
    public TextView message, time_receiver;
    public ImageSwitcher message_delivery;
    public ReceiverViewHolder(@NonNull View itemView) {
        super(itemView);

        receiver_image = itemView.findViewById(R.id.received_image);
        message = itemView.findViewById(R.id.message_received);
        time_receiver = itemView.findViewById(R.id.time_received);
        message_delivery = itemView.findViewById(R.id.message_delivery_status_received);
    }
}
