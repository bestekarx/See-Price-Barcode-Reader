package com.eceyazilim.seeprice;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Application
{
    public static volatile String APP_SYNC_URL = "";


    public static void dlgNoLicenceWarning(final Activity act)
    {
        final Dialog dialog = new Dialog(act);
        dialog.setContentView(R.layout.main_error);
        final Button btnDialog = dialog.findViewById(R.id.btnDialog);
        final TextView txtMessage = dialog.findViewById(R.id.txtMessage);
        final TextView txtParameterMessage = dialog.findViewById(R.id.txtParameterMessage);

        //@SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(act.getContentResolver(), Settings.Secure.ANDROID_ID);


        txtParameterMessage.setText("Uygulama çalıştırılırken bir hata oluştu.\nLütfen daha sonra tekrar deneyiniz.");

        btnDialog.setTypeface(Typeface.createFromAsset(act.getAssets(), "circular_bold.ttf"));
        dialog.setCanceledOnTouchOutside(false);
        btnDialog.setText("ANASAYFAYA DÖN");
        btnDialog.setOnClickListener(v -> {
            //pencereyi kapat ve ana sayfaya dön
            dialog.dismiss();
            gotoMainPage(act);
        });

        try
        {
            PackageInfo pInfo = act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
            String version = pInfo.versionName;
            txtMessage.setText("www.eceyazilim.com V" + version);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        Display display = act.getWindowManager().getDefaultDisplay();
        int widths = display.getWidth();

        dialog.getWindow().setLayout(widths, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }


    public static void gotoMainPage(Activity act)
    {
        Intent i = new Intent(act, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        act.startActivity(i);
        act.finish();
    }



}
