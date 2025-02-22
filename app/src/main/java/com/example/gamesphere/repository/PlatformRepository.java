package com.example.gamesphere.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gamesphere.model.Platform;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The class interacts with Platform realtime table.
 * Operations such as: fetchAllPlatforms, fetchPlatformsByIDs, etc.
 */
public class PlatformRepository {
    private final DatabaseReference platformsRef;

    public PlatformRepository() {
        platformsRef = FirebaseDatabase.getInstance().getReference("Platform"); // reference to the table
    }

    /**
     * Asynchronous function which fetches all the rows on Platform table (Ordered by name)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchAllPlatforms(DataCallback callback) {
        platformsRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Platform> platforms = new ArrayList<>();

                // Loop through the children of the "Platform" node (=all rows)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long id = snapshot.child("id").getValue(Long.class);
                    String name = snapshot.child("name").getValue(String.class);
                    if (id != null && name != null) {
                        platforms.add(new Platform(id, name)); // Add object
                    }
                }
                callback.onDataFetched(platforms); // Pass data back to the callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PlatformRepository", "Error fetching platforms: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Asynchronous function which fetches the Platform objects, of the given ids
     * @param platformIds  an array of game platform ids
     * @return on success- task result, otherwise- exception.
     */
    public Task<ArrayList<Platform>> fetchPlatformsByIDs(List<Long> platformIds)
    {
        // Create a TaskCompletionSource to return the result asynchronously (or error, if exists)
        TaskCompletionSource<ArrayList<Platform>> taskCompletionSource = new TaskCompletionSource<>();

        // create a task for every id (mission: to find the row)
        List<Task<DataSnapshot>> platformTasks = new ArrayList<>();
        for (Long platformID : platformIds) {
            platformTasks.add(platformsRef.child(String.valueOf(platformID)).get());
        }
        Tasks.whenAllSuccess(platformTasks).addOnCompleteListener(task -> {
            ArrayList<Platform> platforms = new ArrayList<>();
            if (task.isSuccessful()) {
                for (Task<DataSnapshot> platformTask : platformTasks) {
                    DataSnapshot platformSnapshot = platformTask.getResult(); // get current result (=the row)
                    if (platformSnapshot.exists()) {
                        Platform platform = platformSnapshot.getValue(Platform.class);
                        if (platform != null) {
                            platforms.add(platform);
                        }
                    }
                }
                taskCompletionSource.setResult(platforms);
            } else {
                taskCompletionSource.setException(new Exception("Failed to fetch platform details"));
            }
        });
        return taskCompletionSource.getTask();
    }


    /**
     * Interface for DataCallback callback
     */
    public interface DataCallback {
        void onDataFetched(ArrayList<Platform> data);
    }

}
