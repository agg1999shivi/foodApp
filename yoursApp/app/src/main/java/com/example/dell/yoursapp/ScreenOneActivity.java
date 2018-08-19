package com.example.dell.yoursapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;
//
//import io.paperdb.Paper;

public class ScreenOneActivity extends AppCompatActivity {
    Button btnsignin,btnsignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.e("TAG", "onCreate: has been stared after setting the layouts");

        Button btnAlert = findViewById(R.id.location);
        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScreenOneActivity.this);
                alertDialog.setTitle("ENABLE GPS");
                alertDialog.setIcon(R.drawable.location);
                alertDialog.setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                })
                        .setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setMessage("Allow FOODCUBO to access this device location?")
                        .create();
                alertDialog.show();
            }
        });

        Paper.init(this);

        btnsignin = findViewById(R.id.main_signin);
        btnsignup = findViewById(R.id.main_signup);

        btnsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signin = new Intent(ScreenOneActivity.this, SignUp.class);
                startActivity(signin);
            }
        });

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signin = new Intent(ScreenOneActivity.this, SignIn.class);
                startActivity(signin);
            }
        });


        String user=Paper.book().read(Common.USER_KEY);
       String pwd=Paper.book().read(Common.USER_PASSWORD);
       if(user!=null && pwd!=null){
            if(!user.isEmpty()&& !pwd.isEmpty()){
                login(user,pwd);
            }

        }
    }

    private void login(final String phone, final String pwd) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if (Common.isConnectedToInternet(getBaseContext())) {
            final ProgressDialog mDialog = new ProgressDialog(ScreenOneActivity.this);
            mDialog.setMessage("Please Wating...");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //check if user not exist in database
                    if (dataSnapshot.child(phone).exists()) {
                        //get user information
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);
                        if (user.getPassword().equals(pwd)) {
                            Intent homeIntent = new Intent(ScreenOneActivity.this, Home.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();

                        } else {
                            Toast.makeText(ScreenOneActivity.this, "Wrong Password !", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ScreenOneActivity.this, "User not exist in Database !", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            Toast.makeText(ScreenOneActivity.this,"Please check your connection!!!!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

}


