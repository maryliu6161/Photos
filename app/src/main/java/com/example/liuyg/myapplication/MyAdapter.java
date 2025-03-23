package com.example.liuyg.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

// Replace old support imports with AndroidX
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by liuyg on 7/19/15.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView mImageView;
        public Button mShareButton;
        public Button mDeleteButton;


        public ViewHolder(View v) {
            super(v);
            mImageView = (ImageView) v.findViewById(R.id.image);
            mShareButton = (Button) v.findViewById(R.id.share);
            mDeleteButton = (Button) v.findViewById(R.id.delete);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context) {
        mContext = context;
    }

    public void setCursor(Cursor cursor) {
        if (mCursor != cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.my_view_holder, null);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Uri uri = convertCursorToUri(getCursorAt(position));
        loadBitmap(uri, holder.mImageView);
        holder.mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/jpeg");
                mContext.startActivity(Intent.createChooser(shareIntent, "Send to"));
            }
        });
        holder.mDeleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new DeleteUriTask(mContext, uri.toString()).execute();
            }
        });
    }

    public void add(String uriString) {
        new AddUriTask(mContext, uriString).execute();
    }
    /**
     * Converts a database cursor containing a URI string into a Uri object
     * 
     * @param cursor The cursor positioned at the row containing the URI string
     * @return A Uri object parsed from the URI string stored in the cursor
     * @throws IllegalArgumentException if the URI string column is not found in the cursor
     */
    private Uri convertCursorToUri(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(UriTable.COLUMN_URI_STRING);
        return  Uri.parse(cursor.getString(columnIndex));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
//        return mDataset.length;
    }

    public Cursor getCursorAt(int position) {
        mCursor.moveToPosition(position);
        System.out.println("Cursor at position " + position + ": " + mCursor.getString(mCursor.getColumnIndex(UriTable.COLUMN_URI_STRING)));
        return mCursor;
    }
    //----------------

    class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private Uri mUri;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... params) {
            mUri = params[0];
            Bitmap captureBmp = null;
            try {
                captureBmp = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return captureBmp;
        }

        // Once complete, see if ImageView is still around and set bitmap.

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    public void loadBitmap(Uri uri, ImageView imageView) {
        if (cancelPotentialWork(uri, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
//            final AsyncDrawable asyncDrawable =
//                    new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);

            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(uri);
        }
    }
    public static boolean cancelPotentialWork(Uri uri, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Uri bitmapData = bitmapWorkerTask.mUri;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || bitmapData != uri) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}