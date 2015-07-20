package com.example.liuyg.myapplication;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by liuyg on 7/19/15.
 */
public class AddUriTask extends AsyncTask<Void, Void, Void> {

    Context mContext;
    String mString;

    public AddUriTask(Context context, String uriString) {
        super();
        mContext = context;
        mString = uriString;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ContentValues values = new ContentValues();
        values.put(UriTable.COLUMN_URI_STRING, mString);
        long id = System.currentTimeMillis();
        mContext.getContentResolver().insert(ContentUris.withAppendedId(MyContentProvider.CONTENT_URI, id), values);
        return null;
    }
}
