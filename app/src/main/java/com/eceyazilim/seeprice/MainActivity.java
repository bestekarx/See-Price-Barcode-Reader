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
import android.os.Handler;
import android.os.Looper;
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

import com.eceyazilim.seeprice.Models.Parameter;
import com.eceyazilim.seeprice.Models.Query;
import com.eceyazilim.seeprice.Models.QueryRequest;
import com.eceyazilim.seeprice.Models.QueryResult;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    private TextView txt_discount; //indirimli fiyat
    private TextView txt_price; //normal fiyat
    private TextView txt_productName; //ürün adı
    private TextView txt_stockCode; //stok kodu
    private TextView txt_moneyUnit; //stok kodu
    private SharedPreferences.Editor editor;
    private int testx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        SharedPreferences settings = activity.getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        editor = settings.edit();
        previewView = findViewById(R.id.previewView);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        analyzer = new MyImageAnalyzer();

        //elements
        txt_title = findViewById(R.id.txt_title);
        container_preview = findViewById(R.id.container_preview);
        container_return = findViewById(R.id.container_return);

        txt_price  = findViewById(R.id.txt_price);
        txt_stockCode = findViewById(R.id.txt_stockCode);
        txt_productName  = findViewById(R.id.txt_productName);
        txt_moneyUnit  = findViewById(R.id.txt_moneyUnit);

        //üstü çizili indirim textview
        txt_discount = findViewById(R.id.txt_discount);
        txt_discount.setPaintFlags(txt_discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // fiyat gör butonu
        Button btn_seePrice = findViewById(R.id.btn_seePrice);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "circular_bold.ttf");
        btn_seePrice.setTypeface(typeface);
        btn_seePrice.setOnClickListener(view ->
        {
            container_preview.setVisibility(View.VISIBLE);
            container_return.setVisibility(View.GONE);

            ResetQuery();
        });

        ImageView img_settings = findViewById(R.id.img_settings);
        img_settings.setOnLongClickListener(view -> {
            showPopupSYNC();
            return false;
        });

        //parameter
        RestInterface restInterface = ApiClient.getRetrofit().create(RestInterface.class);
        Call<Parameter> call = restInterface.getParameter();
        call.enqueue(new Callback<Parameter>()
        {
            @Override
            public void onResponse(Call<Parameter> call, Response<Parameter> response)
            {
                //OK değilse her zaman hata ver.
                if(response.code() == 200){
                    txt_title.setText(response.body().getResult().getFirmname());
                }
                else {
                    Application.dlgNoLicenceWarning(activity);
                }
            }

            @Override
            public void onFailure(Call<Parameter> call, Throwable t)
            {
                Application.dlgNoLicenceWarning(activity);
            }
        });

        ResetQuery();
    }

    private void ResetQuery()
    {
        txt_productName.setText("");
        txt_stockCode.setText("");
        txt_price.setText("");
        txt_discount.setText("");
        txt_moneyUnit.setText("");
        txt_discount.setVisibility(View.VISIBLE);

        container_preview.setVisibility(View.VISIBLE);
        container_return.setVisibility(View.GONE);

        StartCamera();
    }

    private void StartCamera()
    {
        cameraProviderFuture.addListener(() ->
        {
            try
            {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED))
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},101);
                else
                {
                    bindPreview((ProcessCameraProvider) cameraProviderFuture.get());
                }
            }
            catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void StopCamera(Barcode b)
    {
        cameraProviderFuture.addListener(() ->
        {
            try
            {
                ProcessCameraProvider v = (ProcessCameraProvider) cameraProviderFuture.get();
                v.unbindAll();
                testx +=1;

                QueryRequest(b.getDisplayValue());
            }
            catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void QueryRequest(String barcode)
    {
        QueryRequest request = new QueryRequest();
        request.setBarcode(barcode);
        //query
        RestInterface restInterface = ApiClient.getRetrofit().create(RestInterface.class);

        Call<Query> call = restInterface.query(request);
        call.enqueue(new Callback<Query>()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<Query> call, Response<Query> response)
            {
                //OK değilse her zaman hata ver.
                if(response.code() == 200)
                {
                    QueryResult result = response.body().getResult();
                    if(result != null)
                    {
                        //ürün adı
                        txt_productName.setText(result.getProductname());
                        //stok kodu
                        txt_stockCode.setText("SKU"+result.getStockcode());
                        //indirimli fiyat (varsa)
                        if(result.getPricelist() != null)
                            txt_discount.setText(result.getPricelist().toString());
                        else
                            txt_discount.setVisibility(View.GONE);

                        txt_price.setText(result.getPrice().toString());

                        container_preview.setVisibility(View.GONE);
                        container_return.setVisibility(View.VISIBLE);


                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> ResetQuery(), 30000);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Ürün bulunamadı. " + testx,Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Ürün bulunamadı." + testx,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Query> call, Throwable t)
            {
                Application.dlgNoLicenceWarning(activity);
            }
        });
    }



    private void showPopupSYNC()
    {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_settings);
        //dialog.getWindow().getAttributes().windowAnimations = R.style.AnimationPopup;

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
        public void analyze(@NonNull ImageProxy image)
        {
            scanbarcode(image);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void scanbarcode (ImageProxy image)
        {
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            assert image1 != null;
            InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
            //BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().build();
            BarcodeScanner scanner = BarcodeScanning.getClient();

            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes ->
                    {
                        //listenin sonunda ki barkodu al.
                        if(barcodes.size() > 0)
                        {
                            Barcode barcode = barcodes.get(barcodes.size() - 1);
                            StopCamera(barcode);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Okuma sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyiniz.",Toast.LENGTH_LONG).show();
                    }).addOnCompleteListener(task ->{
                image.close();
            });
        }
    }
}

