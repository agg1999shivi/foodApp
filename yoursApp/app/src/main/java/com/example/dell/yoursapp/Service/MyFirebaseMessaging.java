package com.example.dell.yoursapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;


import com.example.dell.yoursapp.Common.Common;
import com.example.dell.yoursapp.Helper.NotificationHelper;
import com.example.dell.yoursapp.OrderStatus;
import com.example.dell.yoursapp.R;
import com.example.dell.yoursapp.ScreenOneActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sendNotificatonAPI(remoteMessage);
        else
        sendNotificaton(remoteMessage);
    }

    private void sendNotificatonAPI(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        String title=notification.getTitle();
        String content=notification.getBody();
        Intent intent=new Intent(MyFirebaseMessaging.this, OrderStatus.class);
        intent.putExtra(Common.PHONE_TEXT, Common.currentUser.getPhone());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper helper=new NotificationHelper(this);
        Notification.Builder builder=helper.channelNotification(title,content,pendingIntent,defaultSoundUri);

        helper.getManager().notify(new Random().nextInt(),builder.build());

    }

    private void sendNotificaton(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        Intent intent=new Intent(MyFirebaseMessaging.this, ScreenOneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());

    }
}
