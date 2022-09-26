package com.zell.musicplayer.helpers.cursors;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.zell.musicplayer.models.Folder;
import com.zell.musicplayer.models.Item;

import java.util.ArrayList;
import java.util.List;

public class FoldersForMediaLibraryService implements Cursor {

    private final int size = 5;
    private int position;

    public List<Item> getAllRecords() {
        int currentPosition = position;
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Folder(getString(0), getString(1)));
            moveToNext();
        }
        position = currentPosition;
        return list;
    }

    public List<Item> getAllRecords(String path) {
        int currentPosition = position;
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Folder(path, getString(0)));
            moveToNext();
        }
        position = currentPosition;
        return list;
    }

    public int getCount() {
        return size;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean move(int i) {
        return false;
    }

    @Override
    public boolean moveToPosition(int i) {
        return false;
    }

    @Override
    public boolean moveToFirst() {
        position = 0;
        return true;
    }

    @Override
    public boolean moveToLast() {
        position = size - 1;
        return true;
    }

    @Override
    public boolean moveToNext() {
        if (position < size - 1) {
            position++;
            return true;
        }
        return false;
    }

    @Override
    public boolean moveToPrevious() {
        if (position > 0) {
            position--;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFirst() {
        if (position == 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLast() {
        if (position == size - 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isBeforeFirst() {
        return false;
    }

    @Override
    public boolean isAfterLast() {
        return false;
    }

    @Override
    public int getColumnIndex(String s) {
        switch (s) {
            case MediaStore.Audio.Media.DATA:
                return 0;
            case MediaStore.Audio.Media.TITLE:
                return 1;
        }
        return -1;
    }

    @Override
    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
        return getColumnIndex(s);
    }

    @Override
    public String getColumnName(int i) {
        switch (i) {
            case 0:
                return MediaStore.Audio.Media.DATA;
            case 1:
                return MediaStore.Audio.Media.TITLE;
        }
        return null;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
        };
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public byte[] getBlob(int i) {
        return new byte[0];
    }

    @Override
    public String getString(int i) {
        if (i < 2) {
            return getColumnName(i) + "_" + position;
        }
       return null;
    }

    @Override
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

    }

    @Override
    public short getShort(int i) {
        return 0;
    }

    @Override
    public int getInt(int i) {
        return 0;
    }

    @Override
    public long getLong(int i) {
        return 0;
    }

    @Override
    public float getFloat(int i) {
        return 0;
    }

    @Override
    public double getDouble(int i) {
        return 0;
    }

    @Override
    public int getType(int i) {
        return 0;
    }

    @Override
    public boolean isNull(int i) {
        return false;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean requery() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void registerContentObserver(ContentObserver contentObserver) {

    }

    @Override
    public void unregisterContentObserver(ContentObserver contentObserver) {

    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

    }

    @Override
    public Uri getNotificationUri() {
        return null;
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override
    public void setExtras(Bundle bundle) {

    }

    @Override
    public Bundle getExtras() {
        return null;
    }

    @Override
    public Bundle respond(Bundle bundle) {
        return null;
    }
}
