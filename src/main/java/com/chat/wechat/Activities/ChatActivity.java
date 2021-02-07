package com.chat.wechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chat.wechat.Adapters.MessageAdapter;
import com.chat.wechat.R;
import com.chat.wechat.Utils.DatabaseHandler;
import com.chat.wechat.Utils.Message;
import com.chat.wechat.Utils.MessageReceiverService;
import com.chat.wechat.Utils.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private MessageAdapter adapter;
    private ArrayList<Message> messages;
    private String receiverRoom;
    private RecyclerView chatsView;
    private String currentUid;
    private FirebaseDatabase database;
    private SQLiteDatabase localDatabase;
    private String receiverUid;
    private SimpleDateFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatsView = findViewById(R.id.chatsView);

        currentUid = FirebaseAuth.getInstance().getUid();
        messages = new ArrayList<>();
        adapter = new MessageAdapter(ChatActivity.this, messages, currentUid);
        chatsView.setLayoutManager(new LinearLayoutManager(this));
        chatsView.setAdapter(adapter);
        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");
        formatter = new SimpleDateFormat("hh : mm a");

        
        receiverRoom = receiverUid + currentUid;
        localDatabase = new DatabaseHandler(this).getWritableDatabase();
        Cursor cursor = localDatabase.query("database_"+receiverUid, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Message message = new Message(cursor.getString(2), cursor.getString(1), cursor.getString(3));
            messages.add(message);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        chatsView.scrollToPosition(messages.size() - 1);
        database = FirebaseDatabase.getInstance();
        database.getReference()
                .child("chats")
                .child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            Message message = dataSnapshot.getValue(Message.class);
                            String timeText = formatter.format(new Date(System.currentTimeMillis()));
                            message.setTimeStamp(timeText);
                            messages.add(message);
                            insertToDatabase(message.getSenderId(), message.getMessage(), timeText);
                            dataSnapshot.getRef().removeValue();

                        }
                        adapter.notifyDataSetChanged();
                        chatsView.scrollToPosition(messages.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void onSendBtnClick(View view)
    {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_animation));
        String messageText = ((TextView)findViewById(R.id.sendText)).getText().toString();
        if(!messageText.trim().isEmpty()) {
            String timeText = formatter.format(new Date(System.currentTimeMillis()));
            Message message = new Message(messageText.trim(), currentUid, timeText);
            messages.add(message);
            adapter.notifyDataSetChanged();
            insertToDatabase(message.getSenderId(), message.getMessage(), timeText);
            database.getReference().child("chats")
                    .child(receiverUid)
                    .child(currentUid)
                    .push()
                    .setValue(message);
            chatsView.scrollToPosition(messages.size() - 1);
            ((TextView) findViewById(R.id.sendText)).setText("");
        }
    }

    private void insertToDatabase(String senderID, String message, String timeStamp)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put("senderID", senderID);
        contentValues.put("message", message);
        contentValues.put("timeStamp", timeStamp);
        localDatabase.insert("database_"+receiverUid, null, contentValues);
    }

}