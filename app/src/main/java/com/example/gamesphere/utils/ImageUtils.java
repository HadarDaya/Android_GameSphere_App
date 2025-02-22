package com.example.gamesphere.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.gamesphere.R;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class to handle Image uploading to Cloudinary.
 * Steps are:
 * 1. user selects a picture from his device's gallery, the selected url is on his device.
 * 2. after form validation, uploadImage function is called, and the picture is uploaded to Cloudinary,
 *      the function returns the url on cloudinary. (allows access from anywhere)
 */
public class ImageUtils {

    // important comment (!)
    // "Cloudinary" service demands internet connection.
    // we do not want that Main thread (UI) will execute it (making the app UI become busy),
    // solution: defining ExecutorService
    private final ExecutorService executorService;
    private final Cloudinary mCloudinary;

    /**
     * Constructor which initialize Cloudinary access and executor
     */
    public ImageUtils() {
        // Initialize Cloudinary access (based on information from Dashboard)
        mCloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", Constants.CLOUDINARY_CLOUD_NAME,
                "api_key", Constants.CLOUDINARY_API_KEY,
                "api_secret", Constants.CLOUDINARY_API_SECRET
        ));
        executorService = Executors.newSingleThreadExecutor();
    }

    // uploadImage runs on a background thread (to avoid blocking the UI)

    /**
     * The function calls a function which uploads the given image to Cloudinary.
     * Is called after form passed all validation checkups, and selected an image from his gallery.
     * @param imageUriOnGallery the selected image uri (from user's local device's gallery)
     * @param context   context
     * @param callback  returns url on cloudinary, or exception.
     */
    public void uploadImage(Uri imageUriOnGallery, Context context, OnImageUploadCallback callback) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUriOnGallery);
            uploadImageToCloudinary(bitmap, callback);
        } catch (IOException e) {
            Log.e("ImageService", "Error converting image URI to Bitmap", e);
            callback.onFailure(e);
        }
    }

    /**
     * The function uploads the given image to Cloudinary.
     * The operation is done executorService, so it won't make app UI busy.
     * @param bitmap    bitmap of the image
     * @param callback  returns url on cloudinary, or exception.
     */
    public void uploadImageToCloudinary(Bitmap bitmap, OnImageUploadCallback callback) {
        executorService.execute(() -> {
            try {
                // Convert bitmap to byte array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                // Upload image to Cloudinary
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = mCloudinary.uploader().upload(byteArray, // retrieve the URL of the file on Cloudinary
                        ObjectUtils.asMap("folder", Constants.CLOUDINARY_FOLDER));

                // Extract URL from the response
                String cloudinaryImageUrl = (String) uploadResult.get("url");

                // Call success callback
                callback.onSuccess(cloudinaryImageUrl);

            } catch (IOException e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * The function receives imageURL and displays it (using Glide) in given imageView.
     * ExecutorService is not necessary here, because Glide is asynchronous.
     * If an error occurred- display default picture.
     * @param imageUrlOnCloudinary - url of the image (in our case- full location on Cloudinary)
     * @param imageView - view of the image
     * @param context - context of the image view
     */
    public void loadImageIntoView(String imageUrlOnCloudinary, ImageView imageView, Context context) {
        if (imageUrlOnCloudinary != null && !imageUrlOnCloudinary.isEmpty())
        {
            // replace HTTP by HTTPS
            if (!imageUrlOnCloudinary.startsWith("https://")) {
                imageUrlOnCloudinary = imageUrlOnCloudinary.replace("http://", "https://");
            }

            Glide.with(context)
                    .load(imageUrlOnCloudinary)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.game_placeholder) // Replace with your placeholder
                    .error(R.drawable.game_placeholder) // Replace with your error fallback
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.game_placeholder); // Handle missing URL
        }
    }

    /**
     * Interface for OnImageUploadCallback callback
     */
    public interface OnImageUploadCallback {
        void onSuccess(String cloudinaryImageUrl);
        void onFailure(Exception e);
    }
}