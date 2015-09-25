package com.gowarrior.bucketmanagementdemo.data;

/**
 * Created by gavin.liu on 2015/9/1.
 */
public class FileNode {
    public static final int FILE_TYPE_DIR = 0;
    public static final int FILE_TYPE_VIDEO = 1;
    public static final int FILE_TYPE_MUSIC = 2;
    public static final int FILE_TYPE_PIC = 3;
    public static final int FILE_TYPE_NET = 4;
    public static final int FILE_TYPE_UNKONWN = 16;
    public String mDirPath;
    public String mFileName;
    public int mFileType;
    public int percent;
    public boolean isSelect;
    public FileNode(String dir,String name)
    {
        mDirPath = dir;
        mFileName = name;
        mFileType = FILE_TYPE_UNKONWN;
        isSelect = false;
        percent =0;
    }
    public FileNode(){

    }

    public FileNode cloneWithoutDir(){
        FileNode newNode = new FileNode();
        newNode.mDirPath = null;//this.mDirPath;
        newNode.mFileName = this.mFileName;
        newNode.mFileType = this.mFileType;
        newNode.percent = this.percent;
        newNode.isSelect = this.isSelect;
        return newNode;
    }
    public String getDirPath()
    {
        return mDirPath;
    }

    public String getFileName() {
        return mFileName;
    }
}
