package com.eceyazilim.seeprice;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity
{
    private ListenableFuture cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private MyImageAnalyzer analyzer;
    private Activity activity;

    // elements
    private TextView txt_title; //işletme adı
    private RelativeLayout container_preview; // kamera önizleme
    private PreviewView previewView;
    private RelativeLayout container_return; // tekrar oku
    private Button btn_seePrice; // fiyat gör butonu
    private ImageView img_settings; //ece logo settings
    private TextView txt_discount; //indirimli fiyat
    private TextView txt_price; //normal fiyat
    private TextView txt_productName; //ürün adı
    private TextView txt_stockCode; //stok kodu
    private TextView txt_moneyUnit; //para birimi
    private TextView txt_dotted; //detail layoutta ki dikey çizgi
    private LinearLayout container_title; //test için click


    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        settings = activity.getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        editor = settings.edit();

        previewView = findViewById(R.id.previewView);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        analyzer = new MyImageAnalyzer();
        cameraProviderFuture.addListener((Runnable) () ->
        {
            try
            {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED))
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},101);
                }
                else
                {
                    ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                    bindPreview(processCameraProvider);
                }
            }
            catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        //elements
        txt_title = findViewById(R.id.txt_title);
        container_preview = findViewById(R.id.container_preview);
        container_return = findViewById(R.id.container_return);

        txt_dotted  = findViewById(R.id.txt_dotted);
        txt_price  = findViewById(R.id.txt_price);
        txt_moneyUnit  = findViewById(R.id.txt_moneyUnit);
        txt_stockCode = findViewById(R.id.txt_stockCode);
        txt_productName  = findViewById(R.id.txt_productName);
        img_settings = findViewById(R.id.img_settings);

        //üstü çizili indirim textview
        txt_discount = findViewById(R.id.txt_discount);
        txt_discount.setPaintFlags(txt_discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        btn_seePrice = findViewById(R.id.btn_seePrice);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "circular_bold.ttf");
        btn_seePrice.setTypeface(typeface);

        container_title = findViewById(R.id.container_title);
        container_title.setOnClickListener(view ->
        {

            txt_discount.setText("30₺");
            txt_price.setText("25");
            txt_productName.setText("40 YAPRAK ÇİZGİLİ DEFTER - ECE YAYINLARI");
            txt_stockCode.setText("SKU: 4564123456798");

            container_preview.setVisibility(View.GONE);
            container_return.setVisibility(View.VISIBLE);
        });

        btn_seePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container_preview.setVisibility(View.VISIBLE);
                container_return.setVisibility(View.GONE);
            }
        });

        img_settings.setOnLongClickListener(view -> {
            showPopupSYNC();
            return false;
        });
    }

    private void showPopupSYNC()
    {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_settings);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AnimationPopup;

        ImageButton button_close = dialog.findViewById(R.id.button_close);
        button_close.setOnClickListener(view -> dialog.dismiss());
        Button btnSave = dialog.findViewById(R.id.btnSave);

        EditText et_syncUrl = dialog.findViewById(R.id.et_syncUrl);

        SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        Application.APP_SYNC_URL = settings.getString(Constants.SYNC_SETTINGS_URL,"");


        et_syncUrl.setText(Application.APP_SYNC_URL);


        btnSave.setOnClickListener(v ->
        {
            Application.APP_SYNC_URL = et_syncUrl.getText().toString();

            editor.putString(Constants.SYNC_SETTINGS_URL, Application.APP_SYNC_URL);
            editor.apply();
            editor.commit();

            Toast.makeText(getApplicationContext(), "Kayıt edildi.",Toast.LENGTH_LONG).show();
        });

        Display display = getWindowManager().getDefaultDisplay();
        int widths = display.getWidth();

        dialog.getWindow().setLayout(widths, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101 && grantResults.length > 0)
        {
            ProcessCameraProvider processCameraProvider = null;
            try {
                processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            bindPreview(processCameraProvider);
        }
    }

    private void bindPreview(ProcessCameraProvider processCameraProvider)
    {
        Preview preview = new Preview.Builder().build();
        //CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().
                setTargetResolution(new Size(640,480)).
                setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER).
                build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

        processCameraProvider.unbindAll();
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    public  class MyImageAnalyzer implements ImageAnalysis.Analyzer
    {
        @Override
        public void analyze(@NonNull ImageProxy image){
            scanbarcode(image);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void scanbarcode (ImageProxy image){
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            assert image1 != null;
            InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().build();
            BarcodeScanner scanner = BarcodeScanning.getClient();

            Task<List<Barcode>> result = scanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes)
                        {
                            // Task completed successfully
                            readerBarcodeData(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            // Task failed with an exception
                            Toast.makeText(getApplicationContext(), "Okuma sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.",Toast.LENGTH_LONG).show();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image.close();
                        }
                    });
        }

        private void readerBarcodeData(List<Barcode> barcodes) {
            for (Barcode barcode: barcodes)
            {
                Toast.makeText(getApplicationContext(), barcode.getDisplayValue(),Toast.LENGTH_LONG).show();
            }
        }
    }

}

