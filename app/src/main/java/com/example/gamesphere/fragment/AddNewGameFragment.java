package com.example.gamesphere.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.gamesphere.R;
import com.example.gamesphere.model.Developer;
import com.example.gamesphere.model.Game;
import com.example.gamesphere.model.Genre;
import com.example.gamesphere.model.Platform;
import com.example.gamesphere.model.Publisher;
import com.example.gamesphere.model.Series;
import com.example.gamesphere.repository.GameRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.utils.ImageUtils;
import com.example.gamesphere.utils.LoadingHandlerUtils;
import com.example.gamesphere.utils.SelectorsOptionsUtils;
import com.example.gamesphere.validation.ValidateAddGame;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The class represents Fragment for adding new game. (available for admin only)
 * Corresponding content view: fragment_add_new_game.xml
 */
public class AddNewGameFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // UI components
    // ----------------------
    private EditText gameNameEditText, descriptionEditText, youtube_URL;
    private Spinner developerSpinner, publisherSpinner, seriesSpinner;
    private TextView genresListText, platformsListText, releaseYearTextView, gameImageText;
    private ImageView imageView;
    private Uri selectedImageUri;
    private Chip singlePlayer, multiPlayer;
    // for loading animation
    private FrameLayout loadingContainer;
    private LottieAnimationView loadingAnimation;

    // Repositories
    // ----------------------
    private final GameRepository gameRepository = new GameRepository();

    // Logic- selected options
    // ----------------------
    ArrayList<Genre> selectedGenres = new ArrayList<>();
    ArrayList<Platform> selectedPlatforms = new ArrayList<>();

    // Helpers
    // ----------------------
    ImageUtils imageUtils = new ImageUtils();

    public AddNewGameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InsertGameFragment.
     */
    public AddNewGameFragment newInstance(String param1, String param2) {
        AddNewGameFragment fragment = new AddNewGameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_add_new_game, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(true); // allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("Add New Game");

        // ========================
        // get UI elements
        // =========================
        loadingAnimation = viewScreen.findViewById(R.id.addGameFrag_loadingAnimation);
        loadingContainer = viewScreen.findViewById(R.id.addGameFrag_loadingContainer);
        LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);

        gameNameEditText = viewScreen.findViewById(R.id.addGameFrag_gameName);
        descriptionEditText = viewScreen.findViewById(R.id.addGameFrag_description);
        developerSpinner = viewScreen.findViewById(R.id.addGameFrag_developer);
        genresListText = viewScreen.findViewById(R.id.addGameFrag_genre);
        platformsListText = viewScreen.findViewById(R.id.addGameFrag_platform);
        publisherSpinner = viewScreen.findViewById(R.id.addGameFrag_publisher);
        seriesSpinner = viewScreen.findViewById(R.id.addGameFrag_series);
        releaseYearTextView = viewScreen.findViewById(R.id.addGameFrag_releaseYear);
        imageView = viewScreen.findViewById(R.id.addGameFrag_selectedImage);
        gameImageText = viewScreen.findViewById(R.id.addGameFrag_gameImageText);
        youtube_URL = viewScreen.findViewById(R.id.addGameFrag_youtubeUrl);
        singlePlayer = viewScreen.findViewById(R.id.addGameFrag_singlePlayer);
        multiPlayer = viewScreen.findViewById(R.id.addGameFrag_multiPlayer);

        Button submitButton, clearButton;
        submitButton = viewScreen.findViewById(R.id.addGameFrag_submit);
        clearButton = viewScreen.findViewById(R.id.addGameFrag_clear);

        // ========================
        // initialization
        // =========================
        initSelectors();    // Fetch and set spinners
        initPhotoPicker();   // give access to device's photo gallery

        // ========================
        // events
        // =========================
        // Set click listener for submit button
        submitButton.setOnClickListener(v -> submitHandler());
        // Set click listener for clear button
        clearButton.setOnClickListener(v -> clearForm());

        return viewScreen;
    }

    /**
     * The function initializes data on select fields on the form.
     * Calls methods on SelectorsOptionsUtils helper class.
     */
    public void initSelectors()
    {
        // Set selectors with their relevant data
        SelectorsOptionsUtils.SetDeveloperOptions_SingleSelect(developerSpinner);
        SelectorsOptionsUtils.SetGenreOptions_MultiSelect(genresListText, selectedGenres);
        SelectorsOptionsUtils.SetPlatformOptions_MultiSelect(platformsListText, selectedPlatforms);
        SelectorsOptionsUtils.SetPublisherOptions_SingleSelect(publisherSpinner);
        SelectorsOptionsUtils.SetSeriesOptions_SingleSelect(seriesSpinner);

        // Set up the releaseYear click listener
        releaseYearTextView.setOnClickListener(v -> {
            // Open the showYearPicker when the button is clicked
            SelectorsOptionsUtils.showYearPicker_Single(releaseYearTextView);
        });
    }

    /**
     * The function gives access to device's photo picker (to select a picture from gallery)
     */
    private void initPhotoPicker() {
        // Registers a photo picker activity launcher in single-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->
                {
                    // Callback is invoked after the user selects a media item or closes the photo picker.
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(requireContext()) // Use Glide to load the image asynchronously
                                .load(uri)
                                .into(imageView); // Set the selected image as a preview
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        // Open Photo Picker when clicking the ImageView
        imageView.setOnClickListener(v -> pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        ));
    }

    /**
     * The function handles actions as a result of submitting the form:
     * Validates, creates new game, inserts the new game.
     * Displays messages to the screen.
     */
    private void submitHandler() {
        ValidateAddGame.validateAddGameForm(gameNameEditText, developerSpinner, genresListText, platformsListText,
                publisherSpinner, descriptionEditText, releaseYearTextView, imageView, gameImageText, youtube_URL, singlePlayer, multiPlayer,
                getContext()).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {

                // Form validation passed, now start the loading animation
                LoadingHandlerUtils.showLoadingAnimation(loadingContainer, loadingAnimation);

                // Get the values from view
                String name = gameNameEditText.getText().toString().trim();
                Integer releaseYear = Integer.valueOf(releaseYearTextView.getText().toString());
                String description = descriptionEditText.getText().toString().trim();
                String videoURL = youtube_URL.getText().toString().trim();

                Long seriesID = ((Series) (seriesSpinner.getSelectedItem())).getId();
                Long developerID = ((Developer) (developerSpinner.getSelectedItem())).getId();
                Long publisherID = ((Publisher) (publisherSpinner.getSelectedItem())).getId();
                boolean isSinglePlayer = singlePlayer.isChecked();
                boolean isMultiPlayer = multiPlayer.isChecked();

                // Create an array of platformIDs
                ArrayList<Long> platformIds = new ArrayList<>();
                for (Platform platform : selectedPlatforms) {
                    platformIds.add(platform.getId());
                }

                // Create an array of genreIDs
                ArrayList<Long> genresIds = new ArrayList<>();
                for (Genre genre : selectedGenres) {
                    genresIds.add(genre.getId());
                }

                // Upload image to Cloudinary (location: Media Library -> GameSphere_Games)
                imageUtils.uploadImage(selectedImageUri, requireContext(), new ImageUtils.OnImageUploadCallback() {
                    @Override
                    public void onSuccess(String cloudinaryImageUrl) {
                        // Image upload was successful, now insert game into the repository
                        requireActivity().runOnUiThread(() -> {
                            LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);

                            // Create a new Game instance
                            Game newGame = new Game(null, name, releaseYear, description, cloudinaryImageUrl,
                                    videoURL, platformIds, seriesID, developerID, publisherID, genresIds,
                                    isSinglePlayer, isMultiPlayer, 0, 0);

                            // Insert the new game into the repository
                            gameRepository.insertGame(newGame, new GameRepository.OnGameAddedListener() {
                                @Override
                                public void onSuccess(String gameId) {
                                    LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                                    CustomAlertDialog.showCustomDialog(getContext(), "Game '" + newGame.getName() + "' was added successfully!", CustomAlertDialog.MessageType.SUCCESS);
                                    clearForm();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                                    CustomAlertDialog.showCustomDialog(getContext(), "Game insertion failed: " + e, CustomAlertDialog.MessageType.ERROR);
                                }
                            });
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                            CustomAlertDialog.showCustomDialog(getContext(), "Image upload failed: " + e, CustomAlertDialog.MessageType.ERROR);
                        });
                    }
                });

            } else {
                // If validation fails, show an error or notify the user
                CustomAlertDialog.showCustomDialog(getContext(), "Please fix the validation errors and try again.", CustomAlertDialog.MessageType.ERROR);
            }
        });
    }

    /**
     * The function clears form fields.
     */
    @SuppressLint("SetTextI18n")
    public void clearForm()
    {
        // Clear text inputs
        gameNameEditText.setText("");
        descriptionEditText.setText("");
        youtube_URL.setText("");

        // Reset spinners to default position (first item)
        developerSpinner.setSelection(0);
        publisherSpinner.setSelection(0);
        seriesSpinner.setSelection(0);

        // Clear selected fields
        genresListText.setText("Select");
        platformsListText.setText("Select");
        releaseYearTextView.setText("Select");

        // Clear image selection (reset to default)
        imageView.setImageResource(R.drawable.select_image);
        imageView.setTag("default"); // Use tag to track default image

        // Uncheck checkboxes
        singlePlayer.setChecked(false);
        multiPlayer.setChecked(false);

        // Clear errors
        youtube_URL.setError(null);
        gameImageText.setError(null);
        gameNameEditText.setError(null);

        // Turn color to default
        TextView developerTextView = (TextView) developerSpinner.getSelectedView();
        developerTextView.setTextColor(Color.WHITE);
        TextView publisherTextView = (TextView) publisherSpinner.getSelectedView();
        publisherTextView.setTextColor(Color.WHITE);
        gameNameEditText.setHintTextColor(Color.GRAY);
        singlePlayer.setTextColor(Color.WHITE);
        multiPlayer.setTextColor(Color.WHITE);
        descriptionEditText.setHintTextColor(Color.GRAY);
        platformsListText.setTextColor(Color.WHITE);
        releaseYearTextView.setTextColor(Color.WHITE);
        genresListText.setTextColor(Color.WHITE);

        // Reset selected image URI
        selectedImageUri = null;

        // Reset selected logic
        selectedGenres.clear();
        selectedPlatforms.clear();
    }

}