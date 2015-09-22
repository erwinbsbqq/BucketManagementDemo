package gowarrior.com.bucketexplorer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by gavin.liu on 2015/9/14.
 */
public class PathButton extends Button {

    public PathButton(Context context,Drawable drawableRight,Drawable drawableBack,float weight){
        super(context,null);
        initial(drawableRight,drawableBack,weight);
    }

    public PathButton(Context context){
        super(context, null);
    }

    public PathButton(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public PathButton(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
    }

    private void initial(Drawable drawableRight,Drawable drawableBack,float weight){
        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                weight);
        super.setLayoutParams(params);
        super.setEllipsize(TextUtils.TruncateAt.START);
        super.setSingleLine();
        super.setCompoundDrawables(null, null, drawableRight, null);
        super.setBackgroundResource(R.drawable.button_selector);
        super.setTextColor(0xff000000);
    }

    public void setWeight(float weight){
        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                weight);
        super.setLayoutParams(params);
    }
}
