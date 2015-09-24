package com.gowarrior.bucketmanagementdemo.Utils;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import com.gowarrior.bucketmanagementdemo.data.CloudFileList;
import com.gowarrior.bucketmanagementdemo.data.FileNode;
import com.gowarrior.bucketmanagementdemo.data.ProcNode;
import com.gowarrior.bucketmanagementdemo.data.LocalFileList;


/**
 * Created by gavin.liu on 2015/9/2.
 */
public class FileListManager {
    public static final int TYPE_LOCAL =0;
    public static final int TYPE_CLOUD =1;
    private static final  String TAG = "FileListManager";
    public CloudTool cloudTool;
    private  String LOCAL_PATH="/data/data/com.gowarrior.bucketmanagementdemo";
    private  String CLOUD_PATH="Cloud://";
    private LocalFileList mLocalList;
    private CloudFileList mCloudList;
    private int mCurrentType = TYPE_LOCAL;
    public int pules;


    public FileListManager(Context context,Handler handler,String path){
        if(null != path){
            Log.i(TAG, path);
            LOCAL_PATH = path;
        }
        File dir = new File(LOCAL_PATH);
        if(!dir.exists()){
            dir.mkdirs();
        }
        mLocalList = new LocalFileList();
        mCloudList = new CloudFileList();
        cloudTool = new CloudTool(context,handler);
    }

    public void loadLocalFileList(){
        String name;
        String path;
        int fileType = FileNode.FILE_TYPE_UNKONWN;
        mLocalList.clearFileNodes();
        if (LOCAL_PATH == null || LOCAL_PATH.isEmpty()) {
            return ;
        }
        File dir = new File(LOCAL_PATH);
        if (!dir.isDirectory()) {
            return ;
        }
        File[] fileList = dir.listFiles();
        if(fileList == null) {
            return ;
        }

        for (int i = 0; i < fileList.length; i++) {

            File file = fileList[i];
            if (!file.exists()) {
                break;
            }
            name = file.getName();
            if(file.isDirectory()){
                fileType = FileNode.FILE_TYPE_DIR;
                continue;
            }else{
                fileType = getFileType(name);
            }
            if(name.startsWith(".")){
                continue;
            }
            path = file.getAbsolutePath();
            mLocalList.addFileNode(path, name, fileType);
        }
    }

    public int  deleteLocalFiles(){
        int ret=0;
        String path;
        ArrayList<FileNode> fileList = mLocalList.getFileListData();
        for (int i = 0; i < fileList.size(); i++) {
            if(fileList.get(i).isSelect) {
                path = fileList.get(i).getDirPath();
                File file = new File(path);
                file.delete();
                ret++;
                fileList.get(i).isSelect = false;
                //fileList.remove(i);
            }

        }
        return ret;
    }

    public void cancelLocalSelect(){
        ArrayList<FileNode> fileList = mLocalList.getFileListData();
        for (int i = 0; i < fileList.size(); i++) {
            if(fileList.get(i).isSelect) {
                fileList.get(i).isSelect = false;
            }
        }
    }

    public int loadCloudFileList(){
        if(! cloudTool.isReady()){
            Log.e(TAG,"cloud service not ready !");
            return -1;
        }
        List<String> list = cloudTool.getFileList();
        if(null == list){
            Log.e(TAG,"cloud server error !");
            return -1;
        }
        mCloudList.clearFileNodes();
        for (int i = 0; i < list.size(); i++) {
            mCloudList.addFileNode(CLOUD_PATH, list.get(i), FileNode.FILE_TYPE_NET);
        }
        return  mCloudList.getFileCount();
    }

