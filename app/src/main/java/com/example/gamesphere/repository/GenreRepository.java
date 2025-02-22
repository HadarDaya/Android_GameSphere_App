package com.example.gamesphere.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gamesphere.model.Genre;
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
 * The class interacts with Genre realtime table.
 * Operations such as: fetchAllGenres, fetchGenresByIDs, etc.
 */
public class GenreRepository {
    private final DatabaseReference genreRef;

    public GenreRepository()
    {
        genreRef = FirebaseDatabase.getInstance().getReference("Genre"); // reference to the table
    }

    /**
     * Asynchronous function which fetches all the rows on Genre table (Ordered by name)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void fetchAllGenres(DataCallback callback) {
        // Adding a listener to the "Genre" node to fetch data only once
        genreRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Genre> genres = new ArrayList<>();

                // Loop through all children of the "Genre" node (=all rows)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Genre genre = snapshot.getValue(Genre.class); // Convert snapshot to Genre object
                    if (genre != null) {
                        genres.add(genre); // Add object
                    }
                }
                callback.onDataFetched(genres); // Pass data back to the callback
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GenreRepository", "Error fetching genre: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Asynchronous function which fetches the Genre objects, of the given ids
     * @param genreIds  an array of game genre ids
     * @return on success- task result, otherwise- exception.
     */
    public Task<ArrayList<Genre>> fetchGenresByIDs(ArrayList<Long> genreIds)
    {
        // Create a TaskCompletionSource to return the result asynchronously (or error, if exists)
        TaskCompletionSource<ArrayList<Genre>> taskCompletionSource = new TaskCompletionSource<>();

        // create a task for every id (mission: to find the row)
        List<Task<DataSnapshot>> genreTasks = new ArrayList<>();
        for (Long genreID : genreIds) {
            genreTasks.add(genreRef.child(String.valueOf(genreID)).get());
        }
        Tasks.whenAllSuccess(genreTasks).addOnCompleteListener(task -> {
            ArrayList<Genre> genres = new ArrayList<>();
            if (task.isSuccessful()) {
                for (Task<DataSnapshot> genreTask : genreTasks) {
                    DataSnapshot genreSnapshot = genreTask.getResult(); // get current result (=the row)
                    if (genreSnapshot.exists()) {
                        Genre genre = genreSnapshot.getValue(Genre.class);
                        if (genre != null) {
                            genres.add(genre);
                        }
                    }
                }
                taskCompletionSource.setResult(genres);
            } else {
                taskCompletionSource.setException(new Exception("Failed to fetch genre details"));
            }
        });
        return taskCompletionSource.getTask();
    }

    /**
     * Interface for DataCallback callback
     */
    public interface DataCallback {
        void onDataFetched(ArrayList<Genre> data);
    }
}
