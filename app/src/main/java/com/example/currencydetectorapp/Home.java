package com.example.currencydetectorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;

// import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
// import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

//import java.util.Locale;


public class Home extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//    CameraBridgeViewBase cameraBridgeViewBase;
    JavaCamera2View javaCamera2View;
    private String TAG = "Home Activity";
    Mat Mat1,Mat_t;
//    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        javaCamera2View = (JavaCamera2View)findViewById(R.id.javaCamera2View);
        javaCamera2View.setVisibility(SurfaceView.VISIBLE);
        javaCamera2View.setCvCameraViewListener(this);
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:
                {
                    Log.d(TAG,"Open CV loaded Successfully");
                    javaCamera2View.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if( javaCamera2View != null){
            javaCamera2View.disableView();
        }
//        if(textToSpeech != null){
//            textToSpeech.stop();
//            textToSpeech.shutdown();}
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"Open CV library found and using it.");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
        else{
            Log.d(TAG,"Open CV library not found");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,Home.this,baseLoaderCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( javaCamera2View != null){
            javaCamera2View.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Mat1 = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        Mat1.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat1 = inputFrame.rgba();
        Mat_t = Mat1.t();
        Core.flip(Mat1.t(),Mat_t,1);
        Imgproc.resize(Mat_t,Mat_t,Mat1.size());
        return Mat_t;
    }
}
