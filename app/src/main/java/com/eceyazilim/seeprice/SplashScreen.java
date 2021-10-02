package com.eceyazilim.seeprice;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashScreen  extends Activity
{
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.splash);

        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp < 360){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }else{
            if(configuration.screenWidthDp < 360){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        int SPLASH_DISPLAY_LENGTH = 3000;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }


}
