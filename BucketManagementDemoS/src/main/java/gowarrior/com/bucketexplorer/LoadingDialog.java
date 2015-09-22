package gowarrior.com.bucketexplorer;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by gavin.liu on 2015/9/8.
 */
public class LoadingDialog extends DialogFragment {
        private String title;
        public LoadingDialog(){

        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if(null != title) {
                getDialog().setTitle(title);
            }
            View v = inflater.inflate(R.layout.loading_layout, container,false);
//        TextView tv = (TextView)v.findViewById(R.id.prompt_message);
//        tv.setText(getPrompt());
//
            return v;
        }

    public void setTitle(String title){
        this.title = title;
    }

    
}