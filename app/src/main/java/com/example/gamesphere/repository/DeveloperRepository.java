package com.example.gamesphere.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gamesphere.model.Developer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

/**
 * The class interacts with Developer realtime table.
 * Operations such as: fetchAllDevelopers, fetchDeveloperById, etc.
 */
public class DeveloperRepository {
    private final DatabaseReference developerRef;

    public DeveloperRepository() {
        developerRef = FirebaseDatabase.getInstance().getReference("Developer"); // reference to the table
    }

    /**
     * Asynchronous function which fetches all the rows on Developer table (Ordered by name)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchAllDevelopers(DeveloperListCallback callback)
    {
        developerRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Developer> developers = new ArrayList<>();

                // Loop through the children of the "Developer" node (=all rows)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long id = snapshot.child("id").getValue(java.lang.Long.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id != null && name != null) {
                        developers.add(new Developer(id, name)); // Add object
                    }
                }
                callback.onDeveloperListFetched(developers); // Pass data back to the callback
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DeveloperRepository", "Error fetching developer: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Asynchronous function which fetches single Developer row by the given id.
     * @param developerID   id to fetch
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchDeveloperById(long developerID, DeveloperCallback callback)
    {
        DatabaseReference developerRow = developerRef.child(String.valueOf(developerID)); // the row
        developerRow.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Developer developer = snapshot.getValue(Developer.class);
                    callback.onSuccess(developer);
                } else {
                    callback.onFailure("Developer not found");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Interface for DeveloperListCallback callback
     */
    public interface DeveloperListCallback {
        void onDeveloperListFetched(ArrayList<Developer> data);
    }

    /**
     * Interface for DeveloperCallback callback
     */
    public interface DeveloperCallback {
        void onSuccess(Developer developer);
        void onFailure(String errorMessage);
    }
}
