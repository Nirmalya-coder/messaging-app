package com.chat.wechat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chat.wechat.R;
import com.chat.wechat.Utils.Message;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter{

    private final Context context;
    private ArrayList<Message> messages;
    private String currentUid;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context, ArrayList<Message> messages, String currentUid)
    {
        this.context = context;
        this.messages = messages;
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.sender, parent, false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(currentUid.equals(messages.get(position).getSenderId()))
            return ITEM_SENT;
        else
            return ITEM_RECEIVE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String timeText = messages.get(position).getTimeStamp();
        if(holder.getClass() == SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder)holder;
            viewHolder.senderText.setText(messages.get(position).getMessage());
            viewHolder.timeText.setText(timeText);
        }
        else
        {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.receiverText.setText(messages.get(position).getMessage());
            viewHolder.timeText.setText(timeText);

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder{

        TextView senderText;
        TextView timeText;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.senderText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        TextView receiverText;
        TextView timeText;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverText = itemView.findViewById(R.id.receiverText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
