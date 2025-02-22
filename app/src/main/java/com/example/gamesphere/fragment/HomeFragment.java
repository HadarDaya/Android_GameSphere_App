package com.example.gamesphere.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.gamesphere.R;
import com.example.gamesphere.adapter.GameAdapter;
import com.example.gamesphere.model.Developer;
import com.example.gamesphere.model.Game;
import com.example.gamesphere.model.Genre;
import com.example.gamesphere.model.Platform;
import com.example.gamesphere.model.Publisher;
import com.example.gamesphere.model.Series;
import com.example.gamesphere.repository.GameRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.utils.LoadingHandlerUtils;
import com.example.gamesphere.utils.SelectorsOptionsUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;

/**
 * The class represents Fragment of home page.
 * Corresponding content view: fragment_home.xml
 */
public class HomeFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // UI components
    // ----------------------
    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private LinearLayout sideBarFiltersLayout;
    private TextView genresListText, platformsListText, releaseYearTextView, filterCountText,
            developersListText, publishersListText, seriesListText;
    private Chip singlePlayerChip, multiPlayerChip;
    private MaterialButtonToggleGroup toggleRateButtonGroup;
    private Spinner sortSpinner;
    private MaterialButton sortArrowBtn;
    // for loading animation
    private LottieAnimationView loadingAnimation;
    private FrameLayout loadingContainer;

    // Repositories
    // ----------------------
    private final GameRepository gameRepository = new GameRepository();

    // Logic- selected options
    // ----------------------
    private final ArrayList<Genre> selectedGenres = new ArrayList<>();
    private final ArrayList<Platform> selectedPlatforms = new ArrayList<>();
    private final ArrayList<Developer> selectedDevelopers = new ArrayList<>();
    private final ArrayList<Publisher> selectedPublishers = new ArrayList<>();
    private final ArrayList<Series> selectedSeries = new ArrayList<>();
    private final ArrayList<Integer> selectedYears = new ArrayList<>();
    private final ArrayList<Integer> selectedAvgRatesAbove = new ArrayList<>();
    private String selectedSort = "";
    private boolean isAscending = true;

    private GameAdapter gameAdapter;
    private ArrayList<Game> dataSet;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_home, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(false); // do not allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("GameSphere");

        // ================================
        // get UI elements + handler functions
        // =================================
        recyclerView = viewScreen.findViewById(R.id.homeFrag_recyclerView);
        sortSpinner = viewScreen.findViewById(R.id.homeFrag_sort_sortSpinner);
        sortArrowBtn = viewScreen.findViewById(R.id.homeFrag_sort_btnSortArrow);
        // lottie
        loadingAnimation = viewScreen.findViewById(R.id.homeFrag_loadingAnimation);
        loadingContainer = viewScreen.findViewById(R.id.homeFrag_loadingContainer);
        LoadingHandlerUtils.showLoadingAnimation(loadingContainer, loadingAnimation);
        // search
        searchHandler(viewScreen);
        // filters
        filtersSideBarHandler(viewScreen);
        // sorting
        sortingHandler();

        // ========================
        // dataset
        // =========================
        dataSet = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);  // Set up GridLayoutManager with 1 column
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator()); // the animation to be performed when items in the RecyclerView are added or removed.

        // Adding All Games from database to the dataSet
        gameRepository.fetchAllGames().addOnCompleteListener(task -> { // result is the data of dataset
            if (task.isSuccessful()) {
                LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                ArrayList<Game> dataSet = task.getResult();
                gameAdapter = new GameAdapter(dataSet, "home", null);
                recyclerView = recyclerView.findViewById(R.id.homeFrag_recyclerView);
                recyclerView.setAdapter(gameAdapter);
                gameAdapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed (to update UI)
            } else { // If no games are found, show the Lottie animation and hide RecyclerView
                LoadingHandlerUtils.hideLoadingAnimation(loadingContainer, loadingAnimation);
                CustomAlertDialog.showCustomDialog(getContext(), "Loading games failed: " + task.getException(), CustomAlertDialog.MessageType.ERROR);
            }
        });
        return viewScreen;
    }

    /**
     * The function handles actions regarding to search bar.
     * @param viewScreen view
     */
    private void searchHandler(View viewScreen)
    {
        SearchView searchView = viewScreen.findViewById(R.id.homeFrag_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                // when user pressed "submit" (search)
                applyFiltersAndSearch(viewScreen);
                displayNoDataLottie(viewScreen);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                // while user is typing
                applyFiltersAndSearch(viewScreen);
                displayNoDataLottie(viewScreen);
                return false;
            }
        });
    }

    /**
     * The function handles actions regarding to filtering.
     * @param viewScreen view
     */
    private void filtersSideBarHandler(View viewScreen)
    {
        // ========================
        // get UI elements
        // =========================
        drawerLayout = viewScreen.findViewById(R.id.homeFrag_drawer_layout);
        sideBarFiltersLayout = viewScreen.findViewById(R.id.homeFrag_filter_sidebar);
        developersListText = viewScreen.findViewById(R.id.homeFrag_filter_developer);
        publishersListText = viewScreen.findViewById(R.id.homeFrag_filter_publisher);
        seriesListText = viewScreen.findViewById(R.id.homeFrag_filter_series);
        releaseYearTextView = viewScreen.findViewById(R.id.homeFrag_filter_releaseYear);
        genresListText = viewScreen.findViewById(R.id.homeFrag_filter_genre);
        platformsListText = viewScreen.findViewById(R.id.homeFrag_filter_platform);
        singlePlayerChip = viewScreen.findViewById(R.id.homeFrag_filter_singlePlayer);
        multiPlayerChip = viewScreen.findViewById(R.id.homeFrag_filter_multiPlayer);
        Button applyFiltersBtn = viewScreen.findViewById(R.id.homeFrag_filter_btnApplyFilters);
        ImageButton openFiltersBtn = viewScreen.findViewById(R.id.homeFrag_btnOpenFilters);
        Button clearFiltersBtn = viewScreen.findViewById(R.id.homeFrag_filter_clear);
        filterCountText = viewScreen.findViewById(R.id.homeFrag_filterCountText);
        toggleRateButtonGroup = viewScreen.findViewById(R.id.homeFrag_filter_toggleRateButtonGroup);

        // Fetch and set spinners
        initSelectors();

        // Apply Filters Button Click
        applyFiltersBtn.setOnClickListener(v -> {
            applyFiltersAndSearch(viewScreen);
            displayNoDataLottie(viewScreen);
            drawerLayout.closeDrawer(GravityCompat.START); // Close the sidebar after applying filters
        });

        // Open filter sidebar on button click
        openFiltersBtn.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(sideBarFiltersLayout)) {
                drawerLayout.closeDrawer(sideBarFiltersLayout);
            } else {
                drawerLayout.openDrawer(sideBarFiltersLayout);
            }
        });

        // Clear Filters button click
        clearFiltersBtn.setOnClickListener(v -> clearFilters());

        // Rate toggle button group
        toggleRateButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            MaterialButton selectedButton = viewScreen.findViewById(checkedId);
            String selectedText = selectedButton.getText().toString();
            Integer selectedDigit = Integer.parseInt(selectedText.replaceAll("[^0-9]", ""));
            if (isChecked) {
                if(!selectedAvgRatesAbove.contains(selectedDigit))
                    selectedAvgRatesAbove.add(selectedDigit);
            }
            else
                selectedAvgRatesAbove.remove(selectedDigit);
        });
    }

    /**
     * The function initializes data on select fields on filters sidebar.
     * Calls methods on SelectorsOptionsUtils helper class.
     */
    public void initSelectors() {
        // Set selectors with their relevant data
        SelectorsOptionsUtils.SetDeveloperOptions_MultiSelect(developersListText, selectedDevelopers);
        SelectorsOptionsUtils.SetGenreOptions_MultiSelect(genresListText, selectedGenres);
        SelectorsOptionsUtils.SetPlatformOptions_MultiSelect(platformsListText, selectedPlatforms);
        SelectorsOptionsUtils.SetPublisherOptions_MultiSelect(publishersListText, selectedPublishers);
        SelectorsOptionsUtils.SetSeriesOptions_MultiSelect(seriesListText, selectedSeries);

        // Set up the releaseYear click listener
        releaseYearTextView.setOnClickListener(v -> {
            // Open the showYearPicker when the button is clicked
            SelectorsOptionsUtils.showYearPicker_Multi(releaseYearTextView, selectedYears);
        });
    }

    /**
     * The function handles sorting
     */
    private void sortingHandler()
    {
        // Set the spinner options
        // -----------------------------
        // Define the options manually
        List<String> options = new ArrayList<>();
        options.add("Alphabetical");
        options.add("Rating");
        options.add("Release Year");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_spinner, options);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        // events
        // -----------------------------
        // Set up the sort arrow button click listener
        sortArrowBtn.setOnClickListener(v -> {
            // Toggle sorting order
            isAscending = !isAscending;
            // Change the icon based on the sort order
            if (isAscending) {
                sortArrowBtn.setIconResource(R.drawable.baseline_arrow_upward);  // Ascending order
            } else {
                sortArrowBtn.setIconResource(R.drawable.baseline_arrow_downward); // Descending order
            }
            // Call the sort function
            // If the gameAdapter isn't ready yet, post the action once it's initialized
            new Handler(Looper.getMainLooper()).post(() -> {
                if (gameAdapter != null) {
                    gameAdapter.sortDataSet(selectedSort, isAscending);
                }
            });
        });

        // Set up selection listener of selecting sorting option
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = parent.getItemAtPosition(position).toString(); // "Alphabetical, ..."
                // Call the sort function
                // If the gameAdapter isn't ready yet, post the action once it's initialized
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (gameAdapter != null) {
                        gameAdapter.sortDataSet(selectedSort, isAscending);
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    /**
     * The function displays "no data" lottie (in case that the recycle view is empty)
     * @param viewScreen view
     */
    private void displayNoDataLottie(View viewScreen)
    {
        LottieAnimationView lottieAnimationView = viewScreen.findViewById(R.id.homeFrag_noDataAnimation);
        if(gameAdapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            lottieAnimationView.setVisibility(View.VISIBLE);
        }
        else
        {
            recyclerView.setVisibility(View.VISIBLE);
            lottieAnimationView.setVisibility(View.GONE);
        }
    }

    /**
     * The function applies filters and changes dataset (view + logic)
     * is called as a result of 3 actions:
     * 1. typing something on search
     * 2. submitting something on search
     * 3. clicking on "Apply Filters"
     * @param viewScreen view
     */
    private void applyFiltersAndSearch(View viewScreen)
    {
        // filters
        // ----------------------
        // create lists of IDs (necessary for filterData function)
        ArrayList<Long> selectedGenreIds = new ArrayList<>();
        ArrayList<Long> selectedPlatformIds = new ArrayList<>();
        ArrayList<Long> selectedDeveloperIds = new ArrayList<>();
        ArrayList<Long> selectedPublisherIds = new ArrayList<>();
        ArrayList<Long> selectedSeriesIds = new ArrayList<>();

        for (Genre genre : selectedGenres) {
            selectedGenreIds.add(genre.getId());
        }
        for (Platform platform : selectedPlatforms) {
            selectedPlatformIds.add(platform.getId());
        }
        for (Developer developer : selectedDevelopers) {
            selectedDeveloperIds.add(developer.getId());
        }
        for (Publisher publisher : selectedPublishers) {
            selectedPublisherIds.add(publisher.getId());
        }
        for (Series series : selectedSeries) {
            selectedSeriesIds.add(series.getId());
        }

        boolean isSinglePlayer = singlePlayerChip.isChecked();
        boolean isMultiPlayer = multiPlayerChip.isChecked();

        // search
        // ----------------------
        SearchView searchView = viewScreen.findViewById(R.id.homeFrag_search);
        String searchText = searchView.getQuery().toString();

        // change dataset
        // ----------------------
        // the function sets dataset (depending on all filters and search)
        gameAdapter.filterData(searchText, selectedGenreIds, selectedYears, selectedPlatformIds,
                selectedDeveloperIds, selectedPublisherIds, selectedSeriesIds, selectedAvgRatesAbove,
                isSinglePlayer, isMultiPlayer);

        // update active filters count
        displayFilterCount();
    }

    /**
     * The function counts number of active filters, and updates filterCountText view.
     */
    private void displayFilterCount()
    {
        int countActiveFilters = 0; // for displaying counter on filter button

        // user selected at least one item from them
        if (!selectedGenres.isEmpty())
            countActiveFilters++;
        if (!selectedPlatforms.isEmpty())
            countActiveFilters++;
        if (!selectedDevelopers.isEmpty())
            countActiveFilters++;
        if (!selectedPublishers.isEmpty())
            countActiveFilters++;
        if (!selectedSeries.isEmpty())
            countActiveFilters++;
        if(!selectedAvgRatesAbove.isEmpty())
            countActiveFilters++;

        if (singlePlayerChip.isChecked())
            countActiveFilters++;
        if (multiPlayerChip.isChecked())
            countActiveFilters++;

        filterCountText.setText(String.valueOf(countActiveFilters));
    }

    /**
     * The function clears form fields.
     */
    @SuppressLint("SetTextI18n")
    public void clearFilters()
    {
        // Clear selected fields
        genresListText.setText("Select");
        platformsListText.setText("Select");
        releaseYearTextView.setText("Select");
        developersListText.setText("Select");
        publishersListText.setText("Select");
        seriesListText.setText("Select");

        // check checkboxes (so that will display both single and multi by default)
        singlePlayerChip.setChecked(true);
        multiPlayerChip.setChecked(true);

        toggleRateButtonGroup.clearChecked();

        // reset selected logic
        selectedGenres.clear();
        selectedPlatforms.clear();
        selectedDevelopers.clear();
        selectedPublishers.clear();
        selectedSeries.clear();
        selectedYears.clear();
        selectedAvgRatesAbove.clear();
    }

}