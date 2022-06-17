package com.zell.musicplayer;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity{

    private final int REQUEST_CODE = 1;
    private final String NOTIFICATION_ID = "2";
    public static final String PERMISSION_STRING = android.Manifest.permission.READ_EXTERNAL_STORAGE;

    MediaLibraryAccess mediaLibraryAccess;
    List<Song> mediaLibrary;
    ListView list;
    ArrayAdapter<String> adapter;

    MediaPlayer mediaPlayer = new MediaPlayer();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_STRING}, REQUEST_CODE);
        }else
        {
            setUp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUp();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setUp(){
        mediaLibraryAccess = new MediaLibraryAccess();
        mediaLibrary = mediaLibraryAccess.getAllMediaFromLibrary(getApplication());

        List<String> listOfSongs = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            listOfSongs = mediaLibrary
                    .stream()
                    .map(element->element.getArtist() + " - " + element.getName())
                    .collect(Collectors.toList()
                    );
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listOfSongs);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);

        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mediaLibrary.get(i).getPath()));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });
    }
}