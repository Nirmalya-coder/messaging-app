package com.chat.wechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.chat.wechat.Adapters.ContactsAdapter;
import com.chat.wechat.Utils.DatabaseHandler;
import com.chat.wechat.R;
import com.chat.wechat.Utils.MessageReceiverService;
import com.chat.wechat.Utils.User;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<User> contacts;
    private ContactsAdapter contactsAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contacts = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(this, contacts);
        recyclerView = findViewById(R.id.main_view);
        recyclerView.setAdapter(contactsAdapter);
        SQLiteDatabase database = new DatabaseHandler(MainActivity.this).getReadableDatabase();
        Cursor cursor = database.query(DatabaseHandler.TABLE_NAME, null, null, null, null, null, DatabaseHandler.COLUMN_NAME);
        while (cursor.moveToNext()) {
            User user = new User(cursor.getString(2), cursor.getString(1), cursor.getString(3), cursor.getString(4));
            database.execSQL("CREATE TABLE IF NOT EXISTS " + "database_" + user.getUid() +"(id INTEGER PRIMARY KEY AUTOINCREMENT, senderID TEXT, message TEXT, timeStamp TEXT)");
            contacts.add(user);
        }
        contactsAdapter.notifyDataSetChanged();
        cursor.close();
        database.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //String text = "";
        switch (item.getItemId()) {
           /* case R.id.search:
                text = "search";
                break;
            case R.id.invite:
                text = "invite";
                break;
            case R.id.group:
                text = "group";
                break;
            case R.id.settings:
                text = "settings";
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}