package com.example.currencydetectorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_0;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_180;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_90;


public class CameraActivity extends AppCompatActivity {
    Camera mCamera;
    Button capture;
    FirebaseVisionImageLabeler imageLabeler;
    String text;
    TextToSpeech tts;
    public int previewState = 1;
    FirebaseVisionImage image;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        capture = findViewById(R.id.pic_taken);
        mCamera = getCameraInstance();
        textView = findViewById(R.id.textView);
        Toast.makeText(getApplicationContext(),"Click to capture !",Toast.LENGTH_LONG).show();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        final CameraPreview mPreview = new CameraPreview(this,mCamera);

        FrameLayout preview = findViewById(R.id.camera_p);
        preview.addView(mPreview);
        CameraPreview.setCameraOrientation(this,mCamera);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(previewState) {
                    case 0:
                        mCamera.startPreview();
                        previewState = 1;
                        break;

                    default:
                        mCamera.takePicture( null, mPicture, null);
                        previewState = 0;
                }
            }
        });

        final FirebaseAutoMLLocalModel mlLocalModel = new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("cecp/manifest.json").build();
        final FirebaseAutoMLRemoteModel mlRemoteModel = new FirebaseAutoMLRemoteModel.Builder("cecp").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        FirebaseModelManager.getInstance().download(mlRemoteModel, conditions).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Download Remote Model","Remote Model Successfully Downloaded");
                    }
                });
        FirebaseModelManager.getInstance().isModelDownloaded(mlRemoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
                        if (isDownloaded) {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(mlRemoteModel);
                        } else {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(mlLocalModel);
                        }
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                .setConfidenceThreshold(0.0f)
                                .build();
                        try {
                            imageLabeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                        } catch (FirebaseMLException e) {
                            Log.d("Error Firebase Model","Error building model from firebase;" + e.getMessage());
                        }
                    }
                });

    }

    private static Camera getCameraInstance(){
        Camera med = null;
        try{
            med = Camera.open();
        }catch (Exception e){
            Log.d("Camera","Camera not available:" + e.getMessage());
        }
        return med;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try{
                Uri uri = Uri.parse("android.resource://com.example.currencydetectorapp/drawable/note");
                image = FirebaseVisionImage.fromFilePath(CameraActivity.this,uri);
                imageLabeler.processImage(image).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<FirebaseVisionImageLabel>> task) {
                        for (FirebaseVisionImageLabel label : task.getResult()) {
                            String eachlabel = label.getText().toUpperCase();
                            tts.speak(eachlabel,TextToSpeech.QUEUE_FLUSH,null);
                            textView.append(eachlabel + "\n\n");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("On Fail",""+e);
                        Toast.makeText(getApplicationContext(),"Could not identify",Toast.LENGTH_LONG);
                    }
                });
            }catch(IOException e){
             e.getStackTrace();
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.startPreview();
    }
}
