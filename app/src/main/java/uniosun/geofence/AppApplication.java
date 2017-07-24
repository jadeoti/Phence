package uniosun.geofence;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Morph-Deji on 8/16/2016.
 */

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
