package com.zodiac.in4chan.BackEnd.ConversationMessageList.MessageStructure;

import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zodiac.in4chan.BackEnd.ConversationMessageList.ConversationViewHolder;
import com.zodiac.in4chan.R;

import org.w3c.dom.Text;

public class SenderViewHolder extends ConversationViewHolder {

    public ImageView sent_image;
    public TextView message, time_sent;
    public ImageView message_delivery;
    public SenderViewHolder(@NonNull View itemView) {
        super(itemView);

        sent_image = (ImageView) itemView.findViewById(R.id.sent_image);
        message = (TextView) itemView.findViewById(R.id.message_sent);
        time_sent = (TextView)itemView.findViewById(R.id.time_sent);
        message_delivery = (ImageView) itemView.findViewById(R.id.message_delivery_status_sent);
    }
}
