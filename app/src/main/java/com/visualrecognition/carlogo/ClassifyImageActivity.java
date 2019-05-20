package com.visualrecognition.carlogo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImage;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifierResult;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;
import com.jpegkit.Jpeg;
import com.jpegkit.JpegImageView;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ClassifyImageActivity extends AppCompatActivity {


    private static final String TAG = "ClassifyImageActivity";
    private ImageView mImageView;
    private TextView mResult;
    private ProgressBar mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify_image);

        mImageView = findViewById(R.id.imageView);
        mResult = findViewById(R.id.result);
        mProgress = findViewById(R.id.progress);

        String path = getIntent().getStringExtra("imagePath");

        Log.i(TAG, "onCreate: "+path);
        if(path != null){
            setBitmapToImageView(path);
        }


    }

    private void setBitmapToImageView(String path) {
        try {
            mImageView.setImageBitmap(getThumbnail((path)));

            classifyImage(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void classifyImage(String path) {
        IamOptions options = new IamOptions.Builder()
                .apiKey("ixB-GUO4ip4YoOIU1oYFhv5iVCc8R2XCeI93z74gS6a9")
                .build();


        VisualRecognition visualRecognition = new VisualRecognition("2018-03-19",options);

        try {
            InputStream inputStream = new FileInputStream(path);
            ClassifyOptions mClassifyOptions = new ClassifyOptions.Builder()
                    .imagesFile(inputStream)
                    .imagesFilename("image.jpg")
                    .threshold((float)0.5)
                    .classifierIds(Arrays.asList("DefaultCustomModel_1529974961"))
                    .build();

           /* new Thread(new Runnable() {
                @Override
                public void run() {
                    ClassifiedImages result = visualRecognition.classify(mClassifyOptions).execute();
                    Log.i(TAG, "run: "+result);
                    parseResult(result);
                }
            }).start();*/

           new ClassifyAsyncTask(visualRecognition,mClassifyOptions).execute();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static Bitmap getThumbnail(String uri) throws  IOException {
        InputStream input = new FileInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        int THUMBNAIL_SIZE=750;
        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = new FileInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    class ClassifyAsyncTask extends AsyncTask<String,Void,ClassifiedImages> {

        VisualRecognition recognition;
        ClassifyOptions classifyOptions;

        public ClassifyAsyncTask(VisualRecognition recognition, ClassifyOptions classifyOptions) {
            this.recognition = recognition;
            this.classifyOptions = classifyOptions;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected ClassifiedImages doInBackground(String... strings) {
            ClassifiedImages result = recognition.classify(classifyOptions).execute();
            return result;
        }

        @Override
        protected void onPostExecute(ClassifiedImages classifiedImages) {
            mProgress.setVisibility(View.GONE);
            List<ClassifiedImage> classifiedImagesList = classifiedImages.getImages();
            for(int i=0;i<classifiedImagesList.size();i++){
                ClassifiedImage classiedImage = classifiedImagesList.get(i);

                for(ClassifierResult classifierResult : classiedImage.getClassifiers()){

                    for(ClassResult classResult : classifierResult.getClasses()){
                        String className = classResult.getClassName();
                        mResult.append(className.substring(0,className.length()-4) +" \t "+classResult.getScore() +"\n");
                    }
                }
            }
        }
    }
}
