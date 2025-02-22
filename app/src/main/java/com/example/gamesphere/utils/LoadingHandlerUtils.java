package com.example.gamesphere.utils;

import android.view.View;
import android.widget.FrameLayout;
import com.airbnb.lottie.LottieAnimationView;

/**
 * Helper class which consists methods that reflect on loading display.
 */
public class LoadingHandlerUtils {

    /**
     * The function shows animation of loading lottie.
     * (for example, while fetching large amount of data)
     * @param loadingContainer  container of animation
     * @param loadingAnimation  animation
     */
    public static void showLoadingAnimation(FrameLayout loadingContainer, LottieAnimationView loadingAnimation) {
        loadingContainer.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
    }

    /**
     * The function hides animation of loading lottie.
     * (for example, when fetching large amount of data has finished)
     * @param loadingContainer  container of animation
     * @param loadingAnimation  animation
     */
    public static void hideLoadingAnimation(FrameLayout loadingContainer, LottieAnimationView loadingAnimation) {
        loadingAnimation.cancelAnimation();
        loadingContainer.setVisibility(View.GONE);
    }
}
