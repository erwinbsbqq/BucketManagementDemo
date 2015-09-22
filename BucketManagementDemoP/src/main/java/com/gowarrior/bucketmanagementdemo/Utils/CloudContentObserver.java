package com.gowarrior.bucketmanagementdemo.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;

import android.os.Handler;


/**
 * Created by gavin.liu on 2015/9/21.
 */
public class CloudContentObserver extends ContentObserver {
    private final static String TAG = "CloudContentObserver";
    public static final int UPDATE_TICK_MSG = 1001;
    private Handler mHandler;
    private Context mContext;
    private ContentResolver resolver;

    public CloudContentObserver(Context context,Handler handler){
        super(handler);
        mHandler = handler;
        mContext = context;
        resolver =  mContext.getContentResolver();
    }

    @Override
    public void onChange(final boolean selfChange) {
        if(null != mHandler){
            mHandler.sendEmptyMessage(UPDATE_TICK_MSG);
        }
    }

}
