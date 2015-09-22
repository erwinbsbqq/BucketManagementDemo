package com.gowarrior.bucketmanagementdemo.data;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by gavin.liu on 2015/9/16.
 */
public class LocalDirList extends LocalFileList {
    public static String TAG = "LocalDirList";
    private String rootDir = "/storage";

    private String path = "/storage";
    public LocalDirList(){
        super();
    }

    public void setPath(String path){

        if(this.path.equalsIgnoreCase(path)){
            return;
        }
        this.path = path;
        loadDirs();
    }

    public String getPath(){
        return this.path;
    }

    public String getRootDir(){
        return this.rootDir;
    }
    public int loadDirs(){
        int ret =0;
        String name ;
        String absPath ;
        super.clearFileNodes();
        File dir = new File(this.path);

        if (!dir.isDirectory()) {
            return ret;
        }
        File[] fileList = dir.listFiles();
        if(fileList == null) {
            return ret;
        }
        for (int i = 0; i < fileList.length; i++) {
            File file = fileList[i];
            if (!file.exists()) {
                break;
            }
            if(file.isDirectory()){
                name = file.getName();
                if(name.startsWith(".")){
                    continue;
                }
                absPath = file.getAbsolutePath();
                super.addFileNode(absPath,name,FileNode.FILE_TYPE_DIR);
                ret++;
            }
        }
        return ret;
    }

    public String getExtRootDirName() {
        String name = "/storage";
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/mounts"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("secure") || line.contains("asec")) {
                    continue;
                }
                if (line.contains("vfat") || line.contains("ntfs")
                        || line.contains("fuseblk")) {
                    Log.v(TAG, line);
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (columns[1].contains("sd")
                                || columns[1].contains("usb")) {
                            Log.v(TAG, "Add: " + columns[1]);
                            File file = new File(columns[1]);
                            if (file.isDirectory()) {
                                name = file.getAbsolutePath();
                                File extF = Environment.getExternalStorageDirectory();
                                if(name.equalsIgnoreCase(extF.getAbsolutePath())) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            Log.v(TAG, "Read /proc/mounts fail!");
        }
        rootDir = name;
        return name;
    }

}
