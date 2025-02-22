package com.example.gamesphere.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to handle YouTube video loading.
 */
public class YoutubePlayerUtils {

    /**
     * Initializes the YouTube player and loads the given video ID.
     * @param lifecycle The lifecycle of the activity/fragment (needed for proper cleanup)
     * @param youTubePlayerView The YouTubePlayerView component
     * @param youtubeUrl The full YouTube URL (for example, https://youtu.be/E3Huy2cdih0)
     */
    public static void setupYouTubePlayer(Lifecycle lifecycle, YouTubePlayerView youTubePlayerView, String youtubeUrl) {
        if (youTubePlayerView == null || youtubeUrl == null) {
            return;
        }

        // Extract video ID only, from the given URL
        String videoId = extractVideoId(youtubeUrl); // for example, E3Huy2cdih0
        if (videoId == null) {
            return; // Invalid URL, do nothing
        }

        // Attach lifecycle observer
        lifecycle.addObserver(youTubePlayerView);

        // Load the video
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);
            }
        });
    }

    /**
     * The function extracts the YouTube video ID from a given URL.
     * (Supports various YouTube URL formats)
     * @param youtubeUrl The YouTube URL (for example, https://youtu.be/E3Huy2cdih0)
     * @return String of the extracted video ID, or null if invalid (for example, E3Huy2cdih0)
     */
    public static String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
            return null;
        }

        // Regex to extract video ID from different YouTube URL formats
        String regex = Constants.YOUTUBE_ID_PATTERN;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(youtubeUrl);

        if (matcher.find()) { // found a matching
            return matcher.group(1); // return the extracted video ID
        } else {
            return null;
        }
    }
}
