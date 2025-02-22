package com.example.gamesphere.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gamesphere.model.Game;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class interacts with Game realtime table.
 * Operations such as: insertGame, checkGameNameExists, etc.
 */
public class GameRepository {

    private final DatabaseReference gameRef;

    public GameRepository() {
        gameRef = FirebaseDatabase.getInstance().getReference("Game"); // reference to the table
    }

    /**
     *
     * @param game  a Game object (already valid and initialized with fields)
     * @param callback  callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     *                  returns a string of the new inserted gameID object, otherwise- exception.
     */
    public void insertGame(Game game, OnGameAddedListener callback)
    {
        // Retrieve the current highest game ID
        // ----------------------------------------------
        gameRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long newGameId = 1; // Default ID if no games exist

                // Find the highest existing game ID and increment it
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        long lastGameId = Long.parseLong(snapshot.getKey());
                        newGameId = lastGameId + 1;
                    } catch (NumberFormatException e) {
                        Log.e("Firebase", "Error parsing game ID", e);
                    }
                }

                // create a new entry from the given game and newGameId
                // ----------------------------------------------
                // Set the new ID
                game.setId(newGameId);

                long finalNewGameId = newGameId;
                gameRef.child(String.valueOf(newGameId)).setValue(game) // Insert the new game
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onSuccess(String.valueOf(finalNewGameId));
                                Log.e("Firebase", "Success");
                            } else {
                                callback.onFailure(task.getException());
                                Log.e("Firebase", "Failure");
                            }
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    /**
     * Asynchronous function which checks if the given game name already exists.
     * @param gameName the username to check
     * @return task result (true- if exists, otherwise- false)
     */
    public Task<Boolean> checkGameNameExists(String gameName)
    {
        // Create a TaskCompletionSource to manage the Task
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        Query query = gameRef.orderByChild("name").equalTo(gameName); // get the row in Game
        // // a listener to handle the query result
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the game name exists in the database
                boolean gameExists = dataSnapshot.exists();
                // Set the result
                taskCompletionSource.setResult(gameExists);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                taskCompletionSource.setException(databaseError.toException());
            }
        });
        return taskCompletionSource.getTask();
    }

    /**
     * Asynchronous function which fetches the Game objects, of the given ids
     * (For example: for displaying user's favorites)
     * @param gameIDs  an array of game ids
     * @return on success- task result, otherwise- exception.
     */
    public Task<ArrayList<Game>> fetchGamesByIDs(List<String> gameIDs)
    {
        // Create a TaskCompletionSource to return the result asynchronously (or error, if exists)
        TaskCompletionSource<ArrayList<Game>> taskCompletionSource = new TaskCompletionSource<>();

        // create a task for every id (mission: to find the row)
        List<Task<DataSnapshot>> gameTasks = new ArrayList<>();
        for (String gameID : gameIDs) {
            gameTasks.add(gameRef.child(gameID).get());
        }
        Tasks.whenAllSuccess(gameTasks).addOnCompleteListener(task -> {
            ArrayList<Game> games = new ArrayList<>();
            if (task.isSuccessful()) {
                for (Task<DataSnapshot> gameTask : gameTasks) {
                    DataSnapshot gameSnapshot = gameTask.getResult(); // get current result (=the row)
                    if (gameSnapshot.exists()) {
                        Game game = gameSnapshot.getValue(Game.class);
                        if (game != null) {
                            games.add(game);
                        }
                    }
                }
                taskCompletionSource.setResult(games);
            } else {
                taskCompletionSource.setException(new Exception("Failed to fetch game details"));
            }
        });
        return taskCompletionSource.getTask();
    }

    /**
     * Asynchronous function which fetches all the rows on Game table (Ordered by name)
     * @return a task with the array of games, or exception.
     */
    public Task<ArrayList<Game>> fetchAllGames()
    {
        TaskCompletionSource<ArrayList<Game>> taskCompletionSource = new TaskCompletionSource<>();

        // Order the games by the 'name' field (in ascending order by default)
        gameRef.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Game> games = new ArrayList<>();
                // Iterate over each game in the snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Game game = snapshot.getValue(Game.class);
                    if (game != null) {
                        games.add(game);
                    }
                }
                // Return the list
                taskCompletionSource.setResult(games);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });
        return taskCompletionSource.getTask();
    }

    /**
     * Asynchronous function calculates fields numOfRaters, avgRate for the given gameID.
     * @param gameID    the game which is rated
     * @param newUserRating     the new rating (as selected by user)
     * @param oldUserRating     the new rating (as selected previously by user)
     * @param alreadyRated      boolean
     * @return a task with updatedValues (a map with keys: numOfRaters, avgRate and keys: their values)
     */
    public Task<Map<String, Object>> modifyRateByGameID(Long gameID, float newUserRating,
                                                        float oldUserRating, boolean alreadyRated)
    {
        TaskCompletionSource<Map<String, Object>> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference gameRowRef = gameRef.child(String.valueOf(gameID)); // get game row
        gameRowRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) { // game not exist
                taskCompletionSource.setResult(null);
                return;
            }
            // a row exists
            // 1. get current avgRate, numOfRaters
            Integer numOfRatersObj = snapshot.child("numOfRaters").getValue(Integer.class);
            Float avgRateObj = snapshot.child("avgRate").getValue(Float.class);

            int numOfRaters = (numOfRatersObj != null) ? numOfRatersObj : 0;
            float avgRate = (avgRateObj != null) ? avgRateObj : 0.0f;

            // 2. calculate new avgRate, newNumOfRaters
            float newAvgRate;
            int newNumOfRaters = numOfRaters;
            if(newUserRating == 0) {
                if ((numOfRaters == 1 && alreadyRated) || numOfRaters == 0) {
                    newAvgRate = 0;
                    newNumOfRaters = 0;
                }
                else if (numOfRaters > 1)
                {
                    if(alreadyRated)
                        newNumOfRaters = newNumOfRaters - 1;
                    newAvgRate = ((avgRate * numOfRaters) - oldUserRating + newUserRating) / newNumOfRaters;
                }
                else {
                    newAvgRate = avgRate;
                }
            }
            else {
                if (alreadyRated) {
                    newAvgRate = ((avgRate * numOfRaters) - oldUserRating + newUserRating) / numOfRaters;
                } else {
                    newNumOfRaters++; // Increase numOfRaters for new ratings
                    newAvgRate = ((avgRate * numOfRaters) + newUserRating) / newNumOfRaters;
                }
            }
            // 3. create the map with updated values
            Map<String, Object> updatedValues = new HashMap<>();
            updatedValues.put("avgRate", newAvgRate);
            updatedValues.put("numOfRaters", newNumOfRaters);

            // 4. update values in Game table
            gameRowRef.updateChildren(updatedValues)
                    .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(updatedValues))  // Return the map
                    .addOnFailureListener(taskCompletionSource::setException);
        }).addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    /**
     * Interface for OnGameAddedListener callback
     */
    public interface OnGameAddedListener {
        void onSuccess(String gameId);

        void onFailure(Exception exception);
    }
}
