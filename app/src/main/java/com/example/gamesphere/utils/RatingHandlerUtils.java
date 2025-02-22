package com.example.gamesphere.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import com.example.gamesphere.R;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.repository.UserGameRateRepository;

/**
 * Helper class which Handles steps regarding to rating a game.
 */
public class RatingHandlerUtils {

    private static final UserGameRateRepository userGameRateRepository = new UserGameRateRepository();
    private static final GameRepository gameRepository = new GameRepository();

    /**
     * The function displays rating popup window, allowing user to rate the current game,
     * and handles submission.
     * @param gameID    current game id
     * @param userID    current logged in user id
     * @param context   context of action
     * @param view  view
     * @param callback callback
     */
    public static void showRatingPopupAndHandleSubmit(Long gameID, String userID, Context context,
                                                      View view, RatingUpdateCallback callback)
    {
        // Inflate the modern popup layout
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(context).inflate(R.layout.custom_popup_rating, null);

        // Create a PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Initialize UI components
        RatingBar ratingBar = popupView.findViewById(R.id.ratingPopup_ratingBar);
        Button popupSubmitBtn = popupView.findViewById(R.id.ratingPopup_submitBtn);
        dimBackground(context);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0); // centered on screen

        // Check if user had already rated the game
        final boolean[] alreadyRated = {false};
        final float[] oldUserRating = {0};
        userGameRateRepository.checkUserRatingByGameID(userID, gameID, new UserGameRateRepository.OnRatingCheckCallback() {
            @Override
            public void onSuccess(int userRating) { // Old user rating exists
                ratingBar.setRating(userRating); // set display on rating bar
                oldUserRating[0] = userRating;
                alreadyRated[0] = true;
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.d("RatingCheck", errorMessage);
            }
        });

        // Handle submit rate button click
        popupSubmitBtn.setOnClickListener(v -> {
            float newUserRating = ratingBar.getRating();
            submitRatingHandler(userID, gameID, newUserRating, oldUserRating[0], alreadyRated[0], v.getContext(), callback);
            popupWindow.dismiss();
        });
        // Restore background brightness (at this point, popup is dismissed)
        popupWindow.setOnDismissListener(() -> restoreBrightness(context));
    }


    /**
     * The function handles results of submitting user rating:
     * 1. modify/insert row to 'UserGameRate' table
     * 2. modify rating fields on 'Game' table
     * 3. updates fields (numOfRaters, avgRate) on dataset[position] and refreshes view
     * @param userID - the rater
     * @param gameID - the game to rate
     * @param newUserRating new rating
     * @param oldUserRating old rating
     * @param alreadyRated - indicator
     */
    private static void submitRatingHandler(String userID, Long gameID,
                                            float newUserRating, float oldUserRating,
                                            boolean alreadyRated, Context context,
                                            RatingUpdateCallback callback) {
        // Update 'UserGameRate' table
        userGameRateRepository.modifyRatingForGameAndUser(userID, gameID, newUserRating)
                .addOnSuccessListener(aVoid -> {
                    // Update 'Game' table
                    gameRepository.modifyRateByGameID(gameID, userID, newUserRating, oldUserRating, alreadyRated)
                            .addOnSuccessListener(updatedValues -> {
                                if (updatedValues != null) {
                                    Object avgRateObj = updatedValues.get("avgRate");
                                    Object numOfRatersObj = updatedValues.get("numOfRaters");

                                    if (avgRateObj instanceof Float && numOfRatersObj instanceof Integer) {
                                        float updatedAvgRate = (Float) avgRateObj;
                                        int updatedNumOfRaters = (Integer) numOfRatersObj;

                                        // Invoke the callback to update UI
                                        if (callback != null) {
                                            callback.onRatingUpdated(updatedAvgRate, updatedNumOfRaters);
                                        }
                                    }
                                    CustomAlertDialog.showCustomDialog(context, "Rating submitted successfully!", CustomAlertDialog.MessageType.SUCCESS);
                                }
                            });
                });
    }

    /**
     * The function makes the background of the popup become darker.
     * @param context context
     */
    private static void dimBackground(Context context) {
        Activity activity = (Activity) context;
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.alpha = 0.04f; // Adjusts brightness (lower means darker)
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * The function restores the brightness of popup's background.
     * @param context context
     */
    private static void restoreBrightness(Context context) {
        Activity activity = (Activity) context;
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.alpha = 1.0f; // Reset to full brightness
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * Interface for RatingUpdateCallback callback
     */
    public interface RatingUpdateCallback {
        void onRatingUpdated(float updatedAvgRate, int updatedNumOfRaters);
    }

}
