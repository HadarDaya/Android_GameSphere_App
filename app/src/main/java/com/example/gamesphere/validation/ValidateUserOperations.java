package com.example.gamesphere.validation;

import android.widget.EditText;
import com.example.gamesphere.model.User;
import com.example.gamesphere.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

/**
 * Helper class to validate user operation forms (registration, update)
 */
public class ValidateUserOperations
{
    private static final UserRepository userRepository = new UserRepository();

    /**
     * Asynchronous function validates user registration form fields before submission.
     * @return task result: true if all fields are valid, otherwise- false.
     */
    public static Task<Boolean> validateUserRegisterForm(EditText usernameField, EditText emailField,
                                                  EditText passwordField, EditText verifyPasswordField) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        // flag to track if all validations pass
        final boolean[] isValid = {true};

        // Chain validations
        isValidateEmail(emailField).continueWithTask(emailTask -> {
            if (!emailTask.getResult()) {
                isValid[0] = false;
            }
            return isValidateUsername(usernameField, null, ValidationType.REGISTRATION);
        }).continueWithTask(usernameTask -> {
            if (!usernameTask.getResult()) {
                isValid[0] = false;
            }
            isValid[0] = validatePassword(passwordField, verifyPasswordField).getResult();
            return Tasks.forResult(isValid[0]);  // Skip password validation if not changing password
        }).addOnCompleteListener(task -> {
            // After all validations are done, check if any validation failed
            taskCompletionSource.setResult(isValid[0]);
        });

        return taskCompletionSource.getTask();
    }

    public static Task<Boolean> validateUserUpdateForm(EditText usernameField, EditText emailField,
                                                EditText newPasswordField, EditText verifyPasswordField,
                                                boolean isChangePasswordChecked, User user) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        // Mutable flag to track if all validations pass
        final boolean[] isValid = {true};

        // Chain validations
        isValidateEmail(emailField).continueWithTask(emailTask -> {
            if (!emailTask.getResult()) {
                isValid[0] = false;  // Mark as invalid if email validation fails
            }
            return isValidateUsername(usernameField, user.getUsername(), ValidationType.UPDATE);
        }).continueWithTask(usernameTask -> {
            if (!usernameTask.getResult()) {
                isValid[0] = false;  // Mark as invalid if username validation fails
            }
            if (isChangePasswordChecked) {
                isValid[0] = validatePassword(newPasswordField, verifyPasswordField).getResult();
            }
            return Tasks.forResult(isValid[0]);  // Skip password validation if not changing password
        }).addOnCompleteListener(task -> {
            // After all validations are done, check if any validation failed
            taskCompletionSource.setResult(isValid[0]);
        });

        return taskCompletionSource.getTask();
    }

    public static Task<Boolean> isValidateEmail(EditText emailField) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            emailField.setError("Email cannot be empty");
            taskCompletionSource.setResult(false);
        } else {
            String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
            boolean isValid = email.matches(emailPattern);
            if (!isValid) {
                emailField.setError("Invalid email format");
            }
            taskCompletionSource.setResult(isValid);
        }

        return taskCompletionSource.getTask();
    }

    public static Task<Boolean> isValidateUsername(EditText usernameField, String currentUsername, ValidationType mode) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String username = usernameField.getText().toString().trim();

        if (username.isEmpty()) {
            usernameField.setError("Username cannot be empty");
            taskCompletionSource.setResult(false);
        } else {
            // Check if the username needs to be validated for registration or update
            if (mode == ValidationType.REGISTRATION || (mode == ValidationType.UPDATE && !username.equals(currentUsername))) {
                // Use the checkUsernameExists method that now returns a Task<Boolean>
                userRepository.checkUsernameExists(username)
                        .addOnSuccessListener(usernameExists -> {
                            if (usernameExists) {
                                usernameField.setError("Username is already taken");
                                taskCompletionSource.setResult(false);
                            } else {
                                taskCompletionSource.setResult(true);
                            }
                        })
                        .addOnFailureListener(taskCompletionSource::setException);
            } else {
                taskCompletionSource.setResult(true);
            }
        }

        return taskCompletionSource.getTask();
    }

    public static Task<Boolean> validatePassword(EditText passwordField, EditText verifyPasswordField) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        String password = passwordField.getText().toString().trim();
        String verifyPassword = verifyPasswordField.getText().toString().trim();

        if (password.isEmpty()) {
            passwordField.setError("Password cannot be empty");
            taskCompletionSource.setResult(false);
        } else if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            taskCompletionSource.setResult(false);
        } else if (verifyPassword.isEmpty()) {
            verifyPasswordField.setError("Verify Password cannot be empty");
            taskCompletionSource.setResult(false);
        } else if (!password.equals(verifyPassword)) {
            verifyPasswordField.setError("Passwords do not match");
            taskCompletionSource.setResult(false);
        } else {
            taskCompletionSource.setResult(true);
        }

        return taskCompletionSource.getTask();
    }

    /**
     * Enum for validation type (kind of validation)
     */
    public enum ValidationType {
        REGISTRATION,
        UPDATE
    }
}
