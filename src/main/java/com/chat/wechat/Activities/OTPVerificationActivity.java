package com.chat.wechat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chat.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OtpView;

import java.util.Timer;
import java.util.TimerTask;

public class OTPVerificationActivity extends AppCompatActivity {

    private String phoneNumber;
    FirebaseAuth firebaseAuth;
    private String verifyID;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_verification);
        getSupportActionBar().hide();
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        verifyID = getIntent().getStringExtra("verifyID");
        ((TextView) findViewById(R.id.verifyPhoneNumberText)).setText("Verify " + phoneNumber);
        findViewById(R.id.OtpText).requestFocus();
        firebaseAuth = FirebaseAuth.getInstance();
        final int[] time = {60};
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(time[0] <= 0)
                {
                    Intent intent = new Intent(OTPVerificationActivity.this, VerifyPhoneNumberActivity.class);
                    Toast.makeText(OTPVerificationActivity.this, "Timed out waiting for OTP!", Toast.LENGTH_SHORT).show();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    this.cancel();
                    return;
                }
                String timeText = "";
                time[0]--;
                timeText = String.valueOf(time[0]);
                if(time[0] < 10)
                    timeText = "0"+ time[0];
                runTask("Time left : "+timeText);
            }
        }, 0, 1000);

        ((OtpView) findViewById(R.id.OtpText)).setOtpCompletionListener(otp -> {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifyID, otp);
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    InputMethodManager manager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(findViewById(R.id.OtpText).getWindowToken(), 0);
                    Intent intent = new Intent(OTPVerificationActivity.this, ProfileSetupActivity.class);
                    startActivity(intent);
                    Toast.makeText(OTPVerificationActivity.this, "Verified!", Toast.LENGTH_SHORT).show();
                    finishAffinity();

                } else
                    {
                        ((OtpView) (findViewById(R.id.OtpText))).setText("");
                        Toast.makeText(getBaseContext(), "Wrong OTP! Please try again", Toast.LENGTH_LONG).show();
                    }
            });
        });
    }

    private void runTask(String timeLeft)
    {
        runOnUiThread(() -> ((TextView)findViewById(R.id.timerText)).setText(timeLeft));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
