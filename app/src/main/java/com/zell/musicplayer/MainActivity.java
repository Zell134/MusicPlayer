package com.zell.musicplayer;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.fragments.PlaylistFragment;
import com.zell.musicplayer.models.MusicController;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl{

    private final int REQUEST_CODE = 1;
    private final String NOTIFICATION_ID = "Notification";
    private static final String PERMISSION_STRING = android.Manifest.permission.READ_EXTERNAL_STORAGE;

    private MusicController musicController;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_STRING}, REQUEST_CODE);
        }else
        {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.playlist_container, PlaylistFragment.class, null)
                        .commit();
            }
            setController();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.playlist_container, PlaylistFragment.class, null)
                            .commit();                }
                else {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_ID)
                            .setSmallIcon(android.R.drawable.ic_menu_compass)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.permission_denied))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(new long[]{1000, 1000})
                            .setAutoCancel(true);
                    Intent actionIntent = new Intent(this, MainActivity.class);
                    PendingIntent actionPendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            actionIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(actionPendingIntent);
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID, "Permission_denied", NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                    notificationManager.notify(0, builder.build());
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setController(){
        musicController = new MusicController(this);
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                playPrev();
            }
        });

        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.playlist_container));
        musicController.setEnabled(true);
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int i) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}