package com.bcepnez.gvreader;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    String TAG = "Main Activity";
    Intent CamIntent,GalIntent,CropIntent;
    Toolbar toolbar;
    DisplayMetrics displayMetrics;
    int width,height;
    File file;
    Uri uri;
    ImageView imageView;
    Bitmap bitmap;
    final int RequestRuntimePermissionCode = 1;
    boolean crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        crop = false;
        imageView = (ImageView)findViewById(R.id.imgView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Crop Image");
        setSupportActionBar(toolbar);
        int camPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (camPermission == PackageManager.PERMISSION_DENIED){
            RequestRuntimePermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_camera)
            openCamera();
        else if (item.getItemId() == R.id.btn_gallery)
            openGallery();
        return true;
    }

    private void openGallery() {
//        crop = false;
        GalIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//        startActivityForResult(Intent.createChooser(GalIntent,"Select Image from gallery"),2);
        if (!crop)startActivityForResult(GalIntent,RESULT_LOAD_IMAGE);
    }

    private void openCamera() {
        CamIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                "File"+String.valueOf(System.currentTimeMillis())+".bmp" );
        uri = Uri.fromFile(file);
        CamIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        CamIntent.putExtra("return-data",true);
        startActivityForResult(CamIntent,0);
    }

    private void RequestRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
            Toast.makeText(this,"Camera permission allow to use camera",Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestRuntimePermissionCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && requestCode == RESULT_OK){
            CropImage();
        }
        else if (requestCode == RESULT_LOAD_IMAGE && !crop) {
            if(data!= null && data.getData()!=null){
                uri = data.getData();
                crop = false;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    if (!crop)CropImage();
                    OCR();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == 1){
            if (data!=null && data.getData()!=null){
                Bundle bundle = data.getExtras();
                bitmap = bundle.getParcelable("data");
                crop = true;
//                imageView.setImageBitmap(bitmap);
                OCR();
            }
        }
    }

    private void OCR() {

//            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//            imageView = (ImageView) findViewById(R.id.imgView);
//            imageView.setImageBitmap(bitmap);

            // imageBitmap is the Bitmap image you're trying to process for text
            if (bitmap != null&&crop) {
                Toast.makeText(this, "load bitmap", Toast.LENGTH_SHORT).show();
                imageView.setImageBitmap(bitmap);
                TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();

                if (!textRecognizer.isOperational()) {
                    // Note: The first time that an app using a Vision API is installed on a
                    // device, GMS will download a native libraries to the device in order to do detection.
                    // Usually this completes before the app is run for the first time.  But if that
                    // download has not yet completed, then the above call will not detect any text,
                    // barcodes, or faces.
                    // isOperational() can be used to check if the required native libraries are currently
                    // available.  The detectors will automatically become operational once the library
                    // downloads complete on device.
                    Log.w(TAG, "Detector dependencies are not yet available.");

                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                    if (hasLowStorage) {
                        Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Low Storage");
                    }
                }

                TextView textView = (TextView) findViewById(R.id.text1);
//
//
//
//
//                Frame!!!!!!!!!!
//                Bitmappppppppppp
//
//
//
                Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                StringBuilder strbd = new StringBuilder();
                int i;
                for (i = 0; i < textBlocks.size(); i++) {
//                    Toast.makeText(this, "Hi Yukio", Toast.LENGTH_SHORT).show();
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    String text = textBlock.getValue();
                    strbd.append(i);
                    strbd.append(" : ");
                    strbd.append(text);
                    strbd.append("\n");

                }
                if (i == textBlocks.size()){
                    Toast.makeText(this, "Completed!", Toast.LENGTH_SHORT).show();
                    if (strbd.length() != 0) textView.setText(strbd.toString());
                    else textView.setText("No data");
                }

                crop = false;
            }

    }

    private void CropImage() {
        try {
            crop=true;
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setDataAndType(uri,"image/*");
            CropIntent.putExtra("crop","true");
//            CropIntent.putExtra("OutputX",180);
//            CropIntent.putExtra("OutputY",180);
            CropIntent.putExtra("aspectX",7);
            CropIntent.putExtra("aspectY",1);
            CropIntent.putExtra("scaleUpIfNeeded",true);
            CropIntent.putExtra("scaleDownIfNeeded",true);
            CropIntent.putExtra("data",true);

            startActivityForResult(CropIntent,1);

        }
        catch (ActivityNotFoundException ex){
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestRuntimePermissionCode : {
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"Permission Canceled",Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }
}