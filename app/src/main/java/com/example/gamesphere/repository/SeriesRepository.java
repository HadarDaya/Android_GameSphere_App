package com.example.gamesphere.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.gamesphere.model.Series;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * The class interacts with Series realtime table.
 * Operations such as: fetchAllSeries, fetchSeriesById, etc.
 */
public class SeriesRepository {
    private final DatabaseReference seriesRef;

    public SeriesRepository()
    {
        seriesRef = FirebaseDatabase.getInstance().getReference("Series"); // reference to the table
    }

    /**
     * Asynchronous function which fetches all the rows on Series table (Ordered by name)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchAllSeries(DataCallback callback)
    {
        seriesRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Series> series = new ArrayList<>();

                // Loop through the children of the "Series" node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long id = snapshot.child("id").getValue(Long.class);
                    String name = snapshot.child("name").getValue(String.class);
                    if (id != null && name != null) {
                        series.add(new Series(id, name)); // Add object
                    }
                }
                callback.onDataFetched(series); // Pass data back to the callback
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SeriesRepository", "Error fetching series: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Asynchronous function which fetches single Series row by the given id.
     * @param seriesID   id to fetch
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchSeriesById(long seriesID, SeriesCallback callback)
    {
        DatabaseReference seriesRow = seriesRef.child(String.valueOf(seriesID)); // the row
        seriesRow.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Series series = snapshot.getValue(Series.class);
                    callback.onSuccess(series);
                } else {
                    callback.onFailure("Series not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Interface for DataCallback callback
     */
    public interface DataCallback {
        void onDataFetched(ArrayList<Series> data);
    }

    /**
     * Interface for SeriesCallback callback
     */
    public interface SeriesCallback {
        void onSuccess(Series series);
        void onFailure(String errorMessage);
    }
}
