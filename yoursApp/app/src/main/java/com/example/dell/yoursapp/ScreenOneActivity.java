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
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
//
//import io.paperdb.Paper;

public class ScreenOneActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 7171;
    Button btnsignin,btnsignup;
    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        database=FirebaseDatabase.getInstance();
        users=database.getReference("User");

//        AccountKit.initialize(this);
//        Log.e("TAG", "onCreate: has been stared after setting the layouts");

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

               // startLoginSystem();

            }
        });

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signin = new Intent(ScreenOneActivity.this, SignIn.class);
                startActivity(signin);
            }
        });

        if(AccountKit.getCurrentAccessToken() != null){
            final android.app.AlertDialog waitingDialog=new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("Please wait");
            waitingDialog.setCancelable(false);


            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User localUser=dataSnapshot.getValue(User.class);

                                    Intent homeIntent = new Intent(ScreenOneActivity.this, Home.class);
                                    Common.currentUser = localUser;
                                    startActivity(homeIntent);
                                    waitingDialog.dismiss();
                                    finish();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }

        String user=Paper.book().read(Common.USER_KEY);
       String pwd=Paper.book().read(Common.USER_PASSWORD);
       if(user!=null && pwd!=null){
            if(!user.isEmpty()&& !pwd.isEmpty()){
                login(user,pwd);
            }

        }
    }

    private void startLoginSystem() {
        Intent intent=new Intent(ScreenOneActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder=
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            AccountKitLoginResult result=data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if(result.getError() != null){
                Toast.makeText(this,""+result.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }
            else if(result.wasCancelled()){
                Toast.makeText(this,"Cancel",Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                if(result.getAccessToken() != null){
                    final android.app.AlertDialog waitingDialog=new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Please wait");
                    waitingDialog.setCancelable(false);

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone=account.getPhoneNumber().toString();

                            users.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.child(userPhone).exists()){
                                                User newUser=new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName("");
                                                newUser.setBalance(String.valueOf(0.0));

                                                users.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                    Toast.makeText(ScreenOneActivity.this,"User register successful",Toast.LENGTH_SHORT).show();

                                                                users.child(userPhone)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                User localUser=dataSnapshot.getValue(User.class);

                                                                                Intent homeIntent = new Intent(ScreenOneActivity.this, Home.class);
                                                                                Common.currentUser = localUser;
                                                                                startActivity(homeIntent);
                                                                                waitingDialog.dismiss();
                                                                                finish();

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                            else {
                                                users.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                User localUser=dataSnapshot.getValue(User.class);

                                                                Intent homeIntent = new Intent(ScreenOneActivity.this, Home.class);
                                                                Common.currentUser = localUser;
                                                                startActivity(homeIntent);
                                                                waitingDialog.dismiss();
                                                                finish();

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(ScreenOneActivity.this," "+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}


