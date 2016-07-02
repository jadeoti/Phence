package uniosun.geofence.model;

import com.google.android.gms.location.Geofence;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * A single Geofence object, defined by its center and radius.
 */
public class SimpleGeofence {

    // Instance variables
    private String id;
    private double latitude;
    private double longitude;
    private float radius;
    private long expirationDuration;
    private int transitionType;
    private String description;
    private boolean isEnabled;

    public SimpleGeofence() {
    }

    /**
     * @param geofenceId  The Geofence's request ID.
     * @param latitude    Latitude of the Geofence's center in degrees.
     * @param longitude   Longitude of the Geofence's center in degrees.
     * @param radius      Radius of the geofence circle in meters.
     * @param expiration  Geofence expiration duration.
     * @param transition  Type of Geofence transition.
     * @param description Type of Geofence transition.
     */
    public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius,
                          long expiration, int transition, String description, boolean isEnabled) {
        // Set the instance fields from the constructor.
        this.id = geofenceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.expirationDuration = expiration;
        this.transitionType = transition;
        this.description = description;
        this.isEnabled = isEnabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public long getExpirationDuration() {
        return expirationDuration;
    }

    public void setExpirationDuration(long expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    /*// Instance field getters.
    public String getId() {
        return id;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public float getRadius() {
        return radius;
    }
    public long getExpirationDuration() {
        return expirationDuration;
    }
    public int getTransitionType() {
        return transitionType;
    }
    public String getDescription() {
        return description;
    }
    public boolean isEnabled() {
        return isEnabled;
    }


    public void setId(String mId) {
        this.id = mId;
    }

    public void setLatitude(double mLatitude) {
        this.latitude = mLatitude;
    }

    public void setLongitude(double mLongitude) {
        this.longitude = mLongitude;
    }

    public void setRadius(float mRadius) {
        this.radius = mRadius;
    }

    public void setExpirationDuration(long mExpirationDuration) {
        this.expirationDuration = mExpirationDuration;
    }

    public void setTransitionType(int mTransitionType) {
        this.transitionType = mTransitionType;
    }

    public void setDescription(String mDescription) {
        this.description = mDescription;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }*/

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(description)
                .setTransitionTypes(transitionType)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(expirationDuration)
                .setLoiteringDelay(5000)
                .build();
    }

    /**
     * Creates a Map object from a SimpleGeofence.
     *
     * @return A Map object.
     */
    // [START geofence_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("radius", radius);
        result.put("expirationDuration", expirationDuration);
        result.put("transitionType", transitionType);
        result.put("description", description);
        result.put("isEnabled", isEnabled);

        return result;
    }


    /**
     * Creates String represention.
     *
     * @return A String object.
     */
    // [START geofence_to_string]
    @Override
    public String toString() {
        return "SimpleGeofence{" +
                "id='" + id + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                ", expirationDuration=" + expirationDuration +
                ", transitionType=" + transitionType +
                ", description='" + description + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
