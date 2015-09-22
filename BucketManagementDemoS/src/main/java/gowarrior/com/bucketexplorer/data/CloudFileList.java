package gowarrior.com.bucketexplorer.data;

import java.util.ArrayList;

/**
 * Created by gavin.liu on 2015/9/1.
 */
public class CloudFileList extends  LocalFileList{
    private ArrayList<ProcNode> upProcList =null;
    private ArrayList<ProcNode> downProcList =null;
    public CloudFileList()
    {
        super();
        if(null==upProcList){
            upProcList = new ArrayList<ProcNode>();
        }
        if(null==downProcList){
            downProcList = new ArrayList<ProcNode>();
        }
    }

    public ArrayList<ProcNode> getUpProcList(){
        return upProcList;
    }
    public ArrayList<ProcNode> getDownProcList(){
        return downProcList;
    }


}
