package com.example.gamesphere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.gamesphere.R;
import java.util.List;

/**
 * Custom ArrayAdapter for displaying a list of profile options in a ListView.
 * The class is called from ProfileFragment.java
 * Each item in the list represents an option, such as "Update Info" or "Logout".
 * Corresponding content view: custom_profile_option_item.xml
 */
public class ProfileOptionsAdapter extends ArrayAdapter<String> {
    private final List<String> options; // Option text
    private final List<Integer> icons; // Left icon resource IDs

    /**
     * Constructor for initializing the adapter with the context and list of options.
     *
     * @param context The current context
     * @param options A list of string options to display in the list.
     * @param icons   A list of icons
     */
    public ProfileOptionsAdapter(Context context, List<String> options, List<Integer> icons) {
        super(context, 0, options); // Calls the superclass constructor with a default layout ID of 0.
        this.options = options;
        this.icons = icons;
    }

    /**
     * Inflates and customizes the view for each item in the options list.
     *
     * @param position    The position of the current item in the list.
     * @param convertView The recycled view to populate (if available).
     * @param parent      The parent ViewGroup to which this view will be attached.
     * @return A view representing the current item in the list.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Reuse an existing view if possible, or inflate a new one.
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_profile_option_item,
                    parent, false);
        }
        // Bind views
        ImageView iconView = convertView.findViewById(R.id.optionIcon);
        TextView optionText = convertView.findViewById(R.id.optionText);
        ImageView arrowView = convertView.findViewById(R.id.arrowIcon);

        // Set option text and icons
        optionText.setText(options.get(position));
        iconView.setImageResource(icons.get(position));
        arrowView.setVisibility(View.VISIBLE);

        return convertView;
    }
}
