package com.example.gamesphere.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.gamesphere.R;
import com.example.gamesphere.model.Developer;
import com.example.gamesphere.model.Genre;
import com.example.gamesphere.model.Platform;
import com.example.gamesphere.model.Publisher;
import com.example.gamesphere.model.Series;
import com.example.gamesphere.repository.DeveloperRepository;
import com.example.gamesphere.repository.GenreRepository;
import com.example.gamesphere.repository.PlatformRepository;
import com.example.gamesphere.repository.PublisherRepository;
import com.example.gamesphere.repository.SeriesRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

// Set Options to each spinner/ multi-select dropdown
/**
 * Helper class to Set Options to different selector types, and display the selected strings
 * in UI components.
 */
public class SelectorsOptionsUtils {

    // Repository instances for fetching data from Firebase
    private static final DeveloperRepository developerRepository = new DeveloperRepository();
    private static final GenreRepository genreRepository = new GenreRepository();
    private static final PlatformRepository platformRepository = new PlatformRepository();
    private static final PublisherRepository publisherRepository = new PublisherRepository();
    private static final SeriesRepository seriesRepository = new SeriesRepository();

    /**
     * Set options (view) for the given developer spinner. (single select)
     * @param developerSpinner  view to set options in.
     */
    public static void SetDeveloperOptions_SingleSelect(Spinner developerSpinner) {
        developerRepository.fetchAllDevelopers(developers -> {
            // Add "Select" as the first option
            developers.add(0, new Developer(0, "Select"));

            ArrayAdapter<Developer> adapter = new ArrayAdapter<>(developerSpinner.getContext(),
                    R.layout.custom_spinner, developers);
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
            developerSpinner.setAdapter(adapter);
        });
    }

    /**
     * The function calls a method which sets the alert dialog for the given developer view. (multi select)
     * @param developersListText  clicking on it will display alertDialog
     * @param selectedDevelopers    previously selected developers
     */
    public static void SetDeveloperOptions_MultiSelect(TextView developersListText, ArrayList<Developer> selectedDevelopers) {
        developerRepository.fetchAllDevelopers(developers -> {
            if (developers == null || developers.isEmpty()) return;

            // Set a click listener to open the multi-select dialog
            developersListText.setOnClickListener(v ->
                    showMultiSelectDialog(developers, selectedDevelopers, developersListText,
                            "Select Developer(s)", Developer::getName)
            );
        });
    }

    /**
     * The function calls a method which sets the alert dialog for the given genre view. (multi select)
     * @param genresListText  clicking on it will display alertDialog
     * @param selectedGenres    previously selected genres
     */
    public static void SetGenreOptions_MultiSelect(TextView genresListText, ArrayList<Genre> selectedGenres) {
        genreRepository.fetchAllGenres(genres -> {
            if (genres == null || genres.isEmpty()) return;

            // Set a click listener to open the multi-select dialog
            genresListText.setOnClickListener(v ->
                    showMultiSelectDialog(genres, selectedGenres, genresListText,
                            "Select Genre(s)", Genre::getName)
            );
        });
    }

    /**
     * The function calls a method which sets the alert dialog for the given platform view. (multi select)
     * @param platformsListText  clicking on it will display alertDialog
     * @param selectedPlatforms    previously selected platforms
     */
    public static void SetPlatformOptions_MultiSelect(TextView platformsListText, ArrayList<Platform> selectedPlatforms) {
        platformRepository.fetchAllPlatforms(platforms -> {
            if (platforms == null || platforms.isEmpty()) return;

            // Set a click listener to open the multi-select dialog
            platformsListText.setOnClickListener(v ->
                    showMultiSelectDialog(platforms, selectedPlatforms, platformsListText,
                            "Select Platform(s)", Platform::getName)
            );
        });
    }

    /**
     * Set options (view) for the given publisher spinner. (single select)
     * @param publisherSpinner  view to set options in.
     */
    public static void SetPublisherOptions_SingleSelect(Spinner publisherSpinner) {
        publisherRepository.fetchAllPublishers(publishers -> {
            // Add "Select" as the first option
            publishers.add(0, new Publisher(0, "Select"));

            ArrayAdapter<Publisher> adapter = new ArrayAdapter<>(publisherSpinner.getContext(),
                    R.layout.custom_spinner, publishers);
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
            publisherSpinner.setAdapter(adapter);
        });
    }

    /**
     * The function calls a method which sets the alert dialog for the given publisher view. (multi select)
     * @param publishersListText  clicking on it will display alertDialog
     * @param selectedPublishers    previously selected publishers
     */
    public static void SetPublisherOptions_MultiSelect(TextView publishersListText, ArrayList<Publisher> selectedPublishers) {
        publisherRepository.fetchAllPublishers(publishers -> {
            if (publishers == null || publishers.isEmpty()) return;

            // Set a click listener to open the multi-select dialog
            publishersListText.setOnClickListener(v ->
                    showMultiSelectDialog(publishers, selectedPublishers, publishersListText,
                            "Select Publisher(s)", Publisher::getName)
            );
        });
    }

    /**
     * Set options (view) for the given series spinner. (single select)
     * @param seriesSpinner  view to set options in.
     */
    public static void SetSeriesOptions_SingleSelect(Spinner seriesSpinner) {
        seriesRepository.fetchAllSeries(series -> {
            // Add "Select" as the first option
            series.add(0, new Series(0, "Select"));

            ArrayAdapter<Series> adapter = new ArrayAdapter<>(seriesSpinner.getContext(),
                    R.layout.custom_spinner, series);
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
            seriesSpinner.setAdapter(adapter);
        });
    }

