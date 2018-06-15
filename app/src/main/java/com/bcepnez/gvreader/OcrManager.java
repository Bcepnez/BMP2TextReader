package com.bcepnez.gvreader;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by BenzRST on 15-Jun-18.
 */

public class OcrManager {
    TessBaseAPI baseAPI = null;
    public void initAPI(){
        baseAPI = new TessBaseAPI();

        String dataPath = MainApplication.instance.getTessDataParentDirectory();
        baseAPI.init(dataPath,"eng");
//        first param = datapath to trained data,
//        second = language code

    }
    public String startRecognizer(Bitmap bitmap){
        if(baseAPI == null){
            initAPI();
        }
        baseAPI.setImage(bitmap);
        return baseAPI.getUTF8Text();
    }
}
