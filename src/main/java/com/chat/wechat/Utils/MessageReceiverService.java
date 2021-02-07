package com.chat.wechat.Utils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.chat.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageReceiverService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.sender_drawable)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
        HandlerThread thread = new HandlerThread("MessageReaderService", Process.THREAD_PRIORITY_BACKGROUND) {
            @Override
            public void run() {
                SQLiteDatabase localDatabase = new DatabaseHandler(getApplicationContext()).getWritableDatabase();
                Cursor cursor = localDatabase.query(DatabaseHandler.TABLE_NAME, null, null, null, null, null, null);
                String currentUid = FirebaseAuth.getInstance().getUid();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                SimpleDateFormat formatter = new SimpleDateFormat("hh : mm a");
                while (cursor.moveToNext()) {
                    firebaseDatabase.getReference()
                            .child("chats")
                            .child(currentUid)
                            .child(cursor.getString(2))
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        if (!currentUid.equals(dataSnapshot.getValue(Message.class).getSenderId())) {
                                            String timeText = formatter.format(new Date(System.currentTimeMillis()));
                                            Message message = dataSnapshot.getValue(Message.class);
                                            message.setTimeStamp(timeText);
                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put("senderID", message.getSenderId());
                                            contentValues.put("message", message.getMessage());
                                            contentValues.put("timeStamp", timeText);
                                            Toast.makeText(getApplicationContext(), message.getMessage(), Toast.LENGTH_SHORT).show();
                                            localDatabase.insert("database_" + message.getSenderId(), null, contentValues);
                                            dataSnapshot.getRef().removeValue();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }
        };
        thread.start();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MessageReceiverService.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), alarmIntent);
    }
}
