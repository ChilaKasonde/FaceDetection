package com.devguilds.facedetection;

import android.content.Context;
import android.os.Looper;
import android.widget.ImageView;

/**
 * Created by C.Kasonde on 6/4/2019.
 */
public class FaceDetector {
        Context ctx;
        ImageView img;
        Looper looper;


    public FaceDetector(Context ctx, ImageView img, Looper looper) {
        this.ctx = ctx;
        this.img = img;
        this.looper = looper;
    }
}
