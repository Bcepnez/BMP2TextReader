package com.bcepnez.gvreader;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by BenzRST on 15-Jun-18.
 */

public class MainApplication extends Application {

    public static MainApplication instance = null;
    String type = "eng.traineddata";
    @Override
    public void onCreate() {
        super.onCreate();
        //start copy file here
        instance = this;
        copyTessDataForTextRecognizer();
    }
    private String tessDataPath(){
        return MainApplication.instance.getExternalFilesDir(null)+ "/tessdata";
    }
    public String getTessDataParentDirectory(){
        return MainApplication.instance.getExternalFilesDir(null).getAbsolutePath();
    }


    private void copyTessDataForTextRecognizer(){
        Runnable run = new Runnable() {
            @Override
            public void run() {
                AssetManager assetManager = MainApplication.instance.getAssets();
                OutputStream outputStream = null;
                try {
                    Log.d("MainApplication", "CopyTessDataForTextRecognizer");
                    InputStream in = assetManager.open(type);
                    String tesspath = instance.tessDataPath();
                    File tessFolder = new File(tesspath);
                    if (!tessFolder.exists()) {
                        tessFolder.mkdir();
                    }
                    String tessData = tesspath + "/" + type;
                    File file = new File(tessData);
                    if (!file.exists()) {
                        outputStream = new FileOutputStream(tessData);
                        byte[] buffer = new byte[1024];
                        int read = in.read(buffer);
                        while (read != -1) {
                            outputStream.write(buffer, 0, read);
                            read = in.read(buffer);
                        }
                        Log.d("MainApplication", "***Did finish copy tess file***");
                    } else
                        Log.d("MainApplication", "***tess file exist***");
                } catch (Exception e){
                    Log.d("MainApplication", "***couldn't finish copy \n Error : "+e.toString());
                }finally {
                    try {
                        if (outputStream!=null){
                            outputStream.close();
                        }
                    }catch (Exception exx){

                    }
                }
            }
        };
        new Thread(run).start();
    }
}
