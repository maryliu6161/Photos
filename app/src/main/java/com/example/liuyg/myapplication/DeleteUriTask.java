package com.example.liuyg.myapplication;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by liuyg on 7/19/15.
 */
public class DeleteUriTask extends AsyncTask<Void, Void, Void>{
    Context mContext;
    String mString;

    public DeleteUriTask(Context context, String s) {
        mContext = context;
        mString = s;
    }
    @Override
    protected Void doInBackground(Void... params) {
        mContext.getContentResolver().delete(MyContentProvider.CONTENT_URI, UriTable.COLUMN_URI_STRING + " = ?", new String[]{mString});
        return null;
    }
}
