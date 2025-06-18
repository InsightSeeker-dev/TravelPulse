package Projet.travelpulse.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class LocationPoint {
    private String id;
    private String tripId;
    private double latitude;
    private double longitude;
    @ServerTimestamp
    private Date timestamp;

    public LocationPoint() {}

    public LocationPoint(String tripId, double latitude, double longitude) {
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
