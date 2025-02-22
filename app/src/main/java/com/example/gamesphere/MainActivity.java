package com.example.gamesphere;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.gamesphere.data.InitDB;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Objects;

/**
 * The class is the main activity of GameSphere App.
 * Corresponding content view: activity_main.xml
 * Additional views:
 *      menu.xml (for bottomNavigation display)
 */
public class MainActivity extends AppCompatActivity {

    private final AuthenticationRepository authRepository = new AuthenticationRepository();
    private final InitDB initDB = new InitDB();
    private BottomNavigationView bottomNavigationView;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // =============================
        // Bottom Navigation Bar
        // =============================
        bottomNavigationView = findViewById(R.id.my_menu);
        initializeBottomNavigation();

        // =============================
        // Top Toolbar
        // =============================
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // each fragment will turn this option to true or false: (on onCreateView)
        // true- if the fragment allows "back" (result: the arrow will appear)
        // otherwise- false. (result: the arrow will disappear)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back); // Custom white arrow

        // =============================
        // Tables initialization
        // =============================
        initDB.initTablesInDataBase();
    }

    /**
     * The function is called when clicking on back arrow on Toolbar,
     *  meaning- we only need to navigate back.
     */
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    /**
     * The function initializes navigation directions on bottom navbar
     */
    private void initializeBottomNavigation()
    {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            NavController navController = Navigation.findNavController(this, R.id.fragmentContainerViewDefault);
            int itemId = item.getItemId(); // get the selected item

            // Explicitly navigate and add to back stack (as a result of calling 'navigate')
            if (itemId == R.id.menu_profile_item) {
                if (authRepository.getLoggedInUser() != null) {
                    navController.navigate(R.id.profileFragment);
                } else {
                    navController.navigate(R.id.loginFragment);
                }
                return true;
            } else if (itemId == R.id.menu_home_item) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.menu_favorite_item) {
                if (authRepository.getLoggedInUser() != null) {
                    navController.navigate(R.id.favoritesFragment);
                } else {
                    navController.navigate(R.id.loginFragment);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * The function gives access to bottomNavigationView, if needed
     * (for example: we need to display other selected item on the menu)
     * @return bottom navigation view object
     */
    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }
}