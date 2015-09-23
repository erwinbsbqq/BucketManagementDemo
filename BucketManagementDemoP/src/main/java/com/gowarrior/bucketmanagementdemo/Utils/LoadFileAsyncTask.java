package com.gowarrior.bucketmanagementdemo.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.gowarrior.bucketmanagementdemo.BucketManagementDemo;
import com.gowarrior.bucketmanagementdemo.LoadingDialog;

/**
 * Created by gavin.liu on 2015/9/2.
 */
public class LoadFileAsyncTask extends AsyncTask<String, Integer, FileListManager> {
    public static final int PROC_IDLE=0;
    public static final int PROC_SYNC=1;
    public static final int PROC_DELETE =2;
    public static final int PROC_DOWNLOAD =4;
    public static final int PROC_UPWNLOAD =8;
    private int mState = PROC_IDLE;
    private boolean isCompleted =false;
    private String viewTag="LOADING";
    private BucketManagementDemo mActivity;
    private FileListManager mFileListManager;
    private LoadingDialog mLoadingDialog;
    private int waitTimes = 100;
    private boolean isLoop = true;
    public LoadFileAsyncTask(BucketManagementDemo activity,FileListManager manager){
        this.mActivity = activity;
        this.mFileListManager = manager;
    }

    @Override
    protected void onPreExecute() {
        isLoop = true;
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.setTitle(viewTag);

        //mLoadingDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mLoadingDialog.show(mActivity.getFragmentManager(), viewTag);
    }

    @Override
    protected FileListManager doInBackground(String... params){
        int ret;
        int loop = waitTimes;
        if("sync-all".equals(params[0]) || "sync-local".equals(params[0])){
            mState |= PROC_SYNC;
            mFileListManager.loadLocalFileList();
        }
        if("sync-all".equals(params[0]) || "sync-cloud".equals(params[0])){
            mState |= PROC_SYNC;
            boolean syncOK = false;

            while(isLoop) {
                if(syncOK) {
                    ret = mFileListManager.loadCloudFileList();
                }else{
                    ret = -1;
                    syncOK = mFileListManager.cloudTool.syncWithCloud();
                }
                if(-1 != ret){
                    break;
                }
                loop--;
                if(loop > 0) {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    break;
                }
            }
        }
        else if("upload".equals(params[0])){
            mState |= PROC_UPWNLOAD;
           mFileListManager.uploadFile();
            loop = waitTimes;
            int pules = mFileListManager.pules;
            while (isLoop){
                if(mFileListManager.isUploadFinish()){
                    mFileListManager.cancelLocalSelect();
                    Log.i("LoadFileAsyncTask", "[p]upload finish !");
                    break;
                }
                if(pules == mFileListManager.pules){
                    Log.i("LoadFileAsyncTask", "[p]upload pules= "+mFileListManager.pules+" loop="+loop);
                    loop--;
                }else{
                    loop = waitTimes;
                    pules =  mFileListManager.pules;
                    mFileListManager.progressUpdate(false);
                    publishProgress(loop);
                }
                if(loop > 0) {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    break;
                }

            }
            mFileListManager.loadCloudFileList();
        }
        else if("download".equals(params[0])){
            String dPath = null;
            if(params.length > 1) {
                dPath = params[1];
                Log.i("LoadFileAsyncTask", dPath);
            }
            mState |= PROC_DOWNLOAD;
           mFileListManager.downloadFile(dPath);
            loop = waitTimes;
            int pules = mFileListManager.pules;
            while (isLoop){
                if(mFileListManager.isDownloadFinish()){
                    mFileListManager.cancelCloudSelect();
                    Log.i("LoadFileAsyncTask", "[p]download finish !");
                    break;
                }

                if(pules == mFileListManager.pules){
                    Log.i("LoadFileAsyncTask", "[p]download pules= "+mFileListManager.pules +"loop="+loop);
                    loop--;
                }else{
                    loop = waitTimes;
                    pules =  mFileListManager.pules;
                    mFileListManager.progressUpdate(true);
                    publishProgress(loop);
                }

                if(loop > 0){
                    try {
                        Thread.sleep(300);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    break;
                }

            }
            mFileListManager.loadLocalFileList();
        }
        else if("del-local".equals(params[0])){
            mState |= PROC_DELETE;
            ret = mFileListManager.deleteLocalFiles();
            if(ret > 0)
                mFileListManager.loadLocalFileList();
        }
        else if("del-cloud".equals(params[0])){
            mState |= PROC_DELETE;
            ret = mFileListManager.deleteCloudFiles();
            if(ret >0 ){
                mFileListManager.loadCloudFileList();
            }
        }else if("init".equals(params[0])){
            mState |= PROC_SYNC;
            mFileListManager.loadLocalFileList();
            while(isLoop) {
                if (mFileListManager.cloudTool.isReady()) {
                    mFileListManager.loadCloudFileList();
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("LoadFileAsyncTask","wati cloud service ready !!!");
            }
        }
        return mFileListManager;
    }

    @Override
    protected void onPostExecute(FileListManager obj) {
        isCompleted = true;
        notifyActivityTaskCompleted();
        if(null != mLoadingDialog){
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }

    }

    @Override
    protected void onCancelled(FileListManager obj) {
        super.onCancelled(obj);
    }

    public void setActivity(BucketManagementDemo activity){
        this.mActivity = activity;
        if(null==activity){
            if(null != mLoadingDialog){
                mLoadingDialog.dismiss();
                mLoadingDialog = null;
            }
            isLoop = false;
        }else{
            if(isCompleted){
                notifyActivityTaskCompleted();
            }
//            else{
//                mLoadingDialog = new LoadingDialog();
//                mLoadingDialog.show(activity.getFragmentManager(), viewTag);
//            }

        }

    }

    private void notifyActivityTaskCompleted(){
        if(null != mActivity){
            mActivity.onTaskCompleted(mState);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress)  {
        super.onProgressUpdate(progress);
        if(null != mActivity){
            mActivity.onTaskRefresh();
        }

    }

    public void setViewTag(String tag){
        if(null !=tag ){
            this.viewTag = tag;
        }

    }

    public void cancelTask(){
        isLoop = false;
    }

}