    /**
     * The function calls a method which sets the alert dialog for the given series view. (multi select)
     * @param seriesListText  clicking on it will display alertDialog
     * @param selectedSeries    previously selected series
     */
    public static void SetSeriesOptions_MultiSelect(TextView seriesListText, ArrayList<Series> selectedSeries) {
        seriesRepository.fetchAllSeries(series -> {
            if (series == null || series.isEmpty()) return;

            // Set a click listener to open the multi-select dialog
            seriesListText.setOnClickListener(v ->
                    showMultiSelectDialog(series, selectedSeries, seriesListText,
                            "Select Series", Series::getName)
            );
        });
    }

    /**
     * The function sets year picker. (single select)
     * @param releaseYearTextView   will display the selected year
     */
    public static void showYearPicker_Single(TextView releaseYearTextView)
    {
        // Create a NumberPicker for selecting the year
        NumberPicker yearPicker = new NumberPicker(releaseYearTextView.getContext());

        // Set a range for the years
        yearPicker.setMinValue(2010);
        yearPicker.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));

        // Set the current year as the default value
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setValue(currentYear);

        yearPicker.setBackgroundColor(Color.BLACK);

        // Create a dialog to display the NumberPicker
        AlertDialog.Builder builder = new AlertDialog.Builder(releaseYearTextView.getContext());
        builder.setView(yearPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = yearPicker.getValue(); // Get the previously selected year
                    releaseYearTextView.setText(String.valueOf(selectedYear)); // Display it in the TextView
                })
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        dialog.show();

        // ** Change buttons text color to white **
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    /**
     * The function calls a method which sets the alert dialog for the given year view. (multi select)
     * @param releaseYearTextView  clicking on it will display alertDialog
     * @param selectedYears         previously selected years
     */
    public static void showYearPicker_Multi(TextView releaseYearTextView, ArrayList<Integer> selectedYears) {
        // Define the range of years (2010 to current year)
        int minYear = 2010;
        int maxYear = Calendar.getInstance().get(Calendar.YEAR);

        // Create a list of all years in the range
        List<Integer> allYears = new ArrayList<>();
        for (int i = minYear; i <= maxYear; i++) {
            allYears.add(i);
        }

        // Call showMultiSelectDialog to handle year selection
        showMultiSelectDialog(allYears, selectedYears, releaseYearTextView,
                "Select Year(s)", String::valueOf
        );
    }

    /**
     * Generic function to display a Multi-Select AlertDialog.
     * (including display of previously selected options)
     * @param allItems          all option items
     * @param selectedItems     items selected previously by user
     * @param displayTextView   the selected items will be displayed in this component (separated by comma)
     * @param title             string to display on dialog's title
     * @param getNameFunction   getName() function for the collection.
     */
    @SuppressLint("SetTextI18n")
    private static <T> void showMultiSelectDialog(List<T> allItems, ArrayList<T> selectedItems,
                                                  TextView displayTextView, String title,
                                                  Function<T, String> getNameFunction)
    {
        // Get context and inflate custom dialog layout
        Context context = displayTextView.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_multi_choice_dialog, null);

        // Initialize Dialog's UI components
        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        ListView listView = dialogView.findViewById(R.id.listView);
        Button clearBtn = dialogView.findViewById(R.id.btnClear);
        Button okBtn = dialogView.findViewById(R.id.btnOk);

        // Set dialog title
        titleView.setText(title);

        // -----------------------------------
        // display previously selected options
        // -----------------------------------
        String[] itemNames = new String[allItems.size()];
        boolean[] selectedItemsArray = new boolean[allItems.size()];
        for (int i = 0; i < allItems.size(); i++) {
            itemNames[i] = getNameFunction.apply(allItems.get(i));
            selectedItemsArray[i] = selectedItems.contains(allItems.get(i)); // change "V" logic
        }
        // Set up the adapter for the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, itemNames);
        listView.setAdapter(adapter);

        for (int i = 0; i < allItems.size(); i++) {
            listView.setItemChecked(i, selectedItemsArray[i]);  // change "V" display
        }

        // Create and configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Adjust dialog size (make it smaller)
        dialog.setOnShowListener(dialogInterface -> {
            int height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.5); // 50% of screen height
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        });

        // Handle selection changes in the ListView
        listView.setOnItemClickListener((parent, view, position, id) -> {
            T selectedItem = allItems.get(position);
            if (selectedItems.contains(selectedItem)) {
                selectedItems.remove(selectedItem);
            } else {
                selectedItems.add(selectedItem);
            }
        });

        // Handle "OK" button click
        okBtn.setOnClickListener(v -> {
            if (selectedItems.isEmpty()) {
                displayTextView.setText("Select");
            } else {
                displayTextView.setText(selectedItems.stream()
                        .map(getNameFunction)
                        .collect(Collectors.joining(", ")));
            }
            dialog.dismiss();
        });

        // Handle "Clear" button click
        clearBtn.setOnClickListener(v -> {
            selectedItems.clear();
            displayTextView.setText("Select");
            for (int i = 0; i < listView.getCount(); i++) {
                listView.setItemChecked(i, false);
            }
        });
        // Show the dialog
        dialog.show();
    }

}
