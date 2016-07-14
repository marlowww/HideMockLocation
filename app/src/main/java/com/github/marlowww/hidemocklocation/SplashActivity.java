package com.github.marlowww.hidemocklocation;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public Runnable runnable = new Runnable() {
        public void run() {
            long startTime = System.nanoTime();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            // Get information about apps
            PackageManager pm = getPackageManager();
            final List<ResolveInfo> appsInfo = pm.queryIntentActivities(mainIntent, 0);
            Collections.sort(appsInfo, new ResolveInfo.DisplayNameComparator(pm));

            // Standardize splash screen time
            try {
                long waitTime = System.nanoTime() - startTime;
                long splashTime = 2000;
                if (waitTime < splashTime * 1000000)
                    Thread.sleep((long)(splashTime - (long)((double) waitTime)/1000000.0));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                i.putParcelableArrayListExtra(Common.INTENT_APPS_LIST, (ArrayList<ResolveInfo>) appsInfo);
                startActivity(i);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
