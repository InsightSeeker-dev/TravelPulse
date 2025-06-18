package Projet.travelpulse.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import Projet.travelpulse.model.Trip;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TripViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Trip>> allTrips = new MutableLiveData<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final CollectionReference tripsRef = firestore.collection("trips");

    public TripViewModel(@NonNull Application application) {
        super(application);
        // Ecoute Firestore en temps réel
        tripsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    allTrips.setValue(null);
                    return;
                }
                List<Trip> trips = new ArrayList<>();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Trip trip = doc.toObject(Trip.class);
                        trip.setDocumentId(doc.getId());
                        trips.add(trip);
                    }
                }
                allTrips.setValue(trips);
            }
        });
    }

    public LiveData<List<Trip>> getAllTrips() {
        return allTrips;
    }
}
