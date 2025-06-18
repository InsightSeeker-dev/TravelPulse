package Projet.travelpulse.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import Projet.travelpulse.MainActivity;
import Projet.travelpulse.model.LocationPoint;

public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";
    public static final String ACTION_START_TRACKING = "ACTION_START_TRACKING";
    public static final String ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING";
    public static final String EXTRA_TRIP_ID = "EXTRA_TRIP_ID";
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final int NOTIFICATION_ID = 1;

    public static boolean isRunning = false;
    public static long currentTripId = -1L;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private FirebaseFirestore firestore;
    private String tripId = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        firestore = FirebaseFirestore.getInstance();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (tripId != null && isRunning) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d(TAG, "New location for trip " + tripId + ": " + location.getLatitude() + ", " + location.getLongitude());
                        LocationPoint point = new LocationPoint(tripId, location.getLatitude(), location.getLongitude());
                        firestore.collection("trips")
                            .document(tripId)
                            .collection("points")
                            .add(point)
                            .addOnSuccessListener(documentReference -> Log.d(TAG, "LocationPoint saved to Firestore sous trips/" + tripId + "/points: " + documentReference.getId()))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to save LocationPoint to Firestore", e));
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand received action: " + (intent != null ? intent.getAction() : "null intent"));
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START_TRACKING.equals(action)) {
                String newTripId = intent.getStringExtra(EXTRA_TRIP_ID);
                Log.d(TAG, "ACTION_START_TRACKING for tripId: " + newTripId + ", current isRunning: " + isRunning);
                if (newTripId != null && !isRunning) {
                    this.tripId = newTripId;
                    currentTripId = -1L; 
                    isRunning = true;
                    try {
                        createNotificationChannel();
                        Intent notificationIntent = new Intent(this, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setContentTitle("TravelPulse Suivi GPS")
                                .setContentText("Suivi en cours...")
                                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                                .setContentIntent(pendingIntent)
                                .setOngoing(true)
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .build();
                        startForeground(NOTIFICATION_ID, notification);
                        startLocationUpdates();
                    } catch (Exception e) {
                        Log.e(TAG, "CRITICAL ERROR during service start and startForeground call", e);
                        isRunning = false;
                        currentTripId = -1L;
                        stopSelf();
                        return START_NOT_STICKY;
                    }
                } else if (isRunning && this.tripId != null && this.tripId.equals(newTripId)) {
                    Log.w(TAG, "Service already running for this trip ID: " + newTripId + ". Ignoring start command.");
                } else if (isRunning && this.tripId != null && !this.tripId.equals(newTripId)) {
                    Log.e(TAG, "Service is ALREADY RUNNING for a DIFFERENT trip ID (" + this.tripId + "). Request to start for " + newTripId + " is conflicting. The current tracking must be stopped first from the UI.");
                } else if (newTripId == null) {
                    Log.e(TAG, "Invalid trip ID (null) received. Cannot start tracking.");
                }
            } else if (ACTION_STOP_TRACKING.equals(action)) {
                Log.d(TAG, "ACTION_STOP_TRACKING received. Current tripId: " + this.tripId);
                stopTracking();
            }
        }
        return START_NOT_STICKY;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates called");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); 
        locationRequest.setFastestInterval(5000); 
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            Log.d(TAG, "Requested location updates.");
        } else {
            Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted. Cannot start location updates.");
            stopTracking(); 
        }
    }

    private void stopTracking() {
        Log.i(TAG, "stopTracking called for tripId: " + this.tripId);
        if (fusedLocationProviderClient != null && locationCallback != null) {
            try {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                Log.d(TAG, "Location updates removed successfully.");
            } catch (SecurityException se) {
                Log.e(TAG, "SecurityException removing location updates. Permissions revoked?", se);
            } catch (Exception e) {
                Log.e(TAG, "Error removing location updates", e);
            }
        }

        Log.i(TAG, "Calling stopForeground(true) to remove notification and exit foreground state.");
        stopForeground(true); 

        Log.i(TAG, "Calling stopSelf() to stop the service.");
        stopSelf();

        isRunning = false;
        currentTripId = -1L; 
        this.tripId = null;  
        Log.i(TAG, "Service state reset. isRunning: false. Service stopped.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy. Current isRunning: " + isRunning + ", currentTripId: " + currentTripId + ", service's this.tripId: " + this.tripId);
        isRunning = false;
        currentTripId = -1L;
        Log.d(TAG, "Service destroyed and state reset. isRunning is now: " + isRunning);
    }

    private void createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking Channel",
                    NotificationManager.IMPORTANCE_LOW 
            );
            serviceChannel.setDescription("Channel for TravelPulse location tracking notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created/updated.");
            } else {
                Log.e(TAG, "NotificationManager is null, cannot create channel.");
            }
        } else {
            Log.d(TAG, "Build version < O, no channel needed for explicit creation.");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return null;
    }
}
