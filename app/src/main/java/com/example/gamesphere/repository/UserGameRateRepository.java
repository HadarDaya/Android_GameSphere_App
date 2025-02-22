package com.example.gamesphere.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * The class interacts with UserGameRate realtime table.
 * Operations such as: checkUserRatingByGameID, modifyRatingForGameAndUser, etc.
 */
public class UserGameRateRepository {

    private final DatabaseReference userGameRateRef;
    public UserGameRateRepository()
    {
        userGameRateRef = FirebaseDatabase.getInstance().getReference("UserGameRate");  // reference to the table
    }

    /**
     * Asynchronous function which fetches the rating of a given userID, for a given gameID.
     * (or an error string)
     * @param userID    the user id (for example, the current logged in user)
     * @param gameID    the game id
     * @param callback callback  the result is not immediate, callback will consist the result as soon as
     *                 there is one. (when the listener was informed that the mission completed)
     */
    public void checkUserRatingByGameID(String userID, Long gameID, OnRatingCheckCallback callback) {

        // the required row
        DatabaseReference ratingRef = userGameRateRef.child(userID).child(String.valueOf(gameID)).child("rate");

        ratingRef.get().addOnCompleteListener(task -> { // the mission
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer rateObj = task.getResult().getValue(Integer.class);
                if (rateObj != null) {
                    callback.onSuccess(rateObj);  // Pass data back to the callback
                }
                else
                    callback.onFailure("value not exist");
            }
            else
                callback.onFailure("row not exist");
        }).addOnFailureListener(e -> callback.onFailure("Database error: " + e.getMessage()));
    }

    /**
     * Asynchronous function which modifies the rating of a given userID, for a given gameID.
     * if the new rating is 0 -> remove the entry
     * otherwise -> update the entry
     * @param userID    user who modifies their rate
     * @param gameID    game rated
     * @param newUserRating     the new rating
     * @return Task<Void> the async operation result
     */
    public Task<Void> modifyRatingForGameAndUser(String userID, Long gameID, float newUserRating) {
        if (newUserRating == 0) {
            // remove the row
            return removeUserRating(userID, gameID);
        }
        // otherwise- update the existing row
        DatabaseReference existingRow = userGameRateRef.child(userID).child(String.valueOf(gameID)).child("rate");
        return existingRow.setValue(newUserRating);
    }

    /**
     * Asynchronous function which removes a rating entry for the given userID and gameID.
     * (if the row does not exist- success but without removal)
     * @param userID    the user who rated the game
     * @param gameID    the game whose rating entry needs to be removed
     * @return Task<Void> the async operation result
     */
    public Task<Void> removeUserRating(String userID, Long gameID) {
        DatabaseReference ratingRef = userGameRateRef.child(userID).child(String.valueOf(gameID)); // the required row
        return ratingRef.removeValue();
    }

    /**
     * Interface for OnRatingCheckCallback callback
     */
    public interface OnRatingCheckCallback {
        void onSuccess(int rate);
        void onFailure(String errorMessage);
    }
}
