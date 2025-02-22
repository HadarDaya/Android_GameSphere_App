package com.example.gamesphere.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.gamesphere.R;
import com.example.gamesphere.model.Developer;
import com.example.gamesphere.model.Game;
import com.example.gamesphere.model.Genre;
import com.example.gamesphere.model.Platform;
import com.example.gamesphere.model.Publisher;
import com.example.gamesphere.model.Series;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.repository.DeveloperRepository;
import com.example.gamesphere.repository.GenreRepository;
import com.example.gamesphere.repository.PlatformRepository;
import com.example.gamesphere.repository.PublisherRepository;
import com.example.gamesphere.repository.SeriesRepository;
import com.example.gamesphere.repository.UserFavoritesRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.utils.ImageUtils;
import com.example.gamesphere.utils.RatingHandlerUtils;
import com.example.gamesphere.utils.YoutubePlayerUtils;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseUser;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The class represents Fragment of specific game.
 * Corresponding content view: fragment_game_details.xml
 */
public class GameDetailsFragment extends Fragment
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // UI components
    // ----------------------
    private ImageView imageView, singlePlayerView, multiPlayerView;
    private TextView titleView, releaseYearView, avgRateView, rateView, descriptionView, developerView,
                    platformView, publisherView, seriesView, videoNotExistMsgView ;
    private ImageButton favoriteBtnView;
    private YouTubePlayerView youTubePlayerView;

    // Repositories
    // ----------------------
    private final AuthenticationRepository authRepository = new AuthenticationRepository();
    private final UserFavoritesRepository userFavoritesRepository = new UserFavoritesRepository();
    private final DeveloperRepository developerRepository = new DeveloperRepository();
    private final PublisherRepository publisherRepository = new PublisherRepository();
    private final SeriesRepository seriesRepository = new SeriesRepository();
    private final PlatformRepository platformRepository = new PlatformRepository();
    private final GenreRepository genreRepository = new GenreRepository();

    private Game gameItem = null; // received from bundle on navigation

    public GameDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GameFragment.
     */
    public static GameDetailsFragment newInstance(String param1, String param2) {
        GameDetailsFragment fragment = new GameDetailsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_game_details, container, false);

        // ========================
        // logged in user
        // =========================
        FirebaseUser firebaseUser = authRepository.getLoggedInUser();
        String userID;
        userID = (firebaseUser != null)? firebaseUser.getUid() : "";

        // ========================
        // selected game from bundle
        // =========================
        // get game selected by user (passed as bundle from HomeFragment)
        if (getArguments() != null) {
            gameItem = (Game) getArguments().getSerializable("gameItem");
        }

        // ========================
        // Top Toolbar
        // =========================
        if (gameItem != null)
        {
            ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
            toolbar.setDisplayHomeAsUpEnabled(true); // do not allow "back" on Top Toolbar from this fragment
            toolbar.setTitle(gameItem.getName());
        }

        // ================================
        // get UI elements
        // =================================
        imageView = viewScreen.findViewById(R.id.gameFrag_image);
        titleView = viewScreen.findViewById(R.id.gameFrag_title);
        singlePlayerView = viewScreen.findViewById(R.id.gameFrag_singlePlayerIcon);
        multiPlayerView = viewScreen.findViewById(R.id.gameFrag_multiPlayerIcon);
        releaseYearView = viewScreen.findViewById(R.id.gameFrag_releaseYear);
        avgRateView = viewScreen.findViewById(R.id.gameFrag_avgRate);
        rateView = viewScreen.findViewById(R.id.gameFrag_rateBtn);
        descriptionView = viewScreen.findViewById(R.id.gameFrag_description);
        developerView = viewScreen.findViewById(R.id.gameFrag_developer);
        platformView = viewScreen.findViewById(R.id.gameFrag_platform);
        publisherView = viewScreen.findViewById(R.id.gameFrag_publisher);
        seriesView = viewScreen.findViewById(R.id.gameFrag_series);
        youTubePlayerView = viewScreen.findViewById(R.id.gameFrag_youtubePlayer);
        favoriteBtnView = viewScreen.findViewById(R.id.gameFrag_favoriteBtn);
        videoNotExistMsgView = viewScreen.findViewById(R.id.gameFrag_videoNotExistMsg);
        // rate
        handleRate(userID);
        // favorite
        handleFavorite(userID);

        // make sure that the scrollView starts from [0,0]
        ScrollView scrollView = viewScreen.findViewById(R.id.gameFrag_scrollView);
        scrollView.post(() -> scrollView.scrollTo(0, 0));

        // ================================
        // init game details (display)
        // =================================
        initGameDetails(viewScreen);

        return viewScreen;
    }

    /**
     * The function initializes game information.
     * @param viewScreen view
     */
    private void initGameDetails(View viewScreen)
    {
        // Set the image
        String imageURL = gameItem.getImageURL();
        ImageUtils imageUtils = new ImageUtils();
        imageUtils.loadImageIntoView(imageURL, imageView, imageView.getContext());

        // Set single player visibility
        if(gameItem.isSinglePlayer())
            singlePlayerView.setVisibility(View.VISIBLE);
        else
            singlePlayerView.setVisibility(View.GONE);

        // Set multi player visibility
        if(gameItem.isMultiPlayer())
            multiPlayerView.setVisibility(View.VISIBLE);
        else
            multiPlayerView.setVisibility(View.GONE);

        titleView.setText(gameItem.getName());
        releaseYearView.setText(String.valueOf(gameItem.getReleaseYear()));
        @SuppressLint("DefaultLocale") String roundAvgRate = String.format("%.1f", gameItem.getAvgRate());
        avgRateView.setText(roundAvgRate);
        descriptionView.setText(gameItem.getDescription());

        developerRepository.fetchDeveloperById(gameItem.getDeveloperID(), new DeveloperRepository.DeveloperCallback() {
            @Override
            public void onSuccess(Developer developer) {
                developerView.setText(developer.getName());
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.e("initGameDetails", "Error: " + errorMessage);
            }
        });
        publisherRepository.fetchPublisherById(gameItem.getPublisherID(), new PublisherRepository.PublisherCallback() {
            @Override
            public void onSuccess(Publisher publisher) {
                publisherView.setText(publisher.getName());
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.e("initGameDetails", "Error: " + errorMessage);
            }
        });
        seriesRepository.fetchSeriesById(gameItem.getSeriesID(), new SeriesRepository.SeriesCallback() {
            @Override
            public void onSuccess(Series series) {
                seriesView.setText(series.getName());
            }
            @Override
            public void onFailure(String errorMessage) {
                Log.e("initGameDetails", "Error: " + errorMessage);
            }
        });
        platformRepository.fetchPlatformsByIDs(gameItem.getPlatformIds()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Platform> platforms = task.getResult();
                String platformNames = platforms.stream()
                        .map(Platform::getName)
                        .collect(Collectors.joining(", "));
                platformView.setText(platformNames);
            } else {
                Log.e("initGameDetails", "Error: ", task.getException());
            }
        });
        genreRepository.fetchGenresByIDs(gameItem.getGenreIds()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Genre> genres = task.getResult();
                displayGenres(genres, viewScreen);

            } else {
                Log.e("initGameDetails", "Error: ", task.getException());
            }
        });

        // Set youtube video or message
        String videoURL = gameItem.getVideoURL();
        if (!videoURL.isEmpty()) {
            videoNotExistMsgView.setVisibility(View.GONE);
            YoutubePlayerUtils.setupYouTubePlayer(getLifecycle(), youTubePlayerView, videoURL);
        }
        else
        {
            videoNotExistMsgView.setVisibility(View.VISIBLE);
            youTubePlayerView.setVisibility(View.GONE);
        }
    }

    /**
     * The function handles game rating
     * @param userID logged in user
     */
    private void handleRate(String userID)
    {
        // Set up the 'rate' button click listener
        rateView.setOnClickListener(v -> {
            if (!userID.isEmpty()) // user is signed in
                RatingHandlerUtils.showRatingPopupAndHandleSubmit(gameItem.getId(), userID, v.getContext(), v,
                        (updatedAvgRate, updatedNumOfRaters) -> {
                            // display the average rate
                            @SuppressLint("DefaultLocale") String newAvgRate = String.format("%.1f", updatedAvgRate);
                            avgRateView.setText(newAvgRate);
                        }
                );
            else
                CustomAlertDialog.showCustomDialog(getContext(), "You need to be signed in to perform this action.", CustomAlertDialog.MessageType.INFO);
        });
    }

    /**
     * The function handles adding/removing game from user's favorites.
     * @param userID logged in user
     */
    private void handleFavorite(String userID)
    {
        // Set up the icon image
        if (!userID.isEmpty()) {
            userFavoritesRepository.checkIfUserFavorites(userID, gameItem.getId(), new UserFavoritesRepository.OnFavoritesCheckCallback() {
                @Override
                public void onSuccess(boolean isFavorite) {
                    if (isFavorite) {
                        favoriteBtnView.setImageResource(R.drawable.baseline_favorite_red); // Change UI to filled heart icon
                    } else {
                        favoriteBtnView.setImageResource(R.drawable.baseline_favorite_border_red); // Change UI to outlined heart icon
                    }
                }
                @Override
                public void onFailure(boolean error) {
                    Log.e("FavoritesCheck", "Failed to check favorites status");
                }
            });
        } else { // user is not signed in
            favoriteBtnView.setImageResource(R.drawable.baseline_favorite_border_red); // UI is outlined heart icon
        }
        // Set up the 'favorite' button click listener
        favoriteBtnView.setOnClickListener(v -> {
            if (!userID.isEmpty()) // user is signed in
            {
                userFavoritesRepository.toggleUserFavorite(userID, gameItem.getId(), new UserFavoritesRepository.OnToggleUserFavoriteCallback() {
                    @Override
                    public void onSuccess(boolean added) {
                        if (added) { // adding to favorites was performed successfully
                            favoriteBtnView.setImageResource(R.drawable.baseline_favorite_red); // change UI to Filled heart
                            CustomAlertDialog.showCustomDialog(getContext(), "Added to favorites!", CustomAlertDialog.MessageType.SUCCESS);
                        } else
                        { // removing from favorites was performed successfully
                            favoriteBtnView.setImageResource(R.drawable.baseline_favorite_border_red); // change UI to Outline heart
                            CustomAlertDialog.showCustomDialog(getContext(), "Removed from favorites.", CustomAlertDialog.MessageType.INFO);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        CustomAlertDialog.showCustomDialog(getContext(), "Favorites update Failed:", CustomAlertDialog.MessageType.ERROR);
                    }
                });
            }
            else // user is not signed in
                CustomAlertDialog.showCustomDialog(getContext(), "You need to be signed in to perform this action.", CustomAlertDialog.MessageType.INFO);
        });
    }

    /**
     * The function creates and displays TextView UI objects per genre.
     * @param genres - list of genres for the current game
     * @param viewScreen view
     */
    private void displayGenres(ArrayList<Genre> genres, View viewScreen) {
        FlexboxLayout genreContainer = viewScreen.findViewById(R.id.gameFrag_genreContainer); // container view
        genreContainer.removeAllViews();

        for (Genre genre : genres) {
            TextView genreTag = new TextView(viewScreen.getContext());
            genreTag.setText(genre.getName());
            genreTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            genreTag.setTextColor(ContextCompat.getColor(viewScreen.getContext(), android.R.color.white));
            genreTag.setPadding(16, 8, 16, 8);
            genreTag.setBackground(ContextCompat.getDrawable(viewScreen.getContext(), R.drawable.genre_tag_bg));

            // Set margins dynamically
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 8, 8, 8); // Space between tags
            genreTag.setLayoutParams(params);

            genreContainer.addView(genreTag);
        }
    }

}