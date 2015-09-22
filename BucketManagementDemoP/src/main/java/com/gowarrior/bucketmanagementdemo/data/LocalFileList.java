package com.gowarrior.bucketmanagementdemo.data;

import java.util.ArrayList;

/**
 * Created by gavin.liu on 2015/9/1.
 */
public class LocalFileList {
    private ArrayList<FileNode> mFileList =null;

    public LocalFileList()
    {
       if(null==mFileList){
           mFileList = new ArrayList<FileNode>();
       }
    }
    public void addFileNode(String dir,String name)
    {

        mFileList.add(new FileNode(dir,name));
    }

    public void clearFileNodes(){
        mFileList.clear();
    }
    public void addFileNode(String dir,String name,int type)
    {
        FileNode node = new FileNode(dir,name);
        node.mFileType = type;
        node.isSelect = false;
        mFileList.add(node);
    }

    public int getFileCount(){
        if(null != mFileList){
            return mFileList.size();
        }
        return 0;
    }

    public void setFileSelectFlag(int position,boolean select){
        if(null != mFileList && position < mFileList.size()){
            mFileList.get(position).isSelect = select;
        }
    }
    public ArrayList<FileNode> getFileListData(){
        return mFileList;
    }
}
