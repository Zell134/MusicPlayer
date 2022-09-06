package com.zell.musicplayer.viewModels;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.zell.musicplayer.Services.MusicPlayerService;
import com.zell.musicplayer.Services.NotificationService;

public class MediaBrowserViewModel extends AndroidViewModel {

    private MediaBrowserCompat mediaBrowser;
    private MutableLiveData<MediaControllerCompat> mediaController = new MutableLiveData<>();

    public MediaBrowserViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<MediaControllerCompat> getMediaController() {
        if(mediaBrowser == null){
            Context context = getApplication().getBaseContext();
            mediaBrowser = new MediaBrowserCompat(context,
                    new ComponentName(context, MusicPlayerService.class),
                    connectionCallback,
                    null);
        }
        return mediaController;
    }

    public void connect(){
        try {
            if (!mediaBrowser.isConnected()) {
                mediaBrowser.connect();
            }
        }catch(IllegalStateException e){
            mediaBrowser.connect();
        }
    }

    @Override
    protected void onCleared() {
        if(mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
        if(mediaController != null) {
            mediaController.getValue().getTransportControls().stop();
        }
        NotificationManagerCompat.from(getApplication().getBaseContext()).cancel(NotificationService.ID);
        super.onCleared();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                mediaController.setValue(new MediaControllerCompat(getApplication().getApplicationContext(), mediaBrowser.getSessionToken()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
}
