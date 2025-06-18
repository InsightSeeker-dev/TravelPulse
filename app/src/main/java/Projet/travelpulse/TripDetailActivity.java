package Projet.travelpulse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Projet.travelpulse.service.LocationTrackingService;

public class TripDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "Projet.travelpulse.EXTRA_TRIP_ID";
    public static final String EXTRA_TRIP_NAME = "Projet.travelpulse.EXTRA_TRIP_NAME";
    private static final int PERMISSIONS_REQUEST_CODE = 2;

    private MaterialToolbar toolbar;
    private TextView textViewTripName;
    private Button buttonToggleTracking;
    private Button buttonEditTrip;
    private Button buttonShowMap;
    private Button buttonExportGpx;

    private boolean trackingActive = false;
    private String tripIdString;
    private String tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        toolbar = findViewById(R.id.toolbar_trip_detail);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        textViewTripName = findViewById(R.id.text_view_trip_detail_name);
        buttonToggleTracking = findViewById(R.id.button_toggle_tracking);
        buttonEditTrip = findViewById(R.id.button_edit_trip);
        buttonShowMap = findViewById(R.id.button_show_map);
        buttonExportGpx = findViewById(R.id.button_export_gpx);

        if (getIntent().hasExtra(EXTRA_TRIP_ID) && getIntent().hasExtra(EXTRA_TRIP_NAME)) {
            tripIdString = getIntent().getStringExtra(EXTRA_TRIP_ID);
            tripName = getIntent().getStringExtra(EXTRA_TRIP_NAME);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(tripName);
            }
            textViewTripName.setText(tripName);
        } else {
            Toast.makeText(this, "Erreur: Données du voyage manquantes.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        buttonToggleTracking.setOnClickListener(v -> {
            if (!trackingActive) {
                if (checkAndRequestPermissions()) {
                    startTrackingService();
                }
            } else {
                stopTrackingService();
            }
        });

        buttonEditTrip.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailActivity.this, AddTripActivity.class);
            intent.putExtra("trip_id", tripIdString);
            intent.putExtra("trip_name", tripName);
            startActivity(intent);
        });

        buttonShowMap.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailActivity.this, MapActivity.class);
            intent.putExtra("trip_id", tripIdString);
            startActivity(intent);
        });

        buttonExportGpx.setOnClickListener(v -> exportGpxKml());

        updateUiBasedOnServiceState();
    }

    private boolean checkAndRequestPermissions() {
        String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void startTrackingService() {
        Intent serviceIntent = new Intent(this, Projet.travelpulse.service.LocationTrackingService.class);
        serviceIntent.setAction(Projet.travelpulse.service.LocationTrackingService.ACTION_START_TRACKING);
        serviceIntent.putExtra(Projet.travelpulse.service.LocationTrackingService.EXTRA_TRIP_ID, tripIdString);
        ContextCompat.startForegroundService(this, serviceIntent);
        Toast.makeText(this, "Démarrage du suivi GPS...", Toast.LENGTH_SHORT).show();
        trackingActive = true;
        updateUiBasedOnServiceState();
    }

    private void stopTrackingService() {
        Intent serviceIntent = new Intent(this, Projet.travelpulse.service.LocationTrackingService.class);
        serviceIntent.setAction(Projet.travelpulse.service.LocationTrackingService.ACTION_STOP_TRACKING);
        ContextCompat.startForegroundService(this, serviceIntent);
        Toast.makeText(this, "Arrêt du suivi GPS.", Toast.LENGTH_SHORT).show();
        trackingActive = false;
        updateUiBasedOnServiceState();
    }

    private void updateUiBasedOnServiceState() {
        if (trackingActive) {
            buttonToggleTracking.setText("Arrêter Enregistrement Tracé");
        } else {
            buttonToggleTracking.setText("Démarrer Enregistrement Tracé");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startTrackingService();
            } else {
                Toast.makeText(this, "Permission localisation requise pour enregistrer le parcours.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportGpxKml() {
        FirebaseFirestore.getInstance()
            .collection("trips").document(tripIdString).collection("points")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> gpxPoints = new ArrayList<>();
                List<String> kmlPoints = new ArrayList<>();
                StringBuilder gpxMeta = new StringBuilder();
                StringBuilder kmlMeta = new StringBuilder();
                gpxMeta.append("<name>" + tripName + "</name>\n");
                kmlMeta.append("<name>" + tripName + "</name>\n");
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Double lat = doc.getDouble("latitude");
                    Double lng = doc.getDouble("longitude");
                    com.google.firebase.Timestamp ts = doc.getTimestamp("timestamp");
                    String timeStr = ts != null ? ts.toDate().toInstant().toString() : "";
                    if (lat != null && lng != null) {
                        gpxPoints.add("<trkpt lat=\"" + lat + "\" lon=\"" + lng + "\">" + (timeStr.isEmpty() ? "" : ("<time>" + timeStr + "</time>")) + "</trkpt>");
                        kmlPoints.add("<gx:coord>" + lng + " " + lat + (timeStr.isEmpty() ? "" : (" " + timeStr)) + "</gx:coord>");
                    }
                }
                String gpx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"1.1\" creator=\"TravelPulse\">\n<trk>\n" +
                        gpxMeta + "<trkseg>\n" +
                        String.join("\n", gpxPoints) +
                        "\n</trkseg>\n</trk>\n</gpx>";
                String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n<Document>\n" +
                        kmlMeta + "<Placemark>\n<gx:Track>\n" +
                        String.join("\n", kmlPoints) +
                        "\n</gx:Track>\n</Placemark>\n</Document>\n</kml>";
                shareExportFile(gpx, "parcours.gpx", "application/gpx+xml");
                shareExportFile(kml, "parcours.kml", "application/vnd.google-earth.kml+xml");
            });
    }

    private void shareExportFile(String content, String fileName, String mimeType) {
        try {
            File file = new File(getExternalCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Partager " + fileName));
        } catch (Exception e) {
            Toast.makeText(this, "Erreur export : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
