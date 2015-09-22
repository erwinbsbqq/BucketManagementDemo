package gowarrior.com.bucketexplorer;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import gowarrior.com.bucketexplorer.adapter.DirListAdapter;

/**
 * Created by gavin.liu on 2015/9/15.
 */
public class LocalPathFragment extends Fragment{
    public boolean isShow;
    private ListView mListView;
    private Button mOkButton;
    private Button mCancelButton;
    private TextView pathText;
    private DirListAdapter mListAdapter;
    private AdapterView.OnItemClickListener mListItemClickListener;
    private View.OnClickListener mButtonOnClickListener;
    private String curPath = "/";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view;
        view = inflater.inflate(R.layout.local_path_layout, container, false);
        pathText = (TextView) view.findViewById(R.id.pathName);
        pathText.setText(curPath);
        mListView = (ListView) view.findViewById(R.id.dirList);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(mListItemClickListener);
        mOkButton = (Button) view.findViewById(R.id.pathOK);
        mCancelButton = (Button) view.findViewById(R.id.pathCancel);
        if(null != mButtonOnClickListener){
            mOkButton.setOnClickListener(mButtonOnClickListener);
            mCancelButton.setOnClickListener(mButtonOnClickListener);
        }
        return view;
    }

    public void setListAdapter(DirListAdapter adapter){
        this.mListAdapter = adapter;
    }

    public void setListItemClickListener(AdapterView.OnItemClickListener listener){
        mListItemClickListener = listener;
    }

    public void setButtonOnClickListener(View.OnClickListener listener){
        mButtonOnClickListener = listener;
    }

    public void setPathName(String path){
        this.curPath = path;
        if(null != pathText) {
            pathText.setText(path);
        }
    }

    public boolean isPassUpKey(){
        if(isShow){
            if(mListView.isFocused()){
                if(0 == mListView.getSelectedItemPosition())
                    return false;
            }
            else if(0 == mListAdapter.getCount()){
                return false;
            }
        }
        return true;

    }
}
