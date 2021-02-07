package com.chat.wechat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.chat.wechat.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
;

import java.util.concurrent.TimeUnit;


public class VerifyPhoneNumberActivity extends AppCompatActivity {

    private String phoneNumber;
    private FirebaseAuth auth;
    private EditText nameText;
    private ProgressDialog dialog;
    private final String PREFERENCES = "preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            boolean readDatabase = preferences.getBoolean("readDatabase", true);
            if (readDatabase) {
                Intent intent = new Intent(VerifyPhoneNumberActivity.this, ProfileSetupActivity.class);
                intent.putExtra("readDatabase", false);
                finish();
                startActivity(intent);
                return;
            }
            else {
                Intent intent = new Intent(VerifyPhoneNumberActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
                return;
            }
        } else {
            nameText = findViewById(R.id.nameEntryText);
            getSupportActionBar().hide();
            findViewById(R.id.nameEntryText).requestFocus();
        }
    }
    public void OnContinueBtnClick(View view) {
        phoneNumber = ((EditText) findViewById(R.id.nameEntryText)).getText().toString();
        if (phoneNumber.isEmpty()) {
            nameText.setError("Please enter your phone number!");
            return;
        } else if (phoneNumber.length() < 10) {
            nameText.setError("Please enter a valid phone number!");
            return;
        }
        dialog = new ProgressDialog(this, R.style.customProgressDialog);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(VerifyPhoneNumberActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e)
                    {
                        if (e.getMessage().equals("An internal error has occurred. [ Unable to resolve host \"www.googleapis.com\":No address associated with hostname ]")) {
                            dialog.dismiss();
                            showAlertDialog("No internet connection", "Please make sure that you have an active internet connection before retrying.", (dialog, which) -> nameText.setText(""));
                        } else {
                            dialog.dismiss();
                            showAlertDialog("Invalid phone number", "The phone number you entered is invalid! Please try again.", (dialog, which) -> nameText.setText(""));
                        }
                    }
                    @Override
                    public void onCodeSent(@NonNull String verify, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verify, forceResendingToken);
                        dialog.dismiss();
                        Intent intent = new Intent(VerifyPhoneNumberActivity.this, OTPVerificationActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("verifyID", verify);
                        startActivity(intent);
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void showAlertDialog(String title, String message, DialogInterface.OnClickListener listener)
    {
        new AlertDialog.Builder(VerifyPhoneNumberActivity.this)
                .setTitle(Html.fromHtml("<font color='#FF000000'>"+title+"</font>"))
                .setMessage(Html.fromHtml("<font color='#919191'>"+message+"</font>"))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
}