package Projet.travelpulse;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import android.text.TextUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import Projet.travelpulse.model.Trip;

public class AddTripActivity extends AppCompatActivity {

    private TextInputEditText tripNameEditText;
    private Button saveTripButton;
    private MaterialToolbar toolbar;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        toolbar = findViewById(R.id.toolbar_add_trip);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(R.string.title_activity_add_trip);

        tripNameEditText = findViewById(R.id.edit_text_trip_name);
        saveTripButton = findViewById(R.id.button_save_trip);

        firestore = FirebaseFirestore.getInstance();

        // Mode édition : pré-remplir si extras présents
        String tripId = getIntent().getStringExtra("trip_id");
        String tripName = getIntent().getStringExtra("trip_name");
        if (tripId != null && tripName != null) {
            tripNameEditText.setText(tripName);
            saveTripButton.setText("Modifier");
        }

        saveTripButton.setOnClickListener(v -> saveTrip());
    }

    private void saveTrip() {
        String tripName = tripNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(tripName)) {
            Toast.makeText(this, "Le nom du voyage ne peut pas être vide", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String tripId = getIntent().getStringExtra("trip_id");
        if (tripId != null) {
            // Modification
            firestore.collection("trips").document(tripId)
                .update("name", tripName)
                .addOnSuccessListener(aVoid -> {
                    setResult(RESULT_OK);
                    Toast.makeText(AddTripActivity.this, "Voyage modifié !", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTripActivity.this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
                });
        } else {
            // Création
            Trip newTrip = new Trip(tripName, user.getUid(), 0.0, 0.0); // Latitude/longitude à compléter si besoin
            firestore.collection("trips").add(newTrip)
                .addOnSuccessListener(documentReference -> {
                    setResult(RESULT_OK);
                    Toast.makeText(AddTripActivity.this, "Voyage sauvegardé !", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTripActivity.this, "Erreur lors de la sauvegarde du voyage", Toast.LENGTH_SHORT).show();
                });
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
}
