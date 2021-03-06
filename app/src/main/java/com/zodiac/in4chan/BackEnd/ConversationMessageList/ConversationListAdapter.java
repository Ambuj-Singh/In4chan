package com.zodiac.in4chan.BackEnd.ConversationMessageList;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zodiac.in4chan.BackEnd.ConversationMessageList.MessageStructure.ReceiverViewHolder;
import com.zodiac.in4chan.BackEnd.ConversationMessageList.MessageStructure.SenderViewHolder;
import com.zodiac.in4chan.BackEnd.Models.MessageModel;
import com.zodiac.in4chan.Tools;
import com.zodiac.in4chan.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationViewHolder> {

    private static final int ITEM_LEFT = 1;
    private static final int ITEM_RIGHT = 2;
    List<MessageModel> messageModelList = new ArrayList<>();
    private String receiver;

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){
            case ITEM_LEFT:
                return new ReceiverViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.received_message,parent,false));
            case ITEM_RIGHT:
                return new SenderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_message,parent,false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
           final MessageModel messageModel = messageModelList.get(position);
            if(holder.getItemViewType() == ITEM_LEFT){
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.message.setText(messageModel.getMessage());
                viewHolder.time_receiver.setText(Tools.getTimeStamp(messageModel.getTimestamp()));

                if(messageModel.isDelivery()) {
                    viewHolder.message_delivery.setBackgroundResource(R.drawable.ic_baseline_check_24);
                    if (messageModel.getRead())
                        viewHolder.message_delivery.setBackgroundResource(R.drawable.ic_baseline_done_outline_24);
                }
            }
            else {
                SenderViewHolder viewHolder = (SenderViewHolder) holder;
                //image visibility : gone later change for a url than visibility : visible
                viewHolder.message.setText( messageModel.getMessage());
                viewHolder.time_sent.setText(Tools.getTimeStamp(messageModel.getTimestamp()));

                if(messageModel.isDelivery()) {
                    viewHolder.message_delivery.setBackgroundResource(R.drawable.ic_baseline_check_24);
                    if (messageModel.getRead())
                        viewHolder.message_delivery.setBackgroundResource(R.drawable.ic_baseline_done_outline_24);
                }
            }

    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messageModelList.get(position).getSender().equals(receiver))
            return 1;
        return 2;
    }

    public void setChatMessages(List<MessageModel> messageModelList, String receiver){
        this.messageModelList = messageModelList;
        this.receiver = receiver;
        notifyDataSetChanged();
    }

    public void addMessage(MessageModel messageModel){
        this.messageModelList.add(messageModel);
        notifyDataSetChanged();
    }

    public void deleteMessage(int position){
        this.messageModelList.remove(position);
        notifyItemRemoved(messageModelList.size()-1);
    }

}
