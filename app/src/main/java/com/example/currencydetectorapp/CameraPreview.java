package com.example.currencydetectorapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context,Camera camera){
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder){
        try{
            mCamera.setPreviewDisplay(holder);
        }catch(IOException e){
            Log.d("Surface Preview","Camera preview cannot be set:" + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder){
    }

    public static void setCameraOrientation(Activity activity,Camera camera){
    Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(1,info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void surfaceChanged(SurfaceHolder holder,int format,int w,int h){
        if(mHolder.getSurface() == null){
            return;
        }

        try {
            mCamera.stopPreview();
        }catch (Exception e){

        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }catch(Exception e){
            Log.d("surfaceChanged method","Camera preview unable to reset" + e.getMessage());
        }
    }


}

