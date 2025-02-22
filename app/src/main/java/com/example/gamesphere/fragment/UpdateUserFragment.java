package com.example.gamesphere.fragment;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.example.gamesphere.R;
import com.example.gamesphere.model.User;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.validation.ValidateUserOperations;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The class represents Fragment of specific game.
 * Corresponding content view: fragment_update_user.xml
 */
public class UpdateUserFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // UI components
    // ----------------------
    private EditText usernameEditText, emailEditText,newPasswordEditText,verifyPasswordEditText;
    private CheckBox changePasswordCheckBox;

    // Repositories
    // ----------------------
    private final UserRepository userRepository = new UserRepository();
    private final AuthenticationRepository authRepository = new AuthenticationRepository();

    public UpdateUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateDetailsFragment.
     */
    public static UpdateUserFragment newInstance(String param1, String param2) {
        UpdateUserFragment fragment = new UpdateUserFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_update_user, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(true); // allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("Update Account");

        // ================================
        // get UI elements
        // =================================
        usernameEditText = viewScreen.findViewById(R.id.updateDetailsFrag_username);
        emailEditText = viewScreen.findViewById(R.id.updateDetailsFrag_email);
        changePasswordCheckBox = viewScreen.findViewById(R.id.updateDetailsFrag_changePasswordCheckBox);
        LinearLayout changePasswordFields = viewScreen.findViewById(R.id.changePasswordFields);
        Button updateButton = viewScreen.findViewById(R.id.updateDetailsFrag_updateButton);

        // ================================
        // init user details (display)
        // =================================
        initializeUserDetails();

        // ================================
        // events
        // =================================
        // Set listener for the checkbox to toggle password fields visibility
        changePasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                changePasswordFields.setVisibility(View.VISIBLE);
                newPasswordEditText = viewScreen.findViewById(R.id.updateDetailsFrag_newPassword);
                verifyPasswordEditText = viewScreen.findViewById(R.id.updateDetailsFrag_verifyPassword);
            } else {
                changePasswordFields.setVisibility(View.GONE);
                newPasswordEditText = null;
            }
        });
        // Set listener for update button click
        updateButton.setOnClickListener(v -> validateAndUpdateDetails());

        return  viewScreen;
    }

    /**
     * The function displays current user details.
     */
    private void initializeUserDetails()
    {
        FirebaseUser firebaseUser = authRepository.getLoggedInUser();
        if (firebaseUser != null) {
            emailEditText.setText(firebaseUser.getEmail());
            userRepository.getUserByUid(firebaseUser.getUid())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Successfully retrieved the user
                            User user = task.getResult();
                            usernameEditText.setText(user.getUsername());
                        } else {
                            Log.e("UserFetchError", "Failed to fetch user: ", task.getException());
                        }
                    });
        }
    }

    /**
     * The function calls:
     * 1. a method which validates form fields (using ValidationUtils)
     * 2. a method which updates user details
     * If validation passed- force sign out and navigate to Login
     */
    private void validateAndUpdateDetails() {
        FirebaseUser firebaseUser = authRepository.getLoggedInUser();
        if (firebaseUser == null) {
            return;
        }

        userRepository.getUserByUid(firebaseUser.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Successfully retrieved the user
                        User user = task.getResult();

                        ValidateUserOperations.validateUserUpdateForm(usernameEditText, emailEditText,
                                        newPasswordEditText, verifyPasswordEditText,
                                        changePasswordCheckBox.isChecked(), user)
                                .addOnSuccessListener(isValid -> {
                                    // All the fields are valid
                                    if (isValid) {
                                        String newUsername = usernameEditText.getText().toString().trim();
                                        String newEmail = emailEditText.getText().toString().trim();
                                        String newPassword = "";
                                        if (newPasswordEditText != null) {
                                            newPassword = newPasswordEditText.getText().toString().trim();
                                        }

                                        // Create a list of tasks to perform concurrently
                                        List<Task<Void>> tasks = new ArrayList<>();
                                        tasks.add(changeUsername(newUsername, user, firebaseUser));
                                        tasks.add(changeEmail(newEmail, firebaseUser));
                                        if (!newPassword.isEmpty()) {
                                            tasks.add(changePassword(newPassword));
                                        }

                                        // Once all tasks are complete, sign out and navigate to Login
                                        Tasks.whenAllSuccess(tasks).addOnCompleteListener(allTasks -> {
                                            authRepository.signOutUser();
                                            Navigation.findNavController(requireView()).navigate(R.id.action_updateDetailsFragment_to_loginFragment);
                                        }).addOnFailureListener(e -> {
                                            // Handle any errors that occur during the changes
                                            Log.e("UpdateDetailsError", "Failed to update details: ", e);
                                        });
                                    }
                                })
                                .addOnFailureListener(Throwable::printStackTrace);

                    } else {
                        // Handle the case where the user is null or task failed
                        Log.e("UserFetchError", "Failed to fetch user: ", task.getException());
                    }
                });
    }

    /**
     * Asynchronous function which sets user's password
     * @param newPassword the required new password
     */
    public Task<Void> changePassword(String newPassword) {
        if (changePasswordCheckBox.isChecked()) { // user wants to change his password
            TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

            authRepository.updateUserPassword(newPassword, new AuthenticationRepository.OnPasswordUpdateCallback() {
                @Override
                public void onSuccess() {
                    CustomAlertDialog.showCustomDialog(getContext(), "Password updated successfully!", CustomAlertDialog.MessageType.SUCCESS);
                    taskCompletionSource.setResult(null); // Completing the task successfully
                }

                @Override
                public void onFailure(Exception e) {
                    CustomAlertDialog.showCustomDialog(getContext(), "Password update failed: " + e.getMessage(), CustomAlertDialog.MessageType.ERROR);
                    taskCompletionSource.setException(e); // Completing the task with an exception
                }
            });

            return taskCompletionSource.getTask();
        }
        return Tasks.forResult(null); // If no password change is requested, return a completed task
    }

    /**
     * Asynchronous function which sets user's email
     * @param newEmail     the required new password
     * @param firebaseUser logged in user
     */
    public Task<Void> changeEmail(String newEmail, FirebaseUser firebaseUser) {
        if (!newEmail.equals(firebaseUser.getEmail())) { // user wants to change his email
            TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

            authRepository.verifyUserEmailAndUpdateEmail(newEmail, new AuthenticationRepository.OnEmailUpdateCallback() {
                @Override
                public void onSuccess() {
                    CustomAlertDialog.showCustomDialog(getContext(), "Verification email sent. Please check your inbox.", CustomAlertDialog.MessageType.INFO);
                    taskCompletionSource.setResult(null); // Completing the task successfully
                }

                @Override
                public void onFailure(Exception e) {
                    CustomAlertDialog.showCustomDialog(getContext(), "Failed to send verification email: " + e.getMessage(), CustomAlertDialog.MessageType.ERROR);
                    taskCompletionSource.setException(e); // Completing the task with an exception
                }
            });

            return taskCompletionSource.getTask();
        }
        return Tasks.forResult(null); // If no email change is requested, return a completed task
    }

    /**
     * Asynchronous function which sets user's username
     * @param newUsername  the required new username
     * @param user         logged in user (User instance)
     * @param firebaseUser logged in user
     */
    public Task<Void> changeUsername(String newUsername, User user, FirebaseUser firebaseUser) {
        if (!newUsername.equals(user.getUsername())) { // user wants to change his username
            TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

            userRepository.updateUsername(firebaseUser, user, newUsername, new UserRepository.OnUpdateUsernameCallback() {
                @Override
                public void onSuccess() {
                    CustomAlertDialog.showCustomDialog(getContext(), "Username updated successfully!", CustomAlertDialog.MessageType.SUCCESS);
                    taskCompletionSource.setResult(null); // Completing the task successfully
                }

                @Override
                public void onFailure(Exception e) {
                    CustomAlertDialog.showCustomDialog(getContext(), "Username update failed: " + e.getMessage(), CustomAlertDialog.MessageType.ERROR);
                    taskCompletionSource.setException(e); // Completing the task with an exception
                }
            });
            return taskCompletionSource.getTask();
        }
        return Tasks.forResult(null); // If no username change is requested, return a completed task
    }
}