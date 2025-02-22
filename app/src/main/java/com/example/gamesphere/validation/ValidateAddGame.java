package com.example.gamesphere.validation;

import android.content.Context;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.gamesphere.R;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to validate add game operation form.
 */
public class ValidateAddGame {
    private static final GameRepository gameRepository = new GameRepository();

    /**
     * The function validates adding game form fields before submission.
     * @return task result true if all fields are valid, otherwise- false.
     */
    public static Task<Boolean> validateAddGameForm(EditText gameNameInput, Spinner developerSpinner,
                                                    TextView genreTextView, TextView platformTextView,
                                                    Spinner publisherSpinner, EditText descriptionInput,
                                                    TextView releaseYearButton, ImageView image, TextView gameImageText,
                                                    EditText youtube_URL, CheckBox singlePlayer, CheckBox multiPlayer,
                                                    Context context)
    {

        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        String gameName = gameNameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String selectedDeveloper = developerSpinner.getSelectedItem().toString();
        String selectedGenre = genreTextView.getText().toString().trim();
        String selectedPublisher = publisherSpinner.getSelectedItem().toString();
        String selectedPlatforms = platformTextView.getText().toString().trim();
        String youtubeURLString = youtube_URL.getText().toString().trim();

        // flag to track if all validations pass
        final boolean[] isValid = {true};

        // Validate Game Name
        if (gameName.isEmpty()) {
            gameNameInput.setHintTextColor(Color.RED);
            isValid[0] = false;
        } else {
            gameNameInput.setHintTextColor(Color.WHITE);
        }

        // Validate Developer selection
        TextView developerTextView = (TextView) developerSpinner.getSelectedView();
        if (selectedDeveloper.equals("Select")) {
            developerTextView.setTextColor(Color.RED);
            isValid[0] = false;
        } else {
            developerTextView.setTextColor(Color.WHITE);
        }

        // Validate Genre selection
        if (selectedGenre.equals("Select") || selectedGenre.isEmpty()) {
            genreTextView.setTextColor(Color.RED);
            isValid[0] = false;
        } else {
            genreTextView.setTextColor(Color.WHITE);
        }

        // Validate Platform selection
        if (selectedPlatforms.equals("Select") || selectedPlatforms.isEmpty()) {
            platformTextView.setTextColor(Color.RED);
            isValid[0] = false;
        } else {
            platformTextView.setTextColor(Color.WHITE);
        }

        // Validate Publisher selection
        TextView publisherTextView = (TextView) publisherSpinner.getSelectedView();
        if (selectedPublisher.equals("Select")) {
            publisherTextView.setTextColor(Color.RED);
            isValid[0] = false;
        } else {
            publisherTextView.setTextColor(Color.WHITE);
        }

        // Validate Description
        if (description.isEmpty()) {
            descriptionInput.setHintTextColor(Color.RED);
            isValid[0] = false;
        } else {
            descriptionInput.setHintTextColor(Color.WHITE);
        }

        // Validate Release Date
        if (releaseYearButton.getText().toString().equals("Select")) {
            releaseYearButton.setTextColor(Color.RED);
            isValid[0] = false;
        } else {
            releaseYearButton.setTextColor(Color.WHITE);
        }

        // Validate single/multi player
        if (!singlePlayer.isChecked() && !multiPlayer.isChecked())
        {
            singlePlayer.setTextColor(Color.RED);
            multiPlayer.setTextColor(Color.RED);
            isValid[0] = false;
        } else {
            singlePlayer.setTextColor(Color.WHITE);
            multiPlayer.setTextColor(Color.WHITE);
        }

        // Validate selected image
        // if it is the default image
        if (image.getDrawable() != null &&
                image.getDrawable().getConstantState() != null &&
                image.getDrawable().getConstantState().equals(
                        Objects.requireNonNull(ContextCompat.getDrawable(context, R.drawable.select_image)).getConstantState())) {
            gameImageText.setError("An image is required.");
            isValid[0] = false;
        } else {
            gameImageText.setError(null);
        }

        // Validate videoURL
        if (!youtubeURLString.isEmpty()) {
            // check if it is a valid youtube address
            Pattern pattern = Pattern.compile(Constants.YOUTUBE_URL_PATTERN);
            Matcher matcher = pattern.matcher(youtubeURLString);
            if (!matcher.matches()) {
                isValid[0] = false;
                youtube_URL.setError("Invalid Youtube video URL.");
            }
        }
        // If synchronous validation fails, complete the task immediately with false
        if (!isValid[0]) {
            taskCompletionSource.setResult(false);
            return taskCompletionSource.getTask();
        }
        // Asynchronous validation (checking game name existence)
        gameRepository.checkGameNameExists(gameName).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult()) { // Game name already exists
                    gameNameInput.setError("Game name already exists.");
                    taskCompletionSource.setResult(false);
                } else {
                    taskCompletionSource.setResult(true);
                }
            } else {
                gameNameInput.setHintTextColor(Color.WHITE);
                taskCompletionSource.setResult(false);
            }
        });
        return taskCompletionSource.getTask();
    }
}
