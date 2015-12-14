package com.snapper;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

/**
 * Created by Joel on 26/10/2015.
 */
public class Snapper extends FileObserver {
    private static final String TAG = "Snapper";
    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/Pictures/Screenshots/";

    private boolean deleteScreenshot = false;
    private OnScreenshotTakenListener mListener;
    private String mLastTakenPath;
    private Activity activity;
    private ContentObserver contentObserver;

    private Mode watchMode;

    public Snapper(OnScreenshotTakenListener listener) {
        super(PATH, FileObserver.CLOSE_WRITE);
        mListener = listener;
        watchMode = Mode.FILEOBSERVER;
    }

    public Snapper() {
        super(PATH, FileObserver.CLOSE_WRITE);
        watchMode = Mode.FILEOBSERVER;

    }

    public void useContentObserver(final Activity activity) {
        watchMode = Mode.CONTENTOBSERVER;
        this.activity=activity;
        contentObserver=new ContentObserver(handler) {
            @Override
            public boolean deliverSelfNotifications() {
                Log.d(TAG, "deliverSelfNotifications");
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Log.d(TAG, "onChange " + uri.toString());
                if (uri.toString().matches(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/[0-9]+")) {

                    Cursor cursor = null;
                    try {
                        cursor = activity.getContentResolver().query(uri, new String[]{
                                MediaStore.Images.Media.DISPLAY_NAME,
                                MediaStore.Images.Media.DATA
                        }, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            final String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                            final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            // TODO: apply filter on the file name to ensure it's screen shot event
                            Log.d(TAG, "screen shot added " + fileName + " " + path);
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                super.onChange(selfChange, uri);
            }
        };

    }

    public Mode getWatchMode() {
        return watchMode;
    }


    public void setWatcher(OnScreenshotTakenListener listener) {
        mListener = listener;
    }

    @Override
    public void onEvent(int event, String path) {
        Log.i(TAG, "Event:" + event + "\t" + path);

        if (path == null || event != FileObserver.CLOSE_WRITE)
            Log.i(TAG, "Not important");
        else if (mLastTakenPath != null && path.equalsIgnoreCase(mLastTakenPath))
            Log.i(TAG, "This event has been observed before.");
        else {
            mLastTakenPath = path;
            File file = new File(PATH + path);

            if (deleteScreenshot) {
                if (file != null)
                    file.delete();

                /*
                * A null uri is returned to listener once screenshot
                * has been deleted.
                * */
                if (mListener != null)
                    mListener.onScreenshotTaken(null);

            } else {

                if (mListener != null)
                    mListener.onScreenshotTaken(Uri.fromFile(file));
            }
        }
    }

    public void start() {
        if (watchMode.equals(Mode.FILEOBSERVER))
            super.startWatching();
        else if(watchMode.equals(Mode.CONTENTOBSERVER))
            startContentObserver();
    }

    public void stop() {
        if (watchMode.equals(Mode.FILEOBSERVER))
            super.stopWatching();
        else if(watchMode.equals(Mode.CONTENTOBSERVER))
            unregisterContentObserver();
    }

    public void unregisterContentObserver()
    {
        if(watchMode.equals(Mode.CONTENTOBSERVER))
        activity.getContentResolver().unregisterContentObserver(contentObserver);
    }

    public void deleteScreenshot(boolean deleteScreenshot) {
        this.deleteScreenshot = deleteScreenshot;
    }


    HandlerThread handlerThread = new HandlerThread("content_observer");
    //handlerThread.start();
    final Handler handler = new Handler(handlerThread.getLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void startContentObserver() {
        watchMode = Mode.CONTENTOBSERVER;
        activity.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,contentObserver);
    }
}