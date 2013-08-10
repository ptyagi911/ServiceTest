package com.example.servicetest;

/**
 *  @file DMOAnalyticsService.h
 *  @brief DMOAnalyticsService sits in the background and monitors the the application package
 *  for analytics events.
 */

/**
 * @class DMOAnalyticsConnection
 * DMOAnalyticsConnection sits in the background and monitors the the application package
 *  for analytics events. if the process priority is not 100 it is concidered a background
 *  process.
 */

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//package-private
public class TestService extends Service {

    private static final String TAG = "TestService";
    private boolean isForeground;
    private boolean isDisplayOn;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isForeground = true;
        isDisplayOn = true;
        scheduler();
    }

    /**
     * schedules foreground vs background check
     *
     * @return void
     * @method scheduler
     */
    private void scheduler() {
        Timer foregroundCheck = new Timer(true);
        foregroundCheck.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    activityUpdate();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to update activity: " +
                            e.getMessage());
                }
            }
        }, 0, 3 * 1000);
    }

    /**
     * fires the app end analytics event.
     *
     * @return void
     * @method onDistroy
     */
    @Override
    public void onDestroy() {
        System.out.println("Hello");
        //super.onDestroy();
        endEvent();
    }

    /**
     * fires the start end analytics event.
     *
     * @return void
     * @method onStart
     */
    @Override
    public void onStart(Intent intent, int startid) {
        startEvent();
    }

    /**
     * is called  by the timer. When the Activity has changed and if the process importance
     * has changed it fires the appropriate app_foreground or app_background analytics event.
     *
     * @return void
     * @method activityUpdate
     */
    public TimerTask activityUpdate() {
        boolean state = this.isActivePackage();
        boolean isOn = this.isScreenDisplaying();

        if (state == isForeground && isOn == isDisplayOn) return null;
        if (state == true && isOn == true) {
            foregroundEvent();
        } else if (state == false || isOn == false) {
            backgroundEvent();
        }

        this.isForeground = state;
        this.isDisplayOn = isOn;
        return null;
    }

    /**
     * If the application that spawned this process is the active process return true.
     * It verifys the package name "com.something.app" and the process importance to determine its
     * own active state.
     *
     * @return boolean
     * @method isActivePackage
     */
    private boolean isActivePackage() {
        boolean activeB = false;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list2 = am.getRunningAppProcesses();
        for (RunningAppProcessInfo ti : list2) {
            boolean isPrimaryTask = (ti.importance == 100);
            boolean isSamePackage = String.valueOf(this.getPackageName())
                    .equals(String.valueOf(ti.processName));

            if (isSamePackage && isPrimaryTask) activeB = true;
        }
        return activeB;
    }

    PowerManager pm = null;

    private boolean isScreenDisplaying() {
        if (pm == null) {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }
        return pm.isScreenOn();
    }


    private void startEvent() {
        Log.d(TAG, "app_start");
    }

    private void foregroundEvent() {
        Log.d(TAG, "app_foreground");
    }

    private void backgroundEvent() {
        Log.d(TAG, "app_background");
    }

    private void endEvent() {
        Log.d(TAG, "app_end");
    }
}