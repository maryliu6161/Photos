package com.example.liuyg.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUriTaskTest {

    @Mock
    private Context mockContext;
    
    @Mock
    private ContentResolver mockContentResolver;
    
    private DeleteUriTask deleteUriTask;
    private static final String TEST_URI_STRING = "test_uri";

    @Before
    public void setup() {
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        deleteUriTask = new DeleteUriTask(mockContext, TEST_URI_STRING);
    }

    @Test
    public void doInBackground_shouldDeleteUriFromContentProvider() {
        // Execute
        deleteUriTask.doInBackground();

        // Verify
        verify(mockContentResolver).delete(
            eq(MyContentProvider.CONTENT_URI),
            eq(UriTable.COLUMN_URI_STRING + " = ?"),
            eq(new String[]{TEST_URI_STRING})
        );
    }
} 