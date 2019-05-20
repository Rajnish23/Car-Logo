package com.visualrecognition.carlogo;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jpegkit.Jpeg;
import com.jpegkit.JpegImageView;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Permission;
import java.security.Permissions;

public class MainActivity extends AppCompatActivity {


    private CameraView mCamera;
    private boolean isFlash = false;
    private AppCompatButton mFlashOnOffBtn;
    private JpegImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = findViewById(R.id.camera);
        mFlashOnOffBtn = findViewById(R.id.flashOnOffButton);


        mFlashOnOffBtn.setOnClickListener(mFlashClickListener);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        101);
            }
        }
    }

    private View.OnClickListener mFlashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mCamera.getFlash() != CameraKit.Constants.FLASH_ON) {
                mCamera.setFlash(CameraKit.Constants.FLASH_ON);
                mFlashOnOffBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_flash_on, 0, 0, 0);
                mFlashOnOffBtn.setText("Flash On");
            } else {
                mCamera.setFlash(CameraKit.Constants.FLASH_OFF);
                mFlashOnOffBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_flash_off, 0, 0, 0);
                mFlashOnOffBtn.setText("Flash Off");
            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
            else{
                Toast.makeText(this,"Permission Required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stop();
    }

    public void takePicture(View view) {

        mCamera.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(final CameraKitImage cameraKitImage) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String path = saveToInternalStorage(cameraKitImage.getBitmap());

                        Log.i("vvvv", "run: "+path);
                        Intent mClassifyIntent = new Intent(MainActivity.this, ClassifyImageActivity.class);
                        mClassifyIntent.putExtra("imagePath", path);
                        startActivity(mClassifyIntent);
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                }).start();


            }
        });
    }

    private String saveToInternalStorage(Bitmap bitmap) {

        ContextWrapper mWrapper = new ContextWrapper(getApplicationContext());

        File directory = mWrapper.getDir("imageClassDir", MODE_PRIVATE);

        File myPath = new File(directory, "image.jpg");

        FileOutputStream mOutputStream = null;

        try {
            mOutputStream = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,mOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return myPath.getAbsolutePath();
    }
}
