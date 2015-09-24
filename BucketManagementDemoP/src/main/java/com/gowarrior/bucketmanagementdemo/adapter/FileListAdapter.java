package com.gowarrior.bucketmanagementdemo.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gowarrior.bucketmanagementdemo.R;
import com.gowarrior.bucketmanagementdemo.data.FileNode;
import java.util.ArrayList;



/**
 * Created by gavin.liu on 2015/9/2.
 */
public class FileListAdapter extends BaseAdapter {
    private ArrayList<FileNode> mDataList;
    public boolean  isLandscape=true;
    public LayoutInflater mInflater;
    private Context mContext;
    public FileListAdapter(ArrayList<FileNode> list, Context ctx){
        this.mContext = ctx;
        mDataList = new ArrayList<FileNode>();
        setData(list);
        //this.mDataList = list;
        mInflater = LayoutInflater.from(ctx);
    }
    public void setData(ArrayList<FileNode> list) {
        mDataList.clear();
        for(int i=0;i<list.size();i++) {
            mDataList.add(list.get(i).clone());
        }
//        this.mDataList = list;
    }

    public void updateState(ArrayList<FileNode> list) {
        for(int i=0;i<list.size();i++) {
            if (i < mDataList.size()) {
                mDataList.get(i).percent = list.get(i).percent;
                mDataList.get(i).isSelect = list.get(i).isSelect;
            }
        }
    }
    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mDataList.get(arg0);
       // return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FileListHolder holder = null;
        if (convertView == null) {
            holder = new FileListHolder();
            convertView = mInflater.inflate(R.layout.file_list_item, parent,
                    false);
            holder.fileImage = (ImageView)convertView
                    .findViewById(R.id.file_image);
            holder.fileName = (TextView) convertView
                    .findViewById(R.id.file_name);
            if(isLandscape){
                holder.fileName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            }

            holder.cb = (CheckBox)convertView
                    .findViewById(R.id.item_cb);

            holder.progress = (ProgressBar)convertView
                    .findViewById(R.id.progressBar);
            convertView.setTag(holder);
        } else {
            holder = (FileListHolder) convertView.getTag();
        }

        FileNode fNode =  mDataList.get(position);
        holder.cb.setSelected(fNode.isSelect);
        holder.cb.setChecked(fNode.isSelect);
        holder.progress.setProgress(fNode.percent);

//        if(FileNode.FILE_TYPE_DIR == fnode.mFileType){
//            holder.fileImage.setImageResource(R.drawable.folder_32x32);
//        }
//        else if(FileNode.FILE_TYPE_NET == fnode.mFileType) {
//            holder.fileImage.setImageResource(R.drawable.server_32x32);
//        }else {
//            holder.fileImage.setImageResource(R.drawable.file_32x32);
//        }
        holder.fileName.setText(fNode.mFileName);
        return convertView;
    }


    public void setSelectedFlag(int position, boolean select) {
        FileNode fNode = mDataList.get(position);
        fNode.isSelect = select;
    }

    public void selectedAll() {
        for(int i=0;i<mDataList.size();i++){
            mDataList.get(i).isSelect = true;
        }
    }

    public void cancelSelectedAll() {
        for(int i=0;i<mDataList.size();i++){
            mDataList.get(i).isSelect = false;
        }
    }

    public boolean isItemSelect(){
        for(int i=0;i<mDataList.size();i++){
            if(mDataList.get(i).isSelect){
                return true;
            }
        }
        return false;
    }
}
