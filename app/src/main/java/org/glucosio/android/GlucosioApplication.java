package org.glucosio.android;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GlucosioApplication extends Application {

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_TRACKER);
            mTracker.enableAdvertisingIdCollection(true);

            if (BuildConfig.DEBUG) {
                GoogleAnalytics.getInstance(this).setAppOptOut(true);
                Log.i("Glucosio", "DEBUG BUILD: ANALYTICS IS DISABLED");
            }
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/lato.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        if (BuildConfig.DEBUG) {
            new Instabug.Builder(this, "b2226aa30fec24f6f4bed6ad68964e9b")
                    .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                    .build();
        } else {
            new Instabug.Builder(this, "820ee7db3118d03fd5f4249b5a73672e")
                    .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                    .build();
        }
    }
}