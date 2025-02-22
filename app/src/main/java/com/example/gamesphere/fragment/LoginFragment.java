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
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.utils.CustomAlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Objects;

/**
 * The class represents Fragment for user login.
 * Corresponding content view: fragment_login.xml
 */
public class LoginFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        View viewScreen = inflater.inflate(R.layout.fragment_login, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(false); // do not allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("Login");

        // ========================
        // get view elements
        // =========================
        Button buttonReg= viewScreen.findViewById(R.id.loginFrag_signUpButton);
        Button buttonLogIn= viewScreen.findViewById(R.id.loginFrag_loginButton);

        // ========================
        // events
        // =========================
        // Set click listener for register button
        buttonReg.setOnClickListener(v -> Navigation.findNavController(viewScreen)
                .navigate(R.id.action_loginFragment_to_registerFragment));
        // Set click listener for login button
        buttonLogIn.setOnClickListener(v -> logInFunc(viewScreen, requireActivity()));
        return viewScreen;
    }

    /**
     * The function signs in the user (based on form fields), and navigates to the next fragment.
     * Is called when sign in form fields are valid.
     * Displays messages to user.
     * @param viewScreen    the view
     * @param context       the context
     */
    public void logInFunc(View viewScreen, Context context)
    {
        boolean isValid = true;
        EditText emailEditText = viewScreen.findViewById(R.id.loginFrag_email);
        EditText passwordEditText = viewScreen.findViewById(R.id.loginFrag_password);
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate the input fields for Login.
        if (email.isEmpty()) {
            emailEditText.setError("email cannot be empty");
            isValid = false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            isValid = false;
        }
        if (!isValid) {
            return;
        }

        // Sign in through Authentication
        AuthenticationRepository authRepository = new AuthenticationRepository();
        authRepository.signInUser(email, password, new AuthenticationRepository.OnSignInCallback() {
            @Override
            public void onSuccess() {
                // Navigate to Home
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_homeFragment);
                // Update BottomNavigationView in MainActivity
                if (context instanceof MainActivity) {
                    BottomNavigationView bottomNavigationView = ((MainActivity) context).getBottomNavigationView();
                    bottomNavigationView.setSelectedItemId(R.id.menu_home_item);
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                // If sign-in fails, display a message to the user
                CustomAlertDialog.showCustomDialog(context, "Login failed - one or more of the details are incorrect.", CustomAlertDialog.MessageType.ERROR);
            }
        });
    }
}