package com.example.gamesphere.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gamesphere.model.Publisher;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

/**
 * The class interacts with Publisher realtime table.
 * Operations such as: fetchAllPublishers, fetchPublisherById, etc.
 */
public class PublisherRepository {
    private final DatabaseReference publisherRef;

    public PublisherRepository() {
        publisherRef = FirebaseDatabase.getInstance().getReference("Publisher"); // reference to the table
    }

    /**
     * Asynchronous function which fetches all the rows on Publisher table (Ordered by name)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchAllPublishers(DataCallback callback)
    {
        publisherRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Publisher> publishers = new ArrayList<>();

                // Loop through the children of the "Publisher" node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long id = snapshot.child("id").getValue(Long.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id != null && name != null) {
                        publishers.add(new Publisher(id, name)); // Add object
                    }
                }
                callback.onDataFetched(publishers); // Pass data back to the callback
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PublisherRepository", "Error fetching publisher: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Asynchronous function which fetches single Publisher row by the given id.
     * @param publisherID   id to fetch
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchPublisherById(long publisherID, PublisherCallback callback) {
        DatabaseReference publisherRow = publisherRef.child(String.valueOf(publisherID)); // the row
        publisherRow.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Publisher publisher = snapshot.getValue(Publisher.class);
                    callback.onSuccess(publisher);
                } else {
                    callback.onFailure("Publisher not found");
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
        void onDataFetched(ArrayList<Publisher> data);
    }

    /**
     * Interface for PublisherCallback callback
     */
    public interface PublisherCallback {
        void onSuccess(Publisher publisher);
        void onFailure(String errorMessage);
    }
}
