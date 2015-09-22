package gowarrior.com.bucketexplorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import gowarrior.com.bucketexplorer.R;
import gowarrior.com.bucketexplorer.data.FileNode;

/**
 * Created by gavin.liu on 2015/9/16.
 */
public class DirListAdapter  extends BaseAdapter {
    private ArrayList<FileNode> mDataList;
    public LayoutInflater mInflater;
    private Context mContext;
    public DirListAdapter(ArrayList<FileNode> list, Context ctx){
        this.mContext = ctx;
        this.mDataList = list;
        mInflater = LayoutInflater.from(ctx);
    }
    public void setData(ArrayList<FileNode> list) {
        this.mDataList = list;
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

        DirListHolder holder;
        if (convertView == null) {
            holder = new DirListHolder();
            convertView = mInflater.inflate(R.layout.dir_list_item, parent,
                    false);
            holder.fileImage = (ImageView)convertView
                    .findViewById(R.id.dir_image);
            holder.fileName = (TextView) convertView
                    .findViewById(R.id.dir_name);

            convertView.setTag(holder);
        } else {
            holder = (DirListHolder) convertView.getTag();
        }

        FileNode fNode =  mDataList.get(position);

        holder.fileName.setText(fNode.mFileName);
        return convertView;
    }

    public String getAbsolutePath(int position){
        FileNode fNode =  mDataList.get(position);
        return fNode.mDirPath;
    }

    public String getDirName(int position){
        FileNode fNode =  mDataList.get(position);
        return fNode.mFileName;
    }

}
