package com.gowarrior.bucketmanagementdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.gowarrior.bucketmanagementdemo.Utils.LoadFileAsyncTask;
import com.gowarrior.bucketmanagementdemo.adapter.FileListAdapter;

/**
 * Created by gavin.liu on 2015/9/1.
 */
public class ContentFragment extends  Fragment {
    public static String TAG ="vFragmentCont";
    private LoadFileAsyncTask dataTask;
    private FileListAdapter mListAdapter;
    private AdapterView.OnItemClickListener mListItemClickListener;
    private Button mUpDownLoadButton;
    private Button mSaveButton;
    private View.OnClickListener mButtonOnClickListener;
    private int upDownLoadType =1;
    public boolean  isLandscape;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view;
        if(isLandscape){
            view= inflater.inflate(R.layout.content_layout_stb, container, false);
        }else {
            view = inflater.inflate(R.layout.content_layout, container, false);
        }
        ListView mListView = (ListView) view.findViewById(R.id.file_list_v);
        mListAdapter.isLandscape = isLandscape;
        mListView.setAdapter(mListAdapter);
        if(null != mListItemClickListener) {
            mListView.setOnItemClickListener(mListItemClickListener);
        }
        mUpDownLoadButton =  (Button) view.findViewById(R.id.updownloadButton);
        if(1 == upDownLoadType) {
            mUpDownLoadButton.setText(R.string.upload);
        }
        else{
            mUpDownLoadButton.setText(R.string.download);
        }
        Button mRefreshButton = (Button) view.findViewById(R.id.reflashButton);
        Button mDeletedButton = (Button) view.findViewById(R.id.deleteButton);
        mSaveButton = (Button) view.findViewById(R.id.saveButton);
        if(null != mButtonOnClickListener){
            mUpDownLoadButton.setOnClickListener(mButtonOnClickListener);
            mRefreshButton.setOnClickListener(mButtonOnClickListener);
            mDeletedButton.setOnClickListener(mButtonOnClickListener);
            mSaveButton.setOnClickListener(mButtonOnClickListener);
        }
        return view;
    }

    public void setListAdapter(FileListAdapter adapter){
        this.mListAdapter = adapter;
    }

    public LoadFileAsyncTask getDataTask(){
        return this.dataTask;
    }

    public void setDataTask(LoadFileAsyncTask task){
        this.dataTask = task;
    }

    public void setMyListItemClickListener(AdapterView.OnItemClickListener listener){
        mListItemClickListener = listener;
    }

    public void setButtonOnClickListener(View.OnClickListener listener){
        mButtonOnClickListener = listener;
    }

    public void setUpDownLoadType(int type){
        upDownLoadType = type;
        if(null != mUpDownLoadButton){
            if(1==upDownLoadType) {
                mUpDownLoadButton.setText(R.string.upload);
                mSaveButton.setVisibility(View.GONE);
            }
            else{
                mUpDownLoadButton.setText(R.string.download);
                mSaveButton.setVisibility(View.VISIBLE);
            }
        }
    }

}
