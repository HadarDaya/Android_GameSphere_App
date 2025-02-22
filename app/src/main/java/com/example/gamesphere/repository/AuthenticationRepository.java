package com.example.gamesphere.repository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

/**
 * The class interacts with Authentication Firebase table.
 * Operations such as: getLoggedInUser, createUser, signInUser, etc.
 */
public class AuthenticationRepository {

    private final FirebaseAuth authRef = FirebaseAuth.getInstance();

    /**
     * The function returns an object of the currently logged in user.
     */
    public FirebaseUser getLoggedInUser()
    {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Asynchronous function which creates a new user in Authentication table, using the given parameters.
     * @param email     user email (given by the user)
     * @param password  user password (given by the user)
     * @return on success- task result, otherwise- exception.
     */
    public Task<FirebaseUser> createUser(String email, String password)
    {
        // Create a TaskCompletionSource to return the result asynchronously (or error, if exists)
        TaskCompletionSource<FirebaseUser> taskCompletionSource = new TaskCompletionSource<>();

        // Create user in Firebase Authentication table
        authRef.createUserWithEmailAndPassword(email, password) // the mission, returns AuthResult
                .addOnCompleteListener(task -> { // a listener, will execute as soon as the mission is done
                    if (task.isSuccessful()) {
                        FirebaseUser user = authRef.getCurrentUser();
                        taskCompletionSource.setResult(user); // Successfully created user
                    } else {
                        taskCompletionSource.setException(Objects.requireNonNull(task.getException())); // Failure
                    }
                });

        return taskCompletionSource.getTask(); // Return the Task (with its result/error)
    }

    /**
     * Asynchronous function which performs login using Authentication table, for the given parameters.
     * If failed- displays a message
     * @param email     user email (given by the user)
     * @param password  user password (given by the user)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void signInUser(String email, String password, OnSignInCallback callback) {
        authRef.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Call success callback (navigation will be handled outside)
                        callback.onSuccess();
                    } else {
                        // Call failure callback with error message
                        callback.onFailure("Login failed - one or more of the details are incorrect.");
                    }
                });
    }

    /**
     * The function performs sign out.
     */
    public void signOutUser()
    {
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * Asynchronous function which performs Email update.
     * @param newEmail  user new email (as requested by the user)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void verifyUserEmailAndUpdateEmail(String newEmail, OnEmailUpdateCallback callback) {
        FirebaseUser user = authRef.getCurrentUser();
        if (user != null) {
            user.verifyBeforeUpdateEmail(newEmail) // the mission
                    // Verification email sent to the new email.
                    // When the user clicks the email link,
                    // it will update to newEmail@example.com
                    // Until then, the old email remains on the account.
                    // (old email receives an email too, which notifies that the email changed)
                    .addOnCompleteListener(task -> { // a listener, will execute as soon as the mission is done
                        if (task.isSuccessful()) {
                            // Verification email sent successfully.
                            callback.onSuccess();
                        } else {
                            // Failed to send verification email.
                            callback.onFailure(task.getException());
                        }
                    });
        } else {
            callback.onFailure(new Exception("No user is currently signed in."));
        }
    }

    /**
     * Asynchronous function which performs Password update.
     * @param newPassword   new password (as requested by user)
     * @param callback  the result is not immediate, callback will consist the result as soon as
     *                  there is one. (when the listener was informed that the mission completed)
     */
    public void updateUserPassword(String newPassword, OnPasswordUpdateCallback callback)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        user.updatePassword(newPassword) // the mission
                // password updates immediately, without sending an email
                .addOnCompleteListener(task -> { // a listener, will execute as soon as the mission is done
                    if (task.isSuccessful()) {
                        // Password was updated successfully.
                        callback.onSuccess();
                    } else {
                        // Failed to update password
                        callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Interface for OnEmailUpdateCallback callback
     */
    public interface OnEmailUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Interface for OnPasswordUpdateCallback callback
     */
    public interface OnPasswordUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Interface for OnSignInCallback callback
     */
    public interface OnSignInCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
