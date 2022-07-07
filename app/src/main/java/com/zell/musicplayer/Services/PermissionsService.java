package com.zell.musicplayer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.zell.musicplayer.MainActivity;
import com.zell.musicplayer.R;

public class PermissionsService {

    private static final int REQUEST_CODE = 1;
    private static final String NOTIFICATION_ID = "Notification";
    private static final String PERMISSION_STRING = android.Manifest.permission.READ_EXTERNAL_STORAGE;

    public static boolean checkPermissions(AppCompatActivity activity) {
        int i = 0;
        if (ContextCompat.checkSelfPermission(activity, PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{PERMISSION_STRING}, REQUEST_CODE);
            return false;
        }else{
            return true;
        }
    }

    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, AppCompatActivity activity){

        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                else {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, NOTIFICATION_ID)
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle(activity.getResources().getString(R.string.app_name))
                            .setContentText(activity.getResources().getString(R.string.permission_denied))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(new long[]{1000, 1000})
                            .setAutoCancel(true);
                    Intent actionIntent = new Intent(activity, MainActivity.class);
                    PendingIntent actionPendingIntent = PendingIntent.getActivity(
                            activity,
                            0,
                            actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(actionPendingIntent);
                    NotificationManager notificationManager =
                            (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID, "Permission_denied", NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    notificationManager.notify(0, builder.build());
                    Toast.makeText(activity, R.string.permission_denied, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return false;
    }
}
