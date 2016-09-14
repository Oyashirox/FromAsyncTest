package fr.oyashirox.fromasynctest;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Florian on 14/09/2016.
 */
public class FromAsyncApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }
}
