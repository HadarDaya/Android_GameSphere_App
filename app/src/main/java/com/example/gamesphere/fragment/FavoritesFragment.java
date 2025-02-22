package com.example.gamesphere.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.example.gamesphere.R;
import com.example.gamesphere.adapter.GameAdapter;
import com.example.gamesphere.model.Game;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.airbnb.lottie.LottieAnimationView;
import com.example.gamesphere.repository.UserFavoritesRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.utils.LoadingHandlerUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The class represents Fragment for user favorites.
 * Corresponding content view: fragment_favorites.xml
 */
public class FavoritesFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // UI components
    // ----------------------
    private RecyclerView recyclerView;
    // for loading animation
    private FrameLayout loadingContainer;
    private LottieAnimationView loadingAnimation;

    // Repositories
    // ----------------------
    private final UserFavoritesRepository userFavoritesRepository = new UserFavoritesRepository();
    private final AuthenticationRepository authRepository = new AuthenticationRepository();

    private ArrayList<Game> dataSet;
    private GameAdapter gameAdapter;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoritesFragment.
     */
    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_favorites, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(false); // do not allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("My Favorites");

        // ========================
        // logged in user
        // =========================
        FirebaseUser firebaseUser = authRepository.getLoggedInUser();
        String userID;
        userID = (firebaseUser != null)? firebaseUser.getUid() : "";

        // ========================
        // get UI elements
        // =========================
        recyclerView = viewScreen.findViewById(R.id.favoritesFrag_recyclerView); // initialization to recyclerView
        // lottie
        LottieAnimationView lottieNoDataAnimationView = viewScreen.findViewById(R.id.favoritesFrag_noDataAnimation);
        loadingAnimation = viewScreen.findViewById(R.id.favoritesFrag_loadingAnimation);
        loadingContainer = viewScreen.findViewById(R.id.favoritesFrag_loadingContainer);
        LoadingHandlerUtils.showLoadingAnimation(loadingContainer, loadingAnimation);

        // ========================
        // dataset
        // =========================
        dataSet = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1); // Set up GridLayoutManager with 1 column
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // the animation to be performed when items in the RecyclerView are added or removed.

        // Adding User's favorite Games from database to the dataSet
        // (adapter will be similar to home fragment adapter)
        userFavoritesRepository.fetchAllUserFavoriteGames(userID).addOnCompleteListener(task -> { // result is the data of dataset
                if (task.isSuccessful()) {
                    LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                    ArrayList<Game> dataSet = task.getResult();
                    if (dataSet.isEmpty()) {    // If no games are found, show the Lottie animation and hide RecyclerView
                        recyclerView.setVisibility(View.GONE);
                        lottieNoDataAnimationView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        lottieNoDataAnimationView.setVisibility(View.GONE);
                        // init adapter with the dataset (with data)
                        gameAdapter = new GameAdapter(dataSet, "favorites", () ->
                        {
                            // what should be executed if GameAdapter informed that list has become empty
                            recyclerView.setVisibility(View.GONE);
                            lottieNoDataAnimationView.setVisibility(View.VISIBLE);
                        });
                        recyclerView = recyclerView.findViewById(R.id.favoritesFrag_recyclerView);
                        recyclerView.setAdapter(gameAdapter);
                        gameAdapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed (to update UI)
                    }
                } else { // failed fetching favorites
                    LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                    CustomAlertDialog.showCustomDialog(getContext(), "Loading favorites Failed: " + task.getException(), CustomAlertDialog.MessageType.ERROR);
                }
            });

        // Set up RecyclerView scroll listener to detect changes in scrollability
        // since user can delete items from view
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkAndToggleBottomNavigation(); // Check scrollability on scroll
            }
        });

        return viewScreen;
    }

    /**
     * The function checks if the RecyclerView content is scrollable,
     * and toggles the visibility of BottomNavigationView.
     * (to avoid a bug when the user has scrollbar, hide it and then deletes items from view,
     * which made the menu no longer appear)
     */
    public void checkAndToggleBottomNavigation() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.my_menu);
        if (bottomNavigationView != null && recyclerView != null) {
            boolean canScroll = recyclerView.canScrollVertically(1); // Check if RecyclerView can scroll vertically
            if (canScroll) {
                bottomNavigationView.setVisibility(View.GONE); // Content is scrollable, hide BottomNavigationView
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE); // Content is not scrollable, show BottomNavigationView
            }
        }
    }
}