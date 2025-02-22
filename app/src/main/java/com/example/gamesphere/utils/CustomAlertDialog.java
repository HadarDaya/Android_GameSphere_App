package com.example.gamesphere.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.gamesphere.R;
import com.google.android.material.button.MaterialButton;
import java.util.Objects;

/**
 * Helper class which displays a custom AlertDialog
 */
public class CustomAlertDialog {

    /**
     * The function displays a custom AlertDialog
     * @param context context
     * @param message   message to display
     * @param type  type of message (success, error, info)
     */
    public static void showCustomDialog(Context context, String message, MessageType type)
    {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_alert_dialog, null);

        TextView messageTextView = view.findViewById(R.id.dialog_message);
        ImageView iconView = view.findViewById(R.id.dialog_icon);
        MaterialButton okButton = view.findViewById(R.id.dialog_button);

        // Set the message
        messageTextView.setText(message);

        switch (type)
        {
            case ERROR: iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.decor_icon_error));
                        okButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
                        okButton.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
            case INFO: iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.decor_icon_info));
                okButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.link_blue)));
                okButton.setTextColor(ContextCompat.getColor(context, R.color.link_blue));
                break;
            case SUCCESS: iconView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.decor_icon_success));
                okButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.neon_green)));
                okButton.setTextColor(ContextCompat.getColor(context, R.color.neon_green));
                break;
            default: break;
        }

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        // Show the dialog
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        // Handle ok button click
        okButton.setOnClickListener(v -> dialog.dismiss());
    }

    public enum MessageType {
        ERROR, INFO, SUCCESS
    }
}
