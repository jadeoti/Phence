package uniosun.geofence;

import android.net.Uri;

import com.google.android.gms.location.Geofence;

/**
 * Constants used in companion app.
 */
public final class Constants {

    public static final String TAG = "Constants";
    // Request code to attempt to resolve Google Play services connection failures.
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    public static final long CONNECTION_TIME_OUT_MS = 100;
    // For the purposes of this demo, the geofences are hard-coded and should not expire.
    // An app with dynamically-created geofences would want to include a reasonable expiration time.
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;
    // Geofence parameters for the Android building on Google's main campus in Mountain View.
    public static final String ANDROID_BUILDING_ID = "1";
    /*6.644010,3.360248*/
    public static final double ANDROID_BUILDING_LATITUDE = 6.644010;
    public static final double ANDROID_BUILDING_LONGITUDE = 3.360248;
    //public static final double ANDROID_BUILDING_LONGITUDE = 6.644010;
    //public static final double ANDROID_BUILDING_LONGITUDE = 3.360248;
    public static final float ANDROID_BUILDING_RADIUS_METERS = 100.0f;
    // Geofence parameters for the Yerba Buena Gardens near the Moscone Center in San Francisco.
    public static final String YERBA_BUENA_ID = "2";
    public static final double YERBA_BUENA_LATITUDE = 6.644010;
    public static final double YERBA_BUENA_LONGITUDE = 3.360248;
    /*public static final double YERBA_BUENA_LATITUDE = 3.419063128530979;
    public static final double YERBA_BUENA_LONGITUDE = 6.439881128172646;*/
    public static final float YERBA_BUENA_RADIUS_METERS = 100.0f;
    // Path for the DataItem containing the last geofence id entered.
    public static final String GEOFENCE_DATA_ITEM_PATH = "/geofenceid";
    /**/


    // The constants below are less interesting than those above.
    public static final Uri GEOFENCE_DATA_ITEM_URI =
            new Uri.Builder().scheme("wear").path(GEOFENCE_DATA_ITEM_PATH).build();
    public static final String KEY_GEOFENCE_ID = "geofence_id";
    // Keys for flattened geofences stored in SharedPreferences.
    public static final String KEY_LATITUDE = "uniosun.geofence.KEY_LATITUDE";
    public static final String KEY_LONGITUDE = "uniosun.geofence.KEY_LONGITUDE";
    public static final String KEY_RADIUS = "uniosun.geofence.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION =
            "uniosun.geofence.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE =
            "uniosun.geofence.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys.
    public static final String KEY_PREFIX = "uniosun.geofence.KEY";
    // Invalid values, used to test geofence storage when retrieving geofences.
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;

    private Constants() {
    }

}
