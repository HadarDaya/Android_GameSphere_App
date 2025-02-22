package com.example.gamesphere.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gamesphere.MainActivity;
import com.example.gamesphere.R;
import com.example.gamesphere.fragment.FavoritesFragment;
import com.example.gamesphere.model.Game;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.repository.UserFavoritesRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.utils.ImageUtils;
import com.example.gamesphere.utils.RatingHandlerUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The class responsible for "connecting" the list of Game items to the view.
 * Corresponding content view: custom_row_item_game.xml
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.MyViewHolder>
{
    private ArrayList<Game> dataSet; // Holds the data we want to display in the RecyclerView.
    private ArrayList<Game> dataSetFull; // copy of the original data (all data)
    private String location; // location on the app that the adapter was set for
    private OnFavoritesEmptyListener onFavoritesEmptyListener;

    // Repositories
    // -----------------
    private final AuthenticationRepository authRepository = new AuthenticationRepository();
    private final UserFavoritesRepository userFavoritesRepository = new UserFavoritesRepository();

    /**
     * Constructor for the GameAdapter.
     * @param dataSet       List of Game objects to display in the RecyclerView.
     * @param location      String that represents the location on the app that the adapter was set for.
     *                      locations are: home, favorites. (both consist games recycler view)
     * @param listener  call it when the last item in favorite list is removed.
     *                  if location is "home" -> listener will have value
     *                  otherwise -> listener will be null
     */
    public GameAdapter(ArrayList<Game> dataSet, String location, OnFavoritesEmptyListener listener) {
        this.dataSet = dataSet;
        this.dataSetFull = new ArrayList<>(dataSet);  // keep the original games (For example, if we want to return all characters as soon as the search is deleted)
        this.location = location;
        this.onFavoritesEmptyListener = listener;
    }

    /** The inner class MyViewHolder
       Role: Holds references to the display elements that appear in each row of the RecyclerView.
       When creating a new row (new ViewHolder), it looks for view elements by their id. */
    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        // Repositories
        // ----------------------
        ImageView imageViewGame, rateStarImg, singlePlayerView, multiPlayerView;
        TextView nameView, releaseYearView, avgRateView, descriptionView, rateView, readMoreView;
        ImageButton favoriteBtn;

        /**
         * Constructor for MyViewHolder- Pulls out all the organs that exist inside the card according to id
         * @param itemView  The root view of a single row in the RecyclerView.
         */
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // ========================
            // get UI elements
            // =========================
            imageViewGame = itemView.findViewById(R.id.rowItemGameFrag_image);
            nameView = itemView.findViewById(R.id.rowItemGameFrag_name);
            releaseYearView = itemView.findViewById(R.id.rowItemGameFrag_releaseYear);
            avgRateView = itemView.findViewById(R.id.rowItemGameFrag_avgRate);
            descriptionView = itemView.findViewById(R.id.rowItemGameFrag_description);
            favoriteBtn = itemView.findViewById(R.id.rowItemGameFrag_favoriteBtn);
            rateView = itemView.findViewById(R.id.rowItemGameFrag_rateBtn);
            rateStarImg = itemView.findViewById(R.id.rowItemGameFrag_rateStarImg);
            readMoreView = itemView.findViewById(R.id.rowItemGameFrag_readMore);
            singlePlayerView = itemView.findViewById(R.id.rowItemGameFrag_singlePlayerIcon);
            multiPlayerView = itemView.findViewById(R.id.rowItemGameFrag_multiPlayerIcon);
        }
    }

    /**
     * Lay out the map for us on top of our line.
     * The function Creates a new view for each row in the RecyclerView.
     */
    @NonNull
    public GameAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_row_item_game, parent, false);

        return new MyViewHolder(view);
    }

    /**
     * Build one row inside our recyclerView
     * The function connects data to a specific row in the RecyclerView.
     * According to the position, extracts information from the list (dataSet) and updates the UI
     * @param position  position of the game in dataset
     */
    @Override
    public void onBindViewHolder(@NonNull GameAdapter.MyViewHolder holder, int position) {

        // ========================
        // game details
        // =========================
        Game gameItem = dataSet.get(position);

        // Set name
        String nameStr = gameItem.getName();
        holder.nameView.setText(nameStr);

        // Set the release year
        String releaseYearStr = String.valueOf(gameItem.getReleaseYear());
        holder.releaseYearView.setText(releaseYearStr);

        // Set single player visibility
        if(gameItem.isSinglePlayer())
            holder.singlePlayerView.setVisibility(View.VISIBLE);
        else
            holder.singlePlayerView.setVisibility(View.GONE);

        // Set multi player visibility
        if(gameItem.isMultiPlayer())
            holder.multiPlayerView.setVisibility(View.VISIBLE);
        else
            holder.multiPlayerView.setVisibility(View.GONE);

        // Set the image
        String imageURL = gameItem.getImageURL();
        ImageUtils imageUtils = new ImageUtils();
        imageUtils.loadImageIntoView(imageURL, holder.imageViewGame, holder.imageViewGame.getContext());

        // Set the average rate
        @SuppressLint("DefaultLocale") String avgRateStr = String.format("%.1f", gameItem.getAvgRate());
        holder.avgRateView.setText(avgRateStr);

        // Set description
        String descriptionStr = gameItem.getDescription();
        holder.descriptionView.setText(descriptionStr);
        handleReadMore(holder.descriptionView, holder.readMoreView);

        // Handle clicking on game name
        holder.nameView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("gameItem", gameItem);
            // navigate using the correct action, based on location property
            if (location.equals("home"))
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_gameFragment, bundle);
            else if (location.equals("favorites"))
                Navigation.findNavController(v).navigate(R.id.action_favoritesFragment_to_gameFragment, bundle);
        });

        // ========================
        // logged in user
        // =========================
        FirebaseUser firebaseUser = authRepository.getLoggedInUser();
        String userID;
        userID = (firebaseUser != null)? firebaseUser.getUid() : "";

        // ========================
        // Handlers
        // =========================
        // rate
        handleRate(userID, holder.rateView, gameItem.getId(), position);
        // favorite
        handleFavorite(userID, holder.favoriteBtn, gameItem.getId(), holder.getAdapterPosition());
    }

    /**
     * The function handles game rating
     * @param userID    logged in user id
     * @param rateView  to display the updated rate
     * @param gameID    the game id
     * @param position  in dataset
     */
    private void handleRate(String userID, TextView rateView, Long gameID, int position)
    {
        rateView.setOnClickListener(v -> {
            if (!userID.isEmpty()) // user is signed in
                RatingHandlerUtils.showRatingPopupAndHandleSubmit(gameID, userID, v.getContext(), v,
                        (updatedAvgRate, updatedNumOfRaters) -> {
                            // Update dataset and refresh RecyclerView
                            dataSet.get(position).setAvgRate(updatedAvgRate);
                            dataSet.get(position).setNumOfRaters(updatedNumOfRaters);
                            notifyItemChanged(position);
                        }
                );
            else
                CustomAlertDialog.showCustomDialog(v.getContext(), "You need to be signed in to perform this action.", CustomAlertDialog.MessageType.INFO);
        });
    }

    /**
     * The function handles adding/removing game from user's favorites.
     * @param userID    logged in user id
     * @param favoriteBtn   favorite button
     * @param gameID    the game id
     * @param position   to remove from view if necessary
     */
    private void handleFavorite(String userID, ImageButton favoriteBtn, Long gameID, int position)
    {
        // Set up the icon image
        if (!userID.isEmpty()) {
            userFavoritesRepository.checkIfUserFavorites(userID, gameID, new UserFavoritesRepository.OnFavoritesCheckCallback() {
                @Override
                public void onSuccess(boolean isFavorite) {
                    if (isFavorite) {
                        favoriteBtn.setImageResource(R.drawable.baseline_favorite_red); // Change UI to filled heart icon
                    } else {
                        favoriteBtn.setImageResource(R.drawable.baseline_favorite_border_red); // Change UI to outlined heart icon
                    }
                }
                @Override
                public void onFailure(boolean error) {
                    Log.e("FavoritesCheck", "Failed to check favorites status");
                }
            });
        } else { // user is not signed in
            favoriteBtn.setImageResource(R.drawable.baseline_favorite_border_red); // UI is outlined heart icon
        }
        // Set up the 'favorite' button click listener
        favoriteBtn.setOnClickListener(v -> {
            if (!userID.isEmpty()) // user is signed in
            {
                userFavoritesRepository.toggleUserFavorite(userID, gameID, new UserFavoritesRepository.OnToggleUserFavoriteCallback() {
                    @Override
                    public void onSuccess(boolean added) {
                        if (added) { // adding to favorites was performed successfully
                            favoriteBtn.setImageResource(R.drawable.baseline_favorite_red); // change UI to Filled heart
                            CustomAlertDialog.showCustomDialog(v.getContext(), "Added to favorites!", CustomAlertDialog.MessageType.SUCCESS);
                        } else
                        { // removing from favorites was performed successfully
                            favoriteBtn.setImageResource(R.drawable.baseline_favorite_border_red); // change UI to Outline heart
                            CustomAlertDialog.showCustomDialog(v.getContext(), "Removed from favorites.", CustomAlertDialog.MessageType.INFO);
                            // special case: if current location is 'favorites', removing a game from favorites changes dataset.
                            // (we want that the game will be removed from dataset and from UI)
                            if (location.equals("favorites")) {
                                // Remove item from dataset
                                dataSet.remove(position);
                                // Notify the adapter that an item has been removed
                                notifyItemRemoved(position);
                                // After removing the item, adjust positions of the remaining items in the dataset
                                // This ensures that the view stays in sync with the dataset
                                notifyItemRangeChanged(position, getItemCount() - position);
                                // Check if the dataset is empty
                                if (getItemCount() == 0 && onFavoritesEmptyListener != null) {
                                    onFavoritesEmptyListener.onFavoritesEmpty(); // notify the listener that favorite list became empty
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        CustomAlertDialog.showCustomDialog(v.getContext(), "Favorites update failed: " + errorMessage, CustomAlertDialog.MessageType.ERROR);
                    }
                });
            }
            else // user is not signed in
                CustomAlertDialog.showCustomDialog(v.getContext(), "You need to be signed in to perform this action.", CustomAlertDialog.MessageType.INFO);
        });
    }

    public interface OnFavoritesEmptyListener { // inform FavoritesFragment once favorite list becomes empty
        void onFavoritesEmpty();
    }

    /**
     * The function handles visibility of Read More text, limits description field to 3 lines.
     * @param descriptionView - view of description
     * @param readMoreView - view of read More/read Less text
     */
    @SuppressLint("SetTextI18n")
    private void handleReadMore(TextView descriptionView, TextView readMoreView) {
        descriptionView.setMaxLines(3);
        readMoreView.setVisibility(View.GONE); // Hide "Read More" initially

        descriptionView.post(() -> { // run this block after RecycledView was loaded fully
            if (descriptionView.getLineCount() > 3) {
                readMoreView.setVisibility(View.VISIBLE);
            }
        });

        readMoreView.setOnClickListener(v -> {
            if (descriptionView.getMaxLines() == 3) {
                descriptionView.setMaxLines(Integer.MAX_VALUE);
                readMoreView.setText("Read Less");
            } else {
                descriptionView.setMaxLines(3);
                readMoreView.setText("Read More");
            }
        });
    }

    /**
     * The function Performs the filtering and updates the list of games displayed in the RecyclerView,
     * updates dataset accordingly
     */
    @SuppressLint("NotifyDataSetChanged")
    public void filterData(String searchText, ArrayList<Long> selectedGenreIds, ArrayList<Integer> selectedYears,
                           ArrayList<Long> selectedPlatformIds, ArrayList<Long> selectedDeveloperIds,
                           ArrayList<Long> selectedPublisherIds, ArrayList<Long> selectedSeriesIds,
                           ArrayList<Integer> selectedAvgRatesAbove,
                           boolean isSinglePlayer, boolean isMultiPlayer)
    {
        ArrayList<Game> filteredList = new ArrayList<>();

        if (searchText.isEmpty() && selectedGenreIds.isEmpty() && selectedYears.isEmpty() && selectedPlatformIds.isEmpty() &&
                    selectedDeveloperIds.isEmpty() && selectedPublisherIds.isEmpty() &&
                    selectedAvgRatesAbove.isEmpty() &&
                    selectedSeriesIds.isEmpty() && isSinglePlayer && isMultiPlayer)
        {
            filteredList.addAll(dataSetFull);
        }
        else if (isSinglePlayer || isMultiPlayer)
        {
            for (Game game : dataSetFull) {
                if (!game.getName().toLowerCase().contains(searchText.toLowerCase()))
                    continue;
                if (!((isSinglePlayer && game.isSinglePlayer()) || (isMultiPlayer && game.isMultiPlayer()))) {
                    continue;
                }
                // Filter by average rating
                // check if the rate of current game is larger than at least one option
                if (!selectedAvgRatesAbove.isEmpty()) {
                    boolean largerThanAtLeastOne = false;
                    for (int rate : selectedAvgRatesAbove) {
                        if (game.getAvgRate() >= rate) {
                            largerThanAtLeastOne = true;
                            break; // Stop checking once a match is found
                        }
                    }
                    if (!largerThanAtLeastOne) {
                        continue;
                    }
                }
                // looking for specific ID in Array of IDs
                if (!selectedDeveloperIds.isEmpty() && !selectedDeveloperIds.contains(game.getDeveloperID()))
                    continue;
                if (!selectedPublisherIds.isEmpty() && !selectedPublisherIds.contains(game.getPublisherID()))
                    continue;
                if (!selectedSeriesIds.isEmpty() && !selectedSeriesIds.contains(game.getSeriesID()))
                    continue;
                if (!selectedYears.isEmpty() && !selectedYears.contains(game.getReleaseYear()))
                    continue;

                // looking for several IDs in a Array of IDs (Collection.disjoint will help us)
                if (!selectedGenreIds.isEmpty() && Collections.disjoint(selectedGenreIds, game.getGenreIds())) // disjoint- Returns true if the two specified collections have no elements in common.
                    continue;
                if (!selectedPlatformIds.isEmpty() && Collections.disjoint(selectedPlatformIds, game.getPlatformIds())) // disjoint- Returns true if the two specified collections have no elements in common.
                    continue;

                // if reached this point- the current game passed all filters
                filteredList.add(game);
            }
        }
        // update the adapter with the new dataset
        dataSet.clear();
        dataSet.addAll(filteredList);
        /* update the RecyclerView
        When we make changes to the data set (dataSet) associated with the Adapter, we must call this function to update the view*/
        notifyDataSetChanged();
    }

    /**
     * The function Performs the sorting and updates the list of games displayed in the RecyclerView,
     * updates dataset accordingly
     * @param sortBy    sorting mode ("Alphabetical", ..)
     * @param isAscending   boolean
     */
    @SuppressLint("NotifyDataSetChanged")
    public void sortDataSet(String sortBy, boolean isAscending)
    {
        if (sortBy.isEmpty() || dataSet == null || dataSet.isEmpty()) {
            return; // No sorting if dataset is empty or invalid sorting key
        }

        switch (sortBy) {
            case "Alphabetical":
                if (isAscending) {
                    dataSet.sort(Comparator.comparing(Game::getName, String.CASE_INSENSITIVE_ORDER));
                } else {
                    dataSet.sort(Comparator.comparing(Game::getName, String.CASE_INSENSITIVE_ORDER).reversed());
                }
                break;
            case "Rating":
                if (isAscending) {
                    dataSet.sort(Comparator.comparingDouble(Game::getAvgRate));
                } else {
                    dataSet.sort(Comparator.comparingDouble(Game::getAvgRate).reversed());
                }
                break;
            case "Release Year":
                if (isAscending) {
                    dataSet.sort(Comparator.comparingInt(Game::getReleaseYear));
                } else {
                    dataSet.sort(Comparator.comparingInt(Game::getReleaseYear).reversed()); // Latest first
                }
                break;
            default:
                return; // Do nothing if the sorting option is not recognized
        }
        // Notify RecyclerView adapter
        notifyDataSetChanged();
    }

    /**
     * @return How many members there are in the adapter (=size of the dataset array).
     *          that way the adapter knows how many lines to display
     */
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

