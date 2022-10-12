package com.zell.musicplayer.helpers.cursors;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class FakeCursorForDatabaseHelper implements Cursor {

    private final int size = 5;
    private int position;

    public List<String[]> getProperties() {
        List<String[]> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new String[]{String.valueOf(i), "PROPERTY" + i, "VALUE" + i});
        }
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
        position++;
        return true;
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
        if (position > size - 1) {
            return true;
        }
        return false;
    }

    @Override
    public int getColumnIndex(String s) {
        switch (s) {
            case "_id":
                return 0;
            case "NAME":
                return 1;
            case "VALUE":
                return 2;
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
                return "_id";
            case 1:
                return "NAME";
            case 2:
                return "VALUE";
        }
        return null;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"_id", "NAME", "VALUE"};
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public byte[] getBlob(int i) {
        return new byte[0];
    }

    @Override
    public String getString(int i) {
        String[] value = {String.valueOf(position), "PROPERTY" + position, "VALUE" + position};
        if (i < 3) {
            return value[i];
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
