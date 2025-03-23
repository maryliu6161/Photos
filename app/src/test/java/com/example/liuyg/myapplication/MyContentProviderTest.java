package com.example.liuyg.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;
import org.junit.After;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31)
public class MyContentProviderTest {

    private MyContentProvider provider;
    private ContentProviderController<MyContentProvider> controller;

    @Before
    public void setup() {
        controller = Robolectric.buildContentProvider(MyContentProvider.class);
        provider = controller.create().get();
    }

    @After
    public void tearDown() {
        // Close the provider and its resources
        if (provider != null) {
            provider.shutdown();
        }
    }

    @Test
    public void insert_shouldStoreAndRetrieveUri() {
        // Prepare test data
        ContentValues values = new ContentValues();
        values.put(UriTable.COLUMN_URI_STRING, "test_uri");

        // Insert
        Uri insertedUri = provider.insert(MyContentProvider.CONTENT_URI, values);
        assertNotNull("Insert should return a URI", insertedUri);

        // Query to verify
        try (Cursor cursor = provider.query(
                MyContentProvider.CONTENT_URI,
                new String[]{UriTable.COLUMN_URI_STRING},
                null,
                null,
                null)) {

            assertNotNull("Query should return a cursor", cursor);
            assertEquals("Should have one record", 1, cursor.getCount());
            
            cursor.moveToFirst();
            assertEquals(
                "Retrieved URI should match inserted URI",
                "test_uri",
                cursor.getString(cursor.getColumnIndex(UriTable.COLUMN_URI_STRING))
            );
        }
    }

    @Test
    public void delete_shouldRemoveUri() {
        // Insert test data first
        ContentValues values = new ContentValues();
        values.put(UriTable.COLUMN_URI_STRING, "test_uri");
        provider.insert(MyContentProvider.CONTENT_URI, values);

        // Delete
        int deletedCount = provider.delete(
            MyContentProvider.CONTENT_URI,
            UriTable.COLUMN_URI_STRING + "=?",
            new String[]{"test_uri"}
        );

        assertEquals("Should delete one record", 1, deletedCount);

        // Verify deletion
        try (Cursor cursor = provider.query(
                MyContentProvider.CONTENT_URI,
                new String[]{UriTable.COLUMN_URI_STRING},
                UriTable.COLUMN_URI_STRING + "=?",
                new String[]{"test_uri"},
                null)) {

            assertEquals("Should have no records after deletion", 0, cursor.getCount());
        }
    }

    @Test
    public void update_shouldModifyUri() {
        // Insert test data
        ContentValues values = new ContentValues();
        values.put(UriTable.COLUMN_URI_STRING, "old_uri");
        provider.insert(MyContentProvider.CONTENT_URI, values);

        // Update
        ContentValues updateValues = new ContentValues();
        updateValues.put(UriTable.COLUMN_URI_STRING, "new_uri");
        
        int updatedCount = provider.update(
            MyContentProvider.CONTENT_URI,
            updateValues,
            UriTable.COLUMN_URI_STRING + "=?",
            new String[]{"old_uri"}
        );

        assertEquals("Should update one record", 1, updatedCount);

        // Verify update
        try (Cursor cursor = provider.query(
                MyContentProvider.CONTENT_URI,
                new String[]{UriTable.COLUMN_URI_STRING},
                UriTable.COLUMN_URI_STRING + "=?",
                new String[]{"new_uri"},
                null)) {

            assertEquals("Should find updated record", 1, cursor.getCount());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_withInvalidUri_shouldThrowException() {
        Uri invalidUri = Uri.parse("content://invalid/uri");
        provider.query(invalidUri, null, null, null, null);
    }
} 