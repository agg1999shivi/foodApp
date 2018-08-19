package com.example.dell.yoursapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static int SPLASH_TIME_OUT = 3000;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("TAG", "onCreate: splash activity called" );
//        ImageView image = findViewById(R.id.image);
//        image.setImageDrawable(getResources().getDrawable(R.drawable.splash));

        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(MainActivity.this, ScreenOneActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });

                } catch (InterruptedException e) {
                    Log.e("TAG", "run: error occurred:"+e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();

    }
//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if(currentUser==null){
//            Intent authIntent=new Intent(MainActivity.this,PhoneAuthActivity.class);
//            startActivity(authIntent);
//            finish();
//        }
//    }

}


