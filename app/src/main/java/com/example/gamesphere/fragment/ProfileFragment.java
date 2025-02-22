package com.example.gamesphere.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.example.gamesphere.R;
import com.example.gamesphere.adapter.ProfileOptionsAdapter;
import com.example.gamesphere.model.User;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The class represents Profile Fragment, which displays a menu depending on the user's type (regular or admin).
 * Corresponding content view: fragment_profile.xml
 * Additional views:
 *      custom_profile_option_item.xml (for single option display)
 */
public class ProfileFragment extends Fragment
{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // Repositories
    // ----------------------
    private final AuthenticationRepository authRepository = new AuthenticationRepository();
    private final UserRepository userRepository = new UserRepository();

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View viewScreen = inflater.inflate(R.layout.fragment_profile, container, false);

        // ========================
        // Top Toolbar
        // =========================
        ActionBar toolbar =  Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar());
        toolbar.setDisplayHomeAsUpEnabled(false); // do not allow "back" on Top Toolbar from this fragment
        toolbar.setTitle("My Profile");

        FirebaseUser firebaseUser = authRepository.getLoggedInUser();
        // Ensure the FirebaseUser is not null
        if (firebaseUser != null) {
            // Retrieve the user by UID
            userRepository.getUserByUid(firebaseUser.getUid())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Successfully retrieved the user
                            User user = task.getResult();
                            displayUserMenu(user, viewScreen);
                        }
                    });
        }
        return viewScreen;
    }

    /**
     * The function displays the corresponding menu for the given user.
     * @param user  the logged in user
     * @param viewScreen    view
     */
    @SuppressLint("SetTextI18n")
    public void displayUserMenu(User user, View viewScreen)
    {
        if (user != null)
        {
            // ========================
            // Welcome message
            // =========================
            TextView welcome_message = viewScreen.findViewById(R.id.profileFrag_welcomeMessage);
            // Display username dynamically
            welcome_message.setText("Hi " + user.getUsername() + "!");

            // ========================
            // Option list
            // =========================
            ListView optionsListView = viewScreen.findViewById(R.id.profileFrag_options_list);
            // Define options and corresponding icons, based on user type
            List<String> options;
            List<Integer> icons;
            if (user.isAdmin()) {
                options = Arrays.asList("Insert Game", "Logout");
            } else {
                options = Arrays.asList("Update Info", "Logout");
            }
            icons = Arrays.asList(R.drawable.baseline_create, R.drawable.baseline_logout);

            // Set adapter
            ProfileOptionsAdapter adapter = new ProfileOptionsAdapter(requireContext(), options, icons);
            optionsListView.setAdapter(adapter);

            // ========================
            // events (on optionsListView)
            // =========================
            optionsListView.setOnItemClickListener((parent, view1, position, id) -> {
                if (position == 0) {
                    // navigation
                    if (user.isAdmin())
                        Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_addNewGameFragment);
                    else
                        Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_updateDetailsFragment);
                } else if (position == 1) {
                    // logout
                    authRepository.signOutUser();
                    Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_loginFragment);
                }
            });
        }
    }
}