    public int deleteCloudFiles(){
        int ret =0;
        String object;
        if(cloudTool.isReady()){
            ArrayList<FileNode> fileList = mCloudList.getFileListData();
            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).isSelect) {
                    object = fileList.get(i).mFileName;
                    cloudTool.deleteFile(object);
                    ret++;
                }
            }
        }else{
            Log.e(TAG,"cloud service unbind !");
            ret = -1;
        }
        return ret;
    }

    public void cancelCloudSelect(){
        ArrayList<FileNode> fileList = mCloudList.getFileListData();
        for (int i = 0; i < fileList.size(); i++) {
            if(fileList.get(i).isSelect) {
                fileList.get(i).isSelect = false;
            }
        }
    }

    public int downloadFile(String pathDownload){
        int ret =0;
        int id;
        String path ;
        String object;
        if(null == pathDownload)
            path  = LOCAL_PATH ;
        else
            path =pathDownload;
        ArrayList<FileNode> fileList = mCloudList.getFileListData();
        mCloudList.getDownProcList().clear();
        FileNode fNode;
        for (int i = 0; i < fileList.size(); i++) {
            fNode = fileList.get(i);
            if(fNode.isSelect) {
                object = fNode.mFileName;
                id = cloudTool.downloadFile(object, path);
                if(id > -1){
                    ProcNode node = new ProcNode();
                    node.id= id;
                    node.object = object;
                    node.percent =0;
                    node.idx = i;
                    fNode.percent =0;
                    mCloudList.getDownProcList().add(node);
//                    fileType = getFileType(object);
//                    mLocalList.addFileNode(LOCAL_PATH, object, fileType);
                    ret ++;
                }

            }

        }
        return ret;
    }

    public void cloudSelectAll(){
        ArrayList<FileNode> fileList = mCloudList.getFileListData();
        for (int i = 0; i < fileList.size(); i++) {
            fileList.get(i).isSelect = true;
        }
    }

    public boolean isDownloadFinish(){
        boolean ret = true;
        int percent;
        int size = mCloudList.getDownProcList().size();
        for(int i=0;i<size;i++) {
            percent =  mCloudList.getDownProcList().get(i).percent;
            if((-1 < percent)&&(100 > percent)){
                ret = false;
                break;
            }
        }
        return ret;
    }

    public int uploadFile(){
        int ret =0;
        int id;
        String file;
        FileNode fNode;
        ArrayList<FileNode> fileList = mLocalList.getFileListData();
        mCloudList.getUpProcList().clear();
        for (int i = 0; i < fileList.size(); i++) {
            fNode = fileList.get(i);
            if(fNode.isSelect) {
                file = fNode.mDirPath;
                id = cloudTool.uploadFile(file);
                if(id > -1){
                    ProcNode node = new ProcNode();
                    node.id= id;
                    node.idx = i;
                    node.object = fileList.get(i).mFileName;
                    node.percent =0;
                    fNode.percent = 0;
                    mCloudList.getUpProcList().add(node);
//                    fileType = getFileType(object);
//                    mLocalList.addFileNode(LOCAL_PATH, object, fileType);
                    ret ++;
                }

            }

        }
        return ret;
    }

    public boolean isUploadFinish(){
        boolean ret = true;
        int percent;
        int size = mCloudList.getUpProcList().size();
        for(int i=0;i<size;i++) {
            percent =  mCloudList.getUpProcList().get(i).percent;
            if((-1 < percent)&&(100 > percent)){
                ret = false;
                break;
            }
        }
        return ret;
    }

    public LocalFileList getLocalList(){
        return mLocalList;
    }

    public CloudFileList getCloudList(){
        return mCloudList;
    }

    public int getCurrentType(){
            return mCurrentType;
    }

    public void setCurrentType(int type){
         mCurrentType = type;
    }

    private int getFileType(String name){
        return FileNode.FILE_TYPE_UNKONWN;
    }


    public void progressUpdate(boolean isDownload) {
        ProcNode node;
        int size;
        if(isDownload) {
            size = mCloudList.getDownProcList().size();
            for (int i = 0; i < size; i++) {
                node = mCloudList.getDownProcList().get(i);
                node.percent = cloudTool.getUpDownLoadProgress(node.id);
                if (-1 != node.percent) {
                    ArrayList<FileNode> fileList = mCloudList.getFileListData();
                    fileList.get(node.idx).percent = node.percent;
                }
            }
        }else{
            size = mCloudList.getUpProcList().size();
            for (int i = 0; i < size; i++) {
                node = mCloudList.getUpProcList().get(i);
                node.percent = cloudTool.getUpDownLoadProgress(node.id);
                if (-1 != node.percent) {
                    ArrayList<FileNode> fileList = mLocalList.getFileListData();
                    fileList.get(node.idx).percent = node.percent;
                }
            }
        }
    }

    public void setSelectedFlag(int position, boolean select) {
        FileNode fNode;
        if(TYPE_LOCAL == mCurrentType){
            if(position >= mLocalList.getFileListData().size()){
                return;
            }
            fNode = mLocalList.getFileListData().get(position);
        }else {
            if(position >= mCloudList.getFileListData().size()){
                return;
            }
            fNode = mCloudList.getFileListData().get(position);
        }
        fNode.isSelect = select;
    }
}
