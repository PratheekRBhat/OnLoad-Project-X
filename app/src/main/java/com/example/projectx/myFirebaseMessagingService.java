package com.example.projectx;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;



import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class myFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        Map<String, String> extraData = remoteMessage.getData();
        String DestinationLatitude = extraData.get("Latitude");
        String DestinationLongitude = extraData.get("Longitude");
        String key = extraData.get("Key");
        String notificationSender = extraData.get("Sender");
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.alarm);


        String notificationChannelID = "DistressSignalAlert";
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(3000);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelID)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.login_logo))
                .setSmallIcon(R.drawable.login_logo)
                .setSound(soundUri);








        Intent intent = new Intent(this, User_Activity.class);
        intent.putExtra("DLatitude", DestinationLatitude);
        intent.putExtra("DLongitude", DestinationLongitude);
        intent.putExtra("Skey",key);
        if(notificationSender!=null && notificationSender.equals("DistressSignal")){
            intent.putExtra("Sender","DistressSignal");
        }
        else if(notificationSender!=null && notificationSender.equals("Volunteer")){
            intent.putExtra("Sender","Volunteer");
        }
        else if(notificationSender!=null && notificationSender.equals("emergencyContact")){
            intent.putExtra("Sender","emergencyContact");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelID, "distressSignal", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            notificationChannel.setSound(soundUri, audioAttributes);
        }
        int NOTIFICATION_ID = 23071998;
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
