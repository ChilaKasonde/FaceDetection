package com.devguilds.facedetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;


public class MainActivity extends ActionBarActivity {


    ImageView myFace,imagePreview;
    TextView faceText;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    int requestCode = 101;
    FirebaseVisionFaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myFace = (ImageView)findViewById(R.id.myFace);
        faceText = (TextView)findViewById(R.id.typeFace);
        imagePreview = (ImageView)findViewById(R.id.imagePreview);


        imagePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();




        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);



        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, requestCode);
            dispatchTakePictureIntent();
        } else {
            dispatchTakePictureIntent();
        }


    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imagePreview.setImageBitmap(imageBitmap);
            imageRecognition(imageBitmap);
        }
    }


    public void imageRecognition(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);


        Task<List<FirebaseVisionFace>> task = faceDetector.detectInImage(image);

        task.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                getInfoFromFaces(firebaseVisionFaces);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }



    private String getInfoFromFaces(List<FirebaseVisionFace> faces) {
        StringBuilder result = new StringBuilder();
        float smileProb = 0;
        float leftEyeOpenProb = 0;
        float rightEyeOpenProb = 0;
        for (FirebaseVisionFace face : faces) {

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and nose available):

            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                smileProb = face.getSmilingProbability();
            }
            if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                leftEyeOpenProb = face.getLeftEyeOpenProbability();
            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                rightEyeOpenProb = face.getRightEyeOpenProbability();
            }

            result.append("Smile: ");
            if (smileProb > 0.5) {
                myFace.setImageResource(R.drawable.smiling_face);
                result.append("Yes");
                break;
            } else {
                myFace.setImageResource(R.drawable.blank_face);
                result.append("No");

            }
            result.append("\nLeft eye: ");
            if (leftEyeOpenProb > 0.5) {
                myFace.setImageResource(R.drawable.right_eyes_closed);

               result.append("Open");
                break;
            } else {
                myFace.setImageResource(R.drawable.blank_face);
                result.append("Close");
            }
            result.append("\nRight eye: ");
            if (rightEyeOpenProb > 0.5) {

                myFace.setImageResource(R.drawable.left_eyes_closed);
                result.append("Open");
                break;  
            } else {
                myFace.setImageResource(R.drawable.blank_face);
                result.append("Close");
            }
            result.append("\n\n");
        }

        faceText.setText(result.toString());
        return result.toString();
    }


}
