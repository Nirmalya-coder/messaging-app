package com.chat.wechat.Utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.ActivityCompat;

class ContObserver extends ContentObserver {

    Context context;

    public ContObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }


    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        contactAdded(selfChange);
    }

    public void contactAdded(boolean selfChange) {
        if (!selfChange) {
            try {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    ContentResolver cr = context.getContentResolver();
                    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        //moving cursor to last position
                        //to get last element added
                        cursor.moveToLast();
                        String contactName, contactNumber;
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                        if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            if (pCur != null) {
                                pCur.moveToFirst();
                                contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                Log.i("k", contactName+", "+contactNumber);
                                //here you will get your contact information


                            }
                            pCur.close();
                        }
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}