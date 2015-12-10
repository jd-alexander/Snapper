package com.screen.watch;

import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

import java.io.File;

/**
 * Created by Joel on 26/10/2015.
 */
public class ScreenWatch extends FileObserver {
    private static final String TAG = "ScreenWatch";
    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/Pictures/Screenshots/";

    private boolean deleteScreenshot=false;
    private OnScreenshotTakenListener mListener;
    private String mLastTakenPath;

    public ScreenWatch(OnScreenshotTakenListener listener) {
        super(PATH, FileObserver.CLOSE_WRITE);
        mListener = listener;
    }

    public ScreenWatch() {
        super(PATH, FileObserver.CLOSE_WRITE);
    }

    public void setWatcher(OnScreenshotTakenListener listener)
    {
        mListener=listener;
    }

    @Override
    public void onEvent(int event, String path) {
        Log.i(TAG, "Event:" + event + "\t" + path);

        if (path==null || event!=FileObserver.CLOSE_WRITE)
            Log.i(TAG, "Not important");
        else if (mLastTakenPath!=null && path.equalsIgnoreCase(mLastTakenPath))
            Log.i(TAG, "This event has been observed before.");
        else {
            mLastTakenPath = path;
            File file = new File(PATH+path);

            if(deleteScreenshot)
            {
                if(file!=null)
                    file.delete();

                /*
                * A null uri is returned to listener once screenshot
                * has been deleted.
                * */
                if(mListener!=null)
                    mListener.onScreenshotTaken(null);

            }
            else
            {

                if(mListener!=null)
                    mListener.onScreenshotTaken(Uri.fromFile(file));
            }
        }
    }

    public void start() {
        super.startWatching();
    }

    public void stop() {
        super.stopWatching();
    }

    public void deleteScreenshot(boolean deleteScreenshot) {
        this.deleteScreenshot = deleteScreenshot;
    }


}