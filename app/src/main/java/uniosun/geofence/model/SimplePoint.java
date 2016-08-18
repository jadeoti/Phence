package uniosun.geofence.model;

import java.io.Serializable;

/**
 * Created by Morph-Deji on 8/5/2016.
 */

public class SimplePoint implements Serializable {

    private double latitude;
    private double longitude;
    private double radius;
    private String description;
    private String id;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SimplePoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                ", description='" + description + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
