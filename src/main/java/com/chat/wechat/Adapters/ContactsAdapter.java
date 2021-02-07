package com.chat.wechat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.chat.wechat.Activities.ChatActivity;
import com.chat.wechat.R;
import com.chat.wechat.Utils.User;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.UserViewHolder>{

    private Context context;
    private ArrayList<User> users;

    public ContactsAdapter(Context context, ArrayList<User> users){
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User contact = users.get(position);
        holder.nameText.setText(contact.getName());
        holder.messageText.setText("Tap to chat");
        holder.timeText.setText("Yesterday");
        holder.profileImage.setImageResource(R.drawable.defaultprofilepic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", contact.getName());
            intent.putExtra("uid", contact.getUid());
            intent.putExtra("image", contact.getProfileImage());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView nameText;
        TextView messageText;
        TextView timeText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name);
            profileImage = itemView.findViewById(R.id.profile_image);
            messageText = itemView.findViewById(R.id.message);
            timeText = itemView.findViewById(R.id.timestamp_text);
        }
    }
}
