package gowarrior.com.bucketexplorer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.graphics.drawable.Drawable;
import android.widget.AdapterView;
import android.widget.TextView;

import gowarrior.com.bucketexplorer.Utils.FileListManager;
import gowarrior.com.bucketexplorer.Utils.LoadFileAsyncTask;
import gowarrior.com.bucketexplorer.adapter.DirListHolder;
import gowarrior.com.bucketexplorer.adapter.FileListAdapter;
import gowarrior.com.bucketexplorer.adapter.FileListHolder;
import gowarrior.com.bucketexplorer.adapter.DirListAdapter;
import gowarrior.com.bucketexplorer.data.LocalDirList;

import com.gowarrior.cloudq.CWSBucketService.ICWSBucketAidlInterface;

import java.io.File;
import java.util.List;

public class BrowserActivity extends Activity implements View.OnClickListener
        ,AdapterView.OnItemClickListener {
    private static String TAG ="BrowserActivity";

    private ContentFragment mContentFrag;
    private Button mLocalButton;
    private Button mCloudButton;
    private TextView mExportTitleText;
    private FileListManager mFileListMag =null;
    private LoadFileAsyncTask mLoadTask;
    private FileListAdapter mAdapter;
    private int mProcState =LoadFileAsyncTask.PROC_IDLE;
    private ICWSBucketAidlInterface myBucket;
    private  Drawable drawableSelect;
    private DirListAdapter mPathAdapter;
    private LocalDirList mLocalDirList;
    private LocalPathFragment pathFragment = null;
    private ServiceConnection bucketSC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            myBucket = ICWSBucketAidlInterface.Stub.asInterface(service);
            if(null != mFileListMag){
                mFileListMag.cloudTool.setCloudService(myBucket);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_browser);
        init();
        if(null==savedInstanceState){
            setDefaultFragment();
        }
        else{
            if(null!=mLoadTask){
                mLoadTask.setActivity(this);
            }
        }
        cloudServiceBind();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoadTask.setActivity(null);
        cloudServiceUnbind();
    }

    private void setDefaultFragment()
    {
        if(null==mFileListMag) {
            String path;
            String state = Environment.getExternalStorageState();
            if(state.equals(Environment.MEDIA_MOUNTED)){
                path = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
            else{
                path=getFilesDir().getAbsolutePath();
            }
            Log.i(TAG,"file path: "+ path);
            mFileListMag = new FileListManager(path);
            mFileListMag.setCurrentType(FileListManager.TYPE_LOCAL);
        }
        mContentFrag = new ContentFragment();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mContentFrag.isLandscape= (metric.widthPixels > metric.heightPixels);
        //mContentFrag.isLandscape = false;
        Log.i(TAG, "screen width: " + metric.widthPixels);
        if(FileListManager.TYPE_LOCAL ==mFileListMag.getCurrentType()) {
            mAdapter = new FileListAdapter(mFileListMag.getLocalList().getFileListData()
                    , this);
            //mContentFrag.setDownloadButtonResource(R.drawable.up_arrow_32x32);
            localCloudSelect(true);

        }else{
            mAdapter = new FileListAdapter(mFileListMag.getCloudList().getFileListData()
                    , this);
            //mContentFrag.setDownloadButtonResource(R.drawable.down_arrow_32x32);
            localCloudSelect(false);
        }

        mContentFrag.setListAdapter(mAdapter);
        mContentFrag.setMyListItemClickListener(this);
        mContentFrag.setButtonOnClickListener(this);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.id_content, mContentFrag, mContentFrag.TAG);
        transaction.commit();

        if(null==mLoadTask){
            mLoadTask = new LoadFileAsyncTask(this,mFileListMag);
            mLoadTask.setActivity(this);
            mProcState = LoadFileAsyncTask.PROC_SYNC;
            mLoadTask.setViewTag("Sync All");
            mLoadTask.execute("init");
        }

    }

    private void init(){
        mLocalButton= (Button)this.findViewById(R.id.local_type);
        mLocalButton.setOnClickListener(this);
        mCloudButton= (Button) this.findViewById(R.id.cloud_type);
        mCloudButton.setOnClickListener(this);
        mExportTitleText = (TextView)this.findViewById(R.id.export_title);
        drawableSelect = getResources().getDrawable(R.drawable.arrow_up);
        drawableSelect.setBounds(0, 0, drawableSelect.getIntrinsicWidth(),
                drawableSelect.getIntrinsicHeight());

        mLocalDirList = new LocalDirList();
        mPathAdapter = new DirListAdapter(mLocalDirList.getFileListData(),this);
        pathFragment = new LocalPathFragment();
        pathFragment.setListAdapter(mPathAdapter);
        pathFragment.setButtonOnClickListener(this);
        pathFragment.setListItemClickListener(this);

    }

    private void localCloudSelect(boolean isLocal){
        if(isLocal){
            mLocalButton.setCompoundDrawables(null, null, null, drawableSelect);
            mCloudButton.setCompoundDrawables(null, null, null, null);
            mContentFrag.setUpDownLoadType(1);
        }else {
            mCloudButton.setCompoundDrawables(null,null,null,drawableSelect);
            mLocalButton.setCompoundDrawables(null,null,null,null);
            mContentFrag.setUpDownLoadType(0);
        }

    }
    private void cloudServiceBind(){
        Intent mIntent = new Intent();
        mIntent.setAction(ICWSBucketAidlInterface.class.getName());
        Intent serverIntent = getExplicitIntent(getApplicationContext(), mIntent);
        if(null != serverIntent) {
            Intent intent = new Intent(serverIntent);
            intent.setPackage(getPackageName());
            Log.d(TAG, "bindService");
            bindService(intent, bucketSC, Context.BIND_AUTO_CREATE);
        }else{
            Log.e(TAG, "[bind] Not find cloud service!!");
        }
    }

    private void cloudServiceUnbind(){
        Intent mIntent = new Intent();
        mIntent.setAction(ICWSBucketAidlInterface.class.getName());
        Intent serverIntent = getExplicitIntent(getApplicationContext(), mIntent);
        if(null != serverIntent) {
            Intent intent = new Intent(serverIntent);
            intent.setPackage(getPackageName());
            Log.d(TAG, "unbindService");
            unbindService(bucketSC);
        }else{
            Log.e(TAG, "[unbind] Not find cloud service!!");
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.local_type:
                if(FileListManager.TYPE_CLOUD ==mFileListMag.getCurrentType()) {

//                    mContentFrag.setDownloadButtonResource(R.drawable.up_arrow_32x32);
                    localCloudSelect(true);
                    mFileListMag.setCurrentType(FileListManager.TYPE_LOCAL);
                    mAdapter.setData(mFileListMag.getLocalList().getFileListData());
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.cloud_type:
                if(FileListManager.TYPE_LOCAL ==mFileListMag.getCurrentType()) {
//                    mContentFrag.setDownloadButtonResource(R.drawable.down_arrow_32x32);
                    localCloudSelect(false);
                    mFileListMag.setCurrentType(FileListManager.TYPE_CLOUD);
                    if (null == mFileListMag.getCloudList().getFileListData()) {
                        Log.e(TAG, "getCloudList error !");
                    }
                    mAdapter.setData(mFileListMag.getCloudList().getFileListData());
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.updownloadButton:
                Log.i(TAG,"updownloadButton;mProcState= "+mProcState);
                if(0==mProcState){
                    LoadFileAsyncTask loadTask = new LoadFileAsyncTask(this,mFileListMag);
                    if(FileListManager.TYPE_LOCAL ==mFileListMag.getCurrentType()){
                        mProcState |= LoadFileAsyncTask.PROC_UPWNLOAD;
                        loadTask.setViewTag("Upload");
                        loadTask.execute("upload");

                    }else {
                        mProcState |= LoadFileAsyncTask.PROC_DOWNLOAD;
                        loadTask.setViewTag("Download");
                        loadTask.execute("download");
                    }
                }
                break;
            case R.id.reflashButton:
                Log.i(TAG,"reflashButton;mProcState= "+mProcState);
                if(0==mProcState){
                    mProcState |= LoadFileAsyncTask.PROC_SYNC;
                    LoadFileAsyncTask loadTask = new LoadFileAsyncTask(this,mFileListMag);
                    if(FileListManager.TYPE_LOCAL ==mFileListMag.getCurrentType()) {
                        loadTask.setViewTag("Syn Local");
                        loadTask.execute("sync-local");
                    }
                    else{
                        loadTask.setViewTag("Sync Cloud");
                        loadTask.execute("sync-cloud");
                    }
                }
                break;
            case R.id.deleteButton:
                Log.i(TAG,"deleteButton:mProcState= "+mProcState);
                if(0==mProcState){
                    mProcState |= LoadFileAsyncTask.PROC_DELETE;
                    LoadFileAsyncTask loadTask = new LoadFileAsyncTask(this,mFileListMag);
                    if(FileListManager.TYPE_LOCAL ==mFileListMag.getCurrentType())
                        loadTask.execute("del-local");
                    else loadTask.execute("del-cloud");
                }
                break;

            case R.id.saveButton:
                if(mFileListMag.getCloudList().getFileCount() > 0 ) {
                    String state = Environment.getExternalStorageState();
                    if (state.equals(Environment.MEDIA_MOUNTED)) {
                        mFileListMag.cloudSelectAll();
                        mAdapter.notifyDataSetChanged();
                        listSavePathSwitch(false);
                    }
                }
            break;
            case R.id.pathOK:
                listSavePathSwitch(true);
                //mFileListMag.testUsbWrite(mLocalDirList.getPath());
                if(0==mProcState){
                    LoadFileAsyncTask loadTask = new LoadFileAsyncTask(this,mFileListMag);
                    mProcState |= LoadFileAsyncTask.PROC_DOWNLOAD;
                    loadTask.setViewTag("Download");
                    loadTask.execute("download",mLocalDirList.getPath());
                }
                break;
            case R.id.pathCancel:
                listSavePathSwitch(true);
                mFileListMag.cancelCloudSelect();
                mAdapter.notifyDataSetChanged();
                break;

        }
    }

    public void onTaskCompleted(int proc){
        mProcState &= (~proc);
        Log.i(TAG,"onTaskCompleted;mProcState "+mProcState);
        if(null != mAdapter){
            mAdapter.notifyDataSetChanged();
        }

    }

    public void onTaskRefresh(){
        if(null != mAdapter){
            if(mAdapter.getCount() > 0)
                mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
        super.onRestoreInstanceState(state);
        Log.i(TAG, "onRestoreInstanceState");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        mLoadTask.setActivity(null);
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Object tag = view.getTag();
        if(tag instanceof FileListHolder) {
            FileListHolder holder = (FileListHolder) tag;
            holder.cb.toggle();
            if (-1 != position) {
                mAdapter.setSelectedFlag(position, holder.cb.isChecked());
            }
        }
        else if(tag instanceof DirListHolder) {
            if(-1 != position) {
                String curDir = mPathAdapter.getAbsolutePath(position);
                pathFragment.setPathName(curDir);
                mLocalDirList.setPath(curDir);
                mPathAdapter.notifyDataSetInvalidated();
            }
        }
    }

    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    public void  listSavePathSwitch(boolean isShowList ){
        if(isShowList){
            if(null != mContentFrag) {
                mExportTitleText.setVisibility(View.GONE);
                mLocalButton.setVisibility(View.VISIBLE);
                mCloudButton.setVisibility(View.VISIBLE);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction tx = fm.beginTransaction();
                tx.hide(pathFragment);
                pathFragment.isShow = false;
                tx.show(mContentFrag);
                tx.commit();
            }

        }else{
            if(null != pathFragment) {
                mExportTitleText.setVisibility(View.VISIBLE);
                mLocalButton.setVisibility(View.GONE);
                mCloudButton.setVisibility(View.GONE);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction tx = fm.beginTransaction();
                Fragment pathFrag = fm.findFragmentByTag("PATH");
                tx.hide(mContentFrag);
                if (null == pathFrag){
                    String pathExt = mLocalDirList.getExtRootDirName();
                    mLocalDirList.setPath(pathExt);
                    pathFragment.setPathName(pathExt);
                    tx.add(R.id.id_content, pathFragment, "PATH");
                    tx.addToBackStack(null);
                }
                else{
                    tx.show(pathFragment);
                }
                tx.commit();
                pathFragment.isShow = true;
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_ESCAPE){
            if(pathFragment.isShow){
                String externalDir = mLocalDirList.getRootDir();
                String curPath=mLocalDirList.getPath();
                //if(!(curPath.equals("/") || curPath.equals(externalDir))){
                if(! curPath.equals("/")){
                    String paths[] = curPath.split(File.separator);
                    if(paths.length > 1){
                        String newPath;
                        if(2 == paths.length){
                            newPath = "/";
                        }else {
                            StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < (paths.length - 1); i++) {
                                sb.append(paths[i]);
                                if (i != (paths.length - 2)) {
                                    sb.append(File.separator);
                                }

                            }
                            newPath = sb.toString();
                        }
                        mLocalDirList.setPath(newPath);
                        pathFragment.setPathName(newPath);
                        mPathAdapter.notifyDataSetInvalidated();
                        return true;
                    }
                }
                else{
                    mExportTitleText.setVisibility(View.GONE);
                    mLocalButton.setVisibility(View.VISIBLE);
                    mCloudButton.setVisibility(View.VISIBLE);
                }

            }

        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
            if((null != pathFragment) &&(! pathFragment.isPassUpKey())){
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

}
