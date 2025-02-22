package com.example.gamesphere.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.example.gamesphere.MainActivity;
import com.example.gamesphere.R;
import com.example.gamesphere.model.User;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.example.gamesphere.validation.ValidateUserOperations;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

/**
 * The class represents Fragment for user registration.
 * Corresponding content view: fragment_register.xml
 */
public class RegisterFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_register, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(false); // do not allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("Registration");

        // ========================
        // get view elements
        // =========================
        EditText usernameEditText = viewScreen.findViewById(R.id.regFrag_username);
        EditText emailEditText = viewScreen.findViewById(R.id.regFrag_email);
        EditText passwordEditText = viewScreen.findViewById(R.id.regFrag_password);
        EditText verifyPasswordEditText = viewScreen.findViewById(R.id.regFrag_verifyPassword);

        // ========================
        // events
        // =========================
        // Set click listener for register button
        Button register = viewScreen.findViewById(R.id.regFrag_signUpButton);
        register.setOnClickListener(v -> {
            // Validate input fields before proceeding with registration
            ValidateUserOperations validation = new ValidateUserOperations();
            validation.validateUserRegisterForm(usernameEditText, emailEditText, passwordEditText, verifyPasswordEditText)
                    .addOnSuccessListener(isValid -> { // all fields are valid
                        if (isValid) {
                            registerFunc(viewScreen, requireActivity());
                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        });
        // Set click listener for login button
        Button loginButton= viewScreen.findViewById(R.id.regFrag_login);
        loginButton.setOnClickListener(v ->
                Navigation.findNavController(viewScreen).navigate(R.id.action_registerFragment_to_loginFragment));
        return viewScreen;
    }

    /**
     * The function registers the user (based on form fields), and navigates to the next fragment.
     * Is called when registration form fields are valid.
     * Displays messages to user.
     * @param viewScreen    the view
     * @param context       the context
     */
    public void registerFunc(View viewScreen, Context context)
    {
        // Get the values from view
        EditText emailEditText = viewScreen.findViewById(R.id.regFrag_email);
        EditText passwordEditText = viewScreen.findViewById(R.id.regFrag_password);
        EditText usernameEditText = viewScreen.findViewById(R.id.regFrag_username);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        AuthenticationRepository authRepository = new AuthenticationRepository();
        UserRepository userRepository = new UserRepository();

        // Create user in Authentication table
        authRepository.createUser(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fireBaseUser = task.getResult(); // Successfully created the user in Authentication
                        // Create user in User table
                        userRepository.insertUser(fireBaseUser, username, false,
                                new UserRepository.OnUserInsertedCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        // Navigate to the Home
                                        Navigation.findNavController(viewScreen).navigate(R.id.action_registerFragment_to_homeFragment);
                                        // Update BottomNavigationView in MainActivity
                                        if (context instanceof MainActivity) {
                                            BottomNavigationView bottomNavigationView = ((MainActivity) context).getBottomNavigationView();
                                            bottomNavigationView.setSelectedItemId(R.id.menu_home_item);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        String errorMessage = e != null ? e.getMessage() : "Unknown error";
                                        CustomAlertDialog.showCustomDialog(context, "Registration failed: " + errorMessage, CustomAlertDialog.MessageType.ERROR);
                                    }
                                });
                    } else {
                        // Handle errors during user creation
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                        CustomAlertDialog.showCustomDialog(context, "Registration failed: " + errorMessage, CustomAlertDialog.MessageType.ERROR);
                    }
                });
    }
}