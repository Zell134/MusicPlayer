package com.zell.musicplayer.Services;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.zell.musicplayer.R;

public class MediaStyleHelper {

    private static final String NOTIFY_ID="com.zell.musicplayer.Services";

    public static NotificationCompat.Builder from(
            MusicPlayerService context, MediaSessionCompat mediaSession) {

        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();

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
        String title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String album = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFY_ID);
        builder
                .addAction(
                        new NotificationCompat.Action(
                                android.R.drawable.ic_media_previous, context.getString(R.string.previous),
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        context,
                                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
                .addAction(
                        new NotificationCompat.Action(android.R.drawable.ic_media_next, context.getString(R.string.next),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                    context,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(artist + " - " + title + " (" + album + ")")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.wallpaper1))
                .setSmallIcon(R.drawable.audio)
                .setContentIntent(controller.getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
                .setMediaSession(mediaSession.getSessionToken())
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
        );
        return builder;
    }
}
