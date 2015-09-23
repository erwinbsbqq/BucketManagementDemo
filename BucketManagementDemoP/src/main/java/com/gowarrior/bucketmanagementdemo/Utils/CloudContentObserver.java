package com.gowarrior.bucketmanagementdemo.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;

import android.os.Handler;
import android.util.Log;


/**
 * Created by gavin.liu on 2015/9/21.
 */
public class CloudContentObserver extends ContentObserver {
    public static final int UPDATE_TICK_MSG = 1001;
    private Handler mHandler;
//    private ContentResolver resolver;

    public CloudContentObserver(Context context,Handler handler){
        super(handler);
        mHandler = handler;
//        resolver =  context.getContentResolver();
    }

    @Override
    public void onChange(final boolean selfChange) {
        if(null != mHandler){
            Log.i("CloudContentObserver", "onChange");
            mHandler.sendEmptyMessage(UPDATE_TICK_MSG);
        }
    }

}
