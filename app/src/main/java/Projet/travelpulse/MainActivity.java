package Projet.travelpulse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import Projet.travelpulse.adapter.TripAdapter; 
import Projet.travelpulse.model.Trip;
import Projet.travelpulse.viewmodel.TripViewModel;

// Supposons que ViewBinding est activé pour le module :app
// import Projet.travelpulse.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // Si ViewBinding est activé:
    // private ActivityMainBinding binding;

    // Si vous n'utilisez PAS ViewBinding:
    private RecyclerView recyclerViewTrips;
    private FloatingActionButton fabAddTrip;
    private TextView textViewNoTrips;
    private Toolbar toolbarMain;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TripAdapter tripAdapter;
    private TripViewModel tripViewModel;

    // Constante pour le nom de classe de LoginActivity (pour la navigation inter-modules)
    private static final String LOGIN_ACTIVITY_CLASS_NAME = "Projet.authactivity.ui.login.LoginActivity";

    // Utilise ActivityResultLauncher moderne pour garantir le retour immédiat
    private final androidx.activity.result.ActivityResultLauncher<Intent> addTripLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            recyclerViewTrips.scrollToPosition(0);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Si ViewBinding est activé:
        // binding = ActivityMainBinding.inflate(getLayoutInflater());
        // setContentView(binding.getRoot());
        // setSupportActionBar(binding.toolbarMain); // Ou le nom de votre Toolbar dans le binding

        // Si vous n'utilisez PAS ViewBinding:
        setContentView(R.layout.activity_main);
        toolbarMain = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbarMain);

        recyclerViewTrips = findViewById(R.id.recyclerViewTrips);
        fabAddTrip = findViewById(R.id.fabAddTrip);
        textViewNoTrips = findViewById(R.id.textViewNoTrips);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        recyclerViewTrips.setLayoutManager(new LinearLayoutManager(this));
        tripAdapter = new TripAdapter();
        recyclerViewTrips.setAdapter(tripAdapter);

        // Observe les voyages Firestore de l'utilisateur connecté
        tripViewModel.getAllTrips().observe(this, trips -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;
            String uid = currentUser.getUid();
            // Filtre côté client pour n'afficher que les voyages de l'utilisateur connecté
            List<Trip> userTrips = new ArrayList<>();
            if (trips != null) {
                for (Trip t : trips) {
                    if (uid.equals(t.getUserId())) {
                        userTrips.add(t);
                    }
                }
            }
            tripAdapter.setTrips(userTrips);
            if (userTrips.isEmpty()) {
                recyclerViewTrips.setVisibility(View.GONE);
                textViewNoTrips.setVisibility(View.VISIBLE);
            } else {
                recyclerViewTrips.setVisibility(View.VISIBLE);
                textViewNoTrips.setVisibility(View.GONE);
            }
        });

        // Gestion du clic sur un voyage : ouvrir TripDetailActivity (visualisation et enregistrement parcours)
        tripAdapter.setOnItemClickListener(new TripAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Trip trip) {
                Intent intent = new Intent(MainActivity.this, TripDetailActivity.class);
                intent.putExtra(TripDetailActivity.EXTRA_TRIP_ID, trip.getDocumentId());
                intent.putExtra(TripDetailActivity.EXTRA_TRIP_NAME, trip.getName());
                startActivity(intent);
            }
            @Override
            public void onItemLongClick(Trip trip) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Supprimer ce voyage ?")
                        .setMessage("Voulez-vous vraiment supprimer ce voyage ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            FirebaseFirestore.getInstance()
                                    .collection("trips")
                                    .document(trip.getDocumentId())
                                    .delete();
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            }
        });

        fabAddTrip.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTripActivity.class);
            addTripLauncher.launch(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Aucun utilisateur connecté, rediriger vers LoginActivity
            navigateToLogin();
        } 
    }

    private void navigateToLogin() {
        try {
            Class<?> loginActivityClass = Class.forName(LOGIN_ACTIVITY_CLASS_NAME);
            Intent intent = new Intent(MainActivity.this, loginActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "LoginActivity class not found: " + LOGIN_ACTIVITY_CLASS_NAME, e);
            Toast.makeText(MainActivity.this, "Erreur: Session expirée, veuillez vous reconnecter.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // menu_main.xml sera créé
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            navigateToLogin();
            return true;
        }
        // Gérer d'autres actions du menu ici si nécessaire
        return super.onOptionsItemSelected(item);
    }
}