package com.zell.musicplayer.db;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zell.musicplayer.activities.MainActivitySetup;
import com.zell.musicplayer.helpers.FakeCursor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Properties;

public class DatabaseHelperTest extends MainActivitySetup {

    private final static String TABLE_NAME = "PROPERTIES";
    private final String PROPERTY_NAME = "propertyName";
    private final String PROPERTY_VALUE = "propertyValue";

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    @Before
    public void initDbHelper(){
        dbHelper = new DatabaseHelper(activity);
        db = mock(SQLiteDatabase.class);
    }

    @Test
    public void onCreate_ShouldExecuteQueryToCreateDbAndInsertLibraryTypeProperty(){
        dbHelper.onCreate(db);
        verify(db).execSQL(dbHelper.CREATION_QUERY);
        ContentValues property = new ContentValues();
        property.put("NAME", PropertiesList.LIBRARY_TYPE);
        property.put("VALUE", LibraryType.LIBRARY_TYPE_MEDIA_LIBRARY.getValue());
        verify(db).insert(TABLE_NAME, null, property);
    }

    @Test
    public void insertPropertyMethod_shouldExecuteQueryWithAppropriateParameters(){
        ArgumentCaptor<String> stringCaptor  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> contentCaptor  = ArgumentCaptor.forClass(ContentValues.class);

        DatabaseHelper.insertProperty(db, PROPERTY_NAME, PROPERTY_VALUE);

        verify(db).insert(stringCaptor.capture(),any(),contentCaptor.capture());

        assertThat(contentCaptor.getAllValues()).hasSize(1);
        assertThat(stringCaptor.getAllValues()).hasSize(1);

        assertStringCaptorLastValue(stringCaptor, TABLE_NAME);
        assertContentValuesCaptorLastValue(contentCaptor, PROPERTY_NAME, PROPERTY_VALUE);
    }

    @Test
    public void updateExistedProperty_shouldNotExecuteInsertPropertyMethod(){
        when(db.update(any(),any(), any(), any())).thenReturn(1);
        DatabaseHelper.updateProperty(db,PROPERTY_NAME, PROPERTY_VALUE);
        verify(db, never()).insert(any(), any(), any());
    }

    @Test
    public void updateNotExistedProperty_shouldExecuteInsertPropertyMethod(){

        updatePropertyMethod_shouldUpdateAppropriateFieldInDB();

        ArgumentCaptor<String> stringCaptor  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> contentCaptor  = ArgumentCaptor.forClass(ContentValues.class);

        verify(db).insert(stringCaptor.capture(), any(), contentCaptor.capture());

        assertThat(contentCaptor.getAllValues()).hasSize(1);
        assertThat(stringCaptor.getAllValues()).hasSize(1);

        assertStringCaptorLastValue(stringCaptor, TABLE_NAME);
        assertContentValuesCaptorLastValue(contentCaptor, PROPERTY_NAME, PROPERTY_VALUE);
    }

    @Test
    public void updatePropertyMethod_shouldUpdateAppropriateFieldInDB(){
        DatabaseHelper.updateProperty(db,PROPERTY_NAME, PROPERTY_VALUE);

        ArgumentCaptor<String> stringCaptor  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentValues> contentCaptor  = ArgumentCaptor.forClass(ContentValues.class);

        verify(db).update(stringCaptor.capture(), contentCaptor.capture(), any(), any());

        assertThat(contentCaptor.getAllValues()).hasSize(1);
        assertThat(stringCaptor.getAllValues()).hasSize(1);

        assertStringCaptorLastValue(stringCaptor, TABLE_NAME);
        assertContentValuesCaptorLastValue(contentCaptor, PROPERTY_NAME, PROPERTY_VALUE);
    }

    @Test
    public void getPropertyMethod_shouldExecuteQueryToDbAndReturnAppropriateValue() {
        Cursor cursor = spy(Cursor.class);
        when(cursor.moveToFirst())
                .thenReturn(true);
        when(cursor.getColumnIndexOrThrow(PROPERTY_NAME))
                .thenReturn(0);
        when(cursor.getString(0))
                .thenReturn(PROPERTY_VALUE);
        when(db.query(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cursor);

        String property = DatabaseHelper.getProperty(db, PROPERTY_NAME);

        verify(db).query(any(), any(), any(), any(), any(), any(), any());
        assertThat(property).isEqualTo(PROPERTY_VALUE);
    }

    @Test
    public void getPropertyMethodWithNotExistedValue_shouldReturnsNull() {
        String property = DatabaseHelper.getProperty(db, PROPERTY_NAME);
        verify(db).query(any(), any(), any(), any(), any(), any(), any());
        assertThat(property).isNull();
    }

    @Test
    public void getAllPropertyMethod_shouldReturnsSavedProperties() {

        Cursor cursor = new FakeCursor();
        List<String[]> expectedProps = ((FakeCursor)cursor).getProperties();

        when(db.query(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(cursor);

        Properties actualProps = DatabaseHelper.getAllProperties(db);
        assertThat(actualProps.size()).isEqualTo(expectedProps.size());
        expectedProps.stream().forEach(v ->{
            assertThat(actualProps.containsKey(v[1])).isTrue();
            assertThat((String)actualProps.get(v[1])).isEqualTo(v[2]);
        });

    }


    private void assertStringCaptorLastValue(ArgumentCaptor<String> stringCaptor, String expectedValue){
        List<String> values = stringCaptor.getAllValues();
        String actualValue = values.get(values.size() - 1);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    private void assertContentValuesCaptorLastValue(ArgumentCaptor<ContentValues> contentCaptor, String key, String expectedValue){

        List<ContentValues> contentValuesList = contentCaptor.getAllValues();
        ContentValues contentValues = contentValuesList.get(contentValuesList.size() - 1);
        assertThat(contentValues.get("NAME")).isNotNull().isEqualTo(key);
        assertThat(contentValues.get("VALUE")).isNotNull().isEqualTo(expectedValue);
    }
}