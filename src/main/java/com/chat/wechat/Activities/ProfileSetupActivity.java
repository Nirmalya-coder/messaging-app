package com.chat.wechat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.chat.wechat.Utils.DatabaseHandler;
import com.chat.wechat.R;
import com.chat.wechat.Utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.ArrayList;
import java.util.Locale;


public class ProfileSetupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImage;
    private int ct = 0;
    private ProgressDialog dialog;
    private ArrayList<String> userNumber;
    private final String PREFERENCES = "preferences";
    private SQLiteDatabase localDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this, R.style.customProgressDialog);
        dialog.setMessage("Creating your profile...");
        dialog.setCancelable(false);

        localDatabase = new DatabaseHandler(this).getWritableDatabase();
    }
    public void OnClick(View view) {
        CropImage.activity()
                .setCropMenuCropButtonTitle("Done")
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setOutputCompressQuality(70)
                .setFixAspectRatio(true)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                selectedImage = result.getUri();
                ((ImageView) findViewById(R.id.profileImage)).setImageURI(selectedImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    public void onClickBtn(View view) {
        String name = ((EditText) findViewById(R.id.nameEntryText)).getText().toString();
        if (name.isEmpty()) {
            ((EditText) findViewById(R.id.nameEntryText)).setError("Please enter a name!");
            return;
        }


        if (selectedImage != null) {
            StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
            reference.putFile(selectedImage).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        String Uid = auth.getUid();
                        String phoneNumber = auth.getCurrentUser().getPhoneNumber();
                        User user = new User(Uid, name, phoneNumber, imageUrl);
                        database.getReference()
                                .child("users")
                                .child(Uid)
                                .setValue(user);
                    });
                }
            });
        }
        else {
            String Uid = auth.getUid();
            String phoneNumber = auth.getCurrentUser().getPhoneNumber();
            User user = new User(Uid, name, phoneNumber, "No Image");
            database.getReference()
                    .child("users")
                    .child(Uid)
                    .setValue(user);
        }
        if (ContextCompat.checkSelfPermission(ProfileSetupActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProfileSetupActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
        else {
            dialog.show();
            handlerThread.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            dialog.show();
            handlerThread.start();
        }
        else {
            ct++;
            if (ct <= 1) {
              new AlertDialog.Builder(ProfileSetupActivity.this)
                      .setMessage(R.string.alert_dialog)
                      .setCancelable(false)
                      .setPositiveButton(R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(ProfileSetupActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1))
                      .setNegativeButton(R.string.no, (dialog, which) -> {
                          Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                          startActivity(intent);
                          finish();
                      }).show();

            }
        }
    }

    HandlerThread handlerThread = new HandlerThread("ReadContactsThread", Process.THREAD_PRIORITY_BACKGROUND) {
        private void getUserByNumber(String number, String name) {
            Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("phoneNumber").equalTo(number);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                User user = dataSnapshot.getValue(User.class);
                                if (!userNumber.contains(number)){
                                    userNumber.add(number);
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(DatabaseHandler.COLUMN_NAME, name);
                                    contentValues.put(DatabaseHandler.COLUMN_USER_UID, user.getUid());
                                    contentValues.put(DatabaseHandler.COLUMN_PHONE_NUMBER, user.getPhoneNumber());
                                    contentValues.put(DatabaseHandler.COLUMN_PROFILE_PICTURE, user.getProfileImage());
                                    localDatabase.insert(DatabaseHandler.TABLE_NAME, null, contentValues);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private String getCountryISO() {
            String iso = null;
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
            // Try to get country code from TelephonyManager service
            if (telephonyManager != null) {
                // Query first getSimCountryIso()
                iso = telephonyManager.getNetworkCountryIso();
                if (iso != null) {
                    return iso.toUpperCase();
                }
                if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                    iso = Locale.getDefault().getCountry().toUpperCase();
                    return iso.toUpperCase();
                } else {
                    // For 3G devices (with SIM) query getNetworkCountryIso()
                    iso = telephonyManager.getNetworkCountryIso();
                }
                if (iso != null) {
                    return iso;
                }
            }
            return iso.toUpperCase();
        }
        @Override
        public void run() {
            dialog.show();
            userNumber = new ArrayList<>();
            String Iso = getCountryISO();
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            Phonenumber.PhoneNumber formattedNumber;
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                if (phoneNumber != null) {
                    try {
                        formattedNumber = phoneNumberUtil.parse(phoneNumber, Iso);
                        phoneNumber = phoneNumberUtil.format(formattedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                        getUserByNumber(phoneNumber, name);
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            phones.close();
            try {
                this.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dismiss();
            SharedPreferences preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("readDatabase", false);
            editor.apply();
            Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ProfileSetupActivity.this)
            .setMessage(R.string.exit)
                .setCancelable(true)
                .setPositiveButton(R.string.no, null)
                .setNegativeButton(R.string.yes, (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("readDatabase", true);
                    editor.apply();
                    localDatabase.close();
                    finish();
                })
                .show();
    }
    private void dismiss()
    {
        runOnUiThread(() -> dialog.dismiss());
    }

}
