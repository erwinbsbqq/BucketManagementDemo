package com.gowarrior.bucketmanagementdemo.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by gavin.liu on 2015/9/21.
 */
public class CloudTool{
    private final static String TAG = "CloudTool";
    public static final String AUTHORITY = "com.gowarrior.cloudq.CWSBucketService.CWSBucketProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/files");

    public static final String FILENAME = "fname";
    public static final String FILETYPE = "ftype";
    public static final String STATE = "state";
    public static final String CACHEDURI = "uri";
    public static final String DATE = "fdate";
    public static final String FILESIZE = "fsize";
    public static final String TRANSFERTYPE = "ttype";
    public static final String TRANSFERSIZE = "tsize";

    private int mFileCnt;
    private  ContentResolver resolver;
    private boolean isLog = false;
    private CloudContentObserver contentObserver;

    public CloudTool(Context context,Handler handler){
        resolver =  context.getContentResolver();
        contentObserver = new CloudContentObserver(context,handler);
        resolver.registerContentObserver(CONTENT_URI,true,contentObserver);
    }

    public boolean isReady() {
        boolean ready = false;
        ContentValues Values = new ContentValues();
        int ret = resolver.update(CONTENT_URI, Values, "ready?", null);
        if (ret == 1) {
            Log.v(TAG, "provider db is ready!");
            ready = true;
        }
        return ready;
    }

    public List<String> getFileList() {

        List<String> list = new LinkedList<String>();
        Cursor c = resolver.query(CONTENT_URI, null, null, null, "_id ASC");
        if (c != null) {
            while (c.moveToNext()) {
                if (!c.getString(c.getColumnIndex(STATE)).equals("delete")) {
                    list.add(c.getString(c.getColumnIndex(FILENAME)) + "." + c.getString(c.getColumnIndex(FILETYPE)));
                }
            }
            c.close();
        }
        mFileCnt = list.size();
        return list;
    }

    public int uploadFile(String absolutePath){
        int id = -1;
        String name;
        String type;
        name = getFileName(absolutePath);
        type = getExtensionName(name);
        long size = 0;
        try {
            size = getFileSize(new File(absolutePath));
        }catch (Exception e){
            e.printStackTrace();
        }
        ContentValues values = new ContentValues();
        values.put(this.FILENAME, name);
        values.put(this.FILETYPE, type);
        values.put(this.FILESIZE, size);
        values.put(STATE, "upload");
        values.put(this.CACHEDURI, absolutePath);
        Uri uri = resolver.insert(CONTENT_URI, values);
        if(null != uri){
            id = getIdxfromUri(uri);
        }
        return id;
    }

    public int uploadFile(String dir,String fileName){
        String absolutePath = dir+ File.separator+fileName;
        int id = uploadFile(absolutePath);
        return id;
    }

    public int downloadFile(String object,String localDir){
        int id = getCursorIdx(this.FILENAME, getFileName(object));
        if(id > -1 ) {
            ContentValues updateValues = new ContentValues();
            Uri updateIdUri = ContentUris.withAppendedId(CONTENT_URI, id);
            updateValues.put(this.STATE, "download");
            updateValues.put(this.CACHEDURI, localDir+File.separator+object);
            resolver.update(updateIdUri, updateValues, null, null);
        }
        return id;
    }

    public int getUpDownLoadProgress(int id){
        int ret =0;
        Uri  uri = ContentUris.withAppendedId(CONTENT_URI, id);
        Cursor cursor = resolver.query(uri, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String state =  cursor.getString(cursor.getColumnIndex(STATE));
                if("ERROR".equalsIgnoreCase(state)) {
                    ret = -1;
                }else if("IN_PROGRESS".equalsIgnoreCase(state)){
                    long fSize = cursor.getLong(cursor.getColumnIndex(FILESIZE));
                    long tSize = cursor.getLong(cursor.getColumnIndex(TRANSFERSIZE));
                    if(fSize > 0 ){
                        ret = (int)(tSize/fSize);
                    }
                }else if("COMPLETE".equalsIgnoreCase(state)){
                    ret = 100;
                }
            }
            cursor.close();
        }
        return ret;
    }
    public int  deleteFile(String object){
        int id = getCursorIdx(FILENAME, getFileName(object));
        if (id > -1) {
            Uri deleteIdUri = ContentUris.withAppendedId(CONTENT_URI, id);
            resolver.delete(deleteIdUri, null, null);
        } else {
            Log.v(TAG, "deleteFile error! object " + object + "not found");
        }
        return id;
    }

    public boolean syncWithCloud()  {
        if (resolver.update(CONTENT_URI, null, "refresh", null) != -1) {
           return true;
        } else {
            return false;
        }
    }

    private String getFileName(String absolutePath){
        int start=absolutePath.lastIndexOf("/");
        int end=absolutePath.lastIndexOf(".");
        if(end!=-1){
            return absolutePath.substring(start+1,end);
        }else{
            return null;
        }

    }

    private int getCursorIdx ( String pattern,String value) {
        Cursor cursor = resolver.query(CONTENT_URI, null, null, null, null, null);
        int pos, id = -1;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex(pattern)).equals(value)) {
                    pos = cursor.getPosition();
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
                    if (isLog) {
                        Log.v(TAG, "hit object " + pos + " id=" + id);
                        printCursor(cursor);
                    }
                    break;
                }
            }
            cursor.close();
        }
        return id;
    }

    private int getIdxfromUri(Uri uriI) {
        String id = uriI.toString().substring(uriI.toString().lastIndexOf('/')+1);
        return Integer.valueOf(id);
    }

    private  String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    private long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
            fis.close();
        }
        return size;
    }

    private  void printCursor(Cursor cursor) {
        String myDate =cursor.getString(cursor.getColumnIndex(this.DATE));
        if (myDate != null) {
            Date date = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                //Log.v(TAG, "myDate=" + myDate);
                date = format.parse(myDate);
                myDate = date.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isLog) {
            Log.v(TAG, "object" + cursor.getInt(cursor.getColumnIndex("_id")) + ", fname=" + cursor.getString(cursor.getColumnIndex(this.FILENAME)) +
                    ", ftype=" + cursor.getString(cursor.getColumnIndex(this.FILETYPE)) +
                    ", state=" + cursor.getString(cursor.getColumnIndex(this.STATE)) +
                    ", uri=" + cursor.getString(cursor.getColumnIndex(this.CACHEDURI)) +
                    ", date=" + myDate + ", fsize=" + cursor.getLong(cursor.getColumnIndex(this.FILESIZE)));
        }
    }

}
