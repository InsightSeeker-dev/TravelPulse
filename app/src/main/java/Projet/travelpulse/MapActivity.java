package Projet.travelpulse;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialiser la Toolbar comme ActionBar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_trip_detail);
        setSupportActionBar(toolbar);

        // Afficher la flèche retour et le titre
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Carte du voyage");
        }

        tripId = getIntent().getStringExtra("trip_id");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (tripId == null) {
            Toast.makeText(this, "Aucun voyage sélectionné.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Charger les points GPS depuis Firestore
        FirebaseFirestore.getInstance()
                .collection("trips").document(tripId).collection("points")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LatLng> latLngs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        if (lat != null && lng != null) {
                            latLngs.add(new LatLng(lat, lng));
                        }
                    }
                    if (!latLngs.isEmpty()) {
                        // Tracer le parcours
                        mMap.addPolyline(new PolylineOptions()
                                .addAll(latLngs)
                                .color(Color.BLUE)
                                .width(8)
                        );
                        // Marqueur départ
                        mMap.addMarker(new MarkerOptions()
                                .position(latLngs.get(0))
                                .title("Départ")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        // Marqueur arrivée
                        if (latLngs.size() > 1) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLngs.get(latLngs.size() - 1))
                                    .title("Arrivée")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                        // Centrer la carte sur le parcours
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 15f));
                    } else {
                        Toast.makeText(this, "Aucun point GPS pour ce voyage.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur chargement des points.", Toast.LENGTH_SHORT).show());
    }

    // Gère le clic sur la flèche retour
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
