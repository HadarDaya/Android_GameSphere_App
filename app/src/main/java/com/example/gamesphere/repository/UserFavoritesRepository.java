package com.example.gamesphere.repository;

import androidx.annotation.NonNull;
import com.example.gamesphere.model.Game;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

/**
 * The class interacts with UserFavorites realtime table.
 * Operations such as: checkIfUserFavorites, toggleUserFavorite, etc.
 */
public class UserFavoritesRepository {

    private final DatabaseReference userFavoritesRef;
    private final GameRepository gameRepository = new GameRepository();

    public UserFavoritesRepository()
    {
        userFavoritesRef = FirebaseDatabase.getInstance().getReference("UserFavorites"); // reference to the table
    }

    /**
     * Asynchronous function which checks if the given user had already rated the given game.
     * @param userID    the user id (for example, the current logged in user)
     * @param gameID    the game id
     * @param callback the result is not immediate, callback will consist the result as soon as
     *                 there is one. (when the listener was informed that the mission completed)
     *                 in that case, true (if favorite) or false (if not).
     */
    public void checkIfUserFavorites(String userID, Long gameID, OnFavoritesCheckCallback callback) {

        DatabaseReference favoritesRef = userFavoritesRef.child(userID).child(String.valueOf(gameID)); // get the row (if exists)

        favoritesRef.get().addOnCompleteListener(task -> { // the mission: search for a row
            // a row does not exist
            boolean exists = task.isSuccessful() && task.getResult().exists();
            callback.onSuccess(exists); // boolean
        }).addOnFailureListener(e -> callback.onFailure(false)); // a row does not exists
    }

    /**
     * Asynchronous function which handles insertion/removal of a game from user's favorites
     * (if exists- the action is removal, if not exist - the action is insertion)
     * @param userID    the user id (for example, the current logged in user)
     * @param gameID    the game id
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     *                  if the operation was successful -> if the operation is insertion -> true
     *                                                  -> if the operation is removal -> false
     *                   otherwise -> false.
     */
    public void toggleUserFavorite(String userID, Long gameID, OnToggleUserFavoriteCallback callback)
    {
        DatabaseReference rowRef = userFavoritesRef.child(userID).child(String.valueOf(gameID)); // get the row (if exists)

        rowRef.get().addOnCompleteListener(task -> { // the mission: search for a row
            if (task.isSuccessful() && task.getResult().exists()) { // the row exists
                // Game is already favorite-> user asked to remove it -> Remove the row (return added = false)
                rowRef.removeValue()
                        .addOnSuccessListener(aVoid -> callback.onSuccess(false))  // Indicate removal success
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            } else { // the row does not exist
                // Game is not favorite -> user pressed favorite -> Add a row (return added = true)
                rowRef.setValue(true)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(true))   // Indicate addition success
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }
        });
    }

    /**
     *  Asynchronous function which fetches all user's favorite games, from Game table.
     * @param userID    the given user id (for example, logged in user)
     * @return an array of Game objects
     */
    public Task<ArrayList<Game>> fetchAllUserFavoriteGames(String userID)
    {
        TaskCompletionSource<ArrayList<Game>> taskCompletionSource = new TaskCompletionSource<>();
        // get the row (if exists)
        DatabaseReference userFavoritesRef = FirebaseDatabase.getInstance().getReference("UserFavorites").child(userID);

        // get user's favorite game ids
        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userFavoritesSnapshot) {
                ArrayList<String> gameIDs = new ArrayList<>();

                for (DataSnapshot gameIDSnapshot : userFavoritesSnapshot.getChildren()) {
                    String gameID = gameIDSnapshot.getKey();
                    if (gameID != null) {
                        gameIDs.add(gameID);
                    }
                }
                // get the game row for the current gameID
                gameRepository.fetchGamesByIDs(gameIDs).addOnSuccessListener(taskCompletionSource::setResult)
                        .addOnFailureListener(taskCompletionSource::setException);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });
        return taskCompletionSource.getTask();
    }

    /**
     * Interface for OnFavoritesCheckCallback callback
     */
    public interface OnFavoritesCheckCallback {
        void onSuccess(boolean isFavorite);
        void onFailure(boolean isFavorite);
    }

    /**
     * Interface for OnToggleUserFavoriteCallback callback
     */
    public interface OnToggleUserFavoriteCallback {
        void onSuccess(boolean isAdded); // 'true' for added, 'false' for removed
        void onFailure(String errorMessage);
    }
}

