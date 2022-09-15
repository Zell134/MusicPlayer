package com.zell.musicplayer.services;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.zell.musicplayer.R;

public class NotificationService {

    private final String NOTIFY_ID="com.zell.musicplayer.Services";
    public static final int ID = 1;

    private final NotificationCompat.Builder builder;

    public NotificationService (
            MusicPlayerService context, MediaSessionCompat mediaSession) {
        NotificationChannel notificationChannel;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFY_ID,
                    context.getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder = new NotificationCompat.Builder(context, NOTIFY_ID);

        builder
                .setSmallIcon(R.drawable.audio)
                .setContentIntent(mediaSession.getController().getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
                .setMediaSession(mediaSession.getSessionToken())
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
        );
    }

    public NotificationCompat.Builder getBuilder() {
        return builder;
    }
}
