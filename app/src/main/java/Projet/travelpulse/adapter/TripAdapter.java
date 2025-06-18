package Projet.travelpulse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import Projet.travelpulse.R;
import Projet.travelpulse.model.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip currentTrip = trips.get(position);
        holder.textViewTripName.setText(currentTrip.getName());
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public void setTrips(List<Trip> trips) {
        this.trips.clear();
        if (trips != null) {
            this.trips.addAll(trips);
        }
        notifyDataSetChanged();
    }

    // Interface pour la gestion des clics
    public interface OnItemClickListener {
        void onItemClick(Trip trip);
        void onItemLongClick(Trip trip);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTripName;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTripName = itemView.findViewById(R.id.textViewTripName);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(trips.get(getAdapterPosition()));
                }
            });
            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(trips.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
    }
}
