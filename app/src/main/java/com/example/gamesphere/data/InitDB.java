package com.example.gamesphere.data;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.gamesphere.model.Developer;
import com.example.gamesphere.model.Genre;
import com.example.gamesphere.model.Platform;
import com.example.gamesphere.model.Publisher;
import com.example.gamesphere.model.Series;
import com.example.gamesphere.model.User;
import com.example.gamesphere.repository.AuthenticationRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.utils.Constants;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The class is responsible for initialization of all the tables which are required to be initialized on launching.
 * The class is called once, on MainActivity (onCreate)
 */
public class InitDB {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final UserRepository userRepository = new UserRepository();
    private final AuthenticationRepository authRepository = new AuthenticationRepository();

    /**
     * Initializes necessary tables in the Firebase Realtime Database.
     */
    public void initTablesInDataBase()
    {
        // insert admin into Authentication table (if not exist yet)
        initAuthentication().addOnCompleteListener(task -> {
            if (task.isSuccessful()) { // if success
                FirebaseUser firebaseUser = task.getResult(); // get the recently added Authentication user
                initUserTable(firebaseUser); // insert admin into User table
            }
        });

        initPlatformTable();
        initSeriesTable();
        initDeveloperTable();
        initPublisherTable();
        initGenreTable();
    }

    /**
     * Asynchronous function which initializes Authentication table: Inserts a row for admin,
     * @return a task with the FirebaseUser which was created, otherwise- exception.
     */
    private Task<FirebaseUser> initAuthentication()
    {
        // Create a TaskCompletionSource to return the result asynchronously
        TaskCompletionSource<FirebaseUser> taskCompletionSource = new TaskCompletionSource<>();

        // init Authentication only if 'admin' user does not appear on User
        userRepository.checkUsernameExists(Constants.ADMIN_USERNAME)
                .addOnSuccessListener(usernameExists -> { // usernameExists = true if exists
                    if (!usernameExists) {
                        // Create user through Authentication
                        authRepository.createUser(Constants.ADMIN_EMAIL, Constants.ADMIN_PASSWORD)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = task.getResult(); // Successfully created the user
                                        taskCompletionSource.setResult(firebaseUser); // Set result as the FirebaseUser
                                    } else {
                                        // Handle errors during user creation
                                        Exception exception = task.getException();
                                        assert exception != null;
                                        taskCompletionSource.setException(exception); // Set exception
                                    }
                                });
                    }
                })
                .addOnFailureListener(taskCompletionSource::setException);
        return taskCompletionSource.getTask();
    }

    /**
     *  Asynchronous function which initializes User table: Inserts a row for admin.
     * @param firebaseUser  FirebaseUser which was created on Authentication
     */
    private void initUserTable(FirebaseUser firebaseUser)
    {
        DatabaseReference table = database.getReference("User");
        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) // no row exists (meaning: admin does not exist)
                {
                    userRepository.insertUser(firebaseUser, Constants.ADMIN_USERNAME, true,
                            new UserRepository.OnUserInsertedCallback() {
                        @Override
                        public void onSuccess(User user) {
                        }
                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * The function initializes Platform table
     */
    private void initPlatformTable()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference platformsTable = database.getReference("Platform");

        platformsTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Create a list of platformsMap
                    Map<String, Platform> platformsMap=  new HashMap<>();
                    platformsMap.put("1", new Platform(1, "PlayStation 4"));
                    platformsMap.put("2", new Platform(2, "PlayStation 5"));
                    platformsMap.put("3", new Platform(3, "Xbox One"));
                    platformsMap.put("4", new Platform(4, "Xbox Series X/S"));
                    platformsMap.put("5", new Platform(5, "Nintendo Switch"));
                    platformsMap.put("6", new Platform(6, "Microsoft Windows"));
                    platformsMap.put("7", new Platform(7, "MacOS"));
                    platformsMap.put("8", new Platform(8, "iPadOS"));
                    platformsMap.put("9", new Platform(9, "iOS"));
                    platformsMap.put("10", new Platform(10, "Xbox Cloud Gaming"));
                    platformsMap.put("11", new Platform(11, "Playdate"));
                    platformsMap.put("12", new Platform(12, "Linux"));
                    platformsMap.put("13", new Platform(13, "Android"));
                    platformsMap.put("14", new Platform(14, "Luna"));
                    platformsMap.put("15", new Platform(15, "Nvidia GeForce Now"));
                    platformsMap.put("16", new Platform(16, "Windows"));

                    platformsTable.setValue(platformsMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Platforms", "Platforms added successfully.");
                        } else {
                            Log.e("Platforms", "Failed to add platformsMap: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * The function initializes Series table
     */
    private void initSeriesTable()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference seriesTable = database.getReference("Series");

        seriesTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Create a list of platformsMap
                    Map<String, Series> seriesMap=  new HashMap<>();
                    seriesMap.put("1", new Series(1, "God of War"));
                    seriesMap.put("2", new Series(2, "Marvel's Spider-Man"));
                    seriesMap.put("3", new Series(3, "Resident Evil"));
                    seriesMap.put("4", new Series(4, "Horizon"));
                    seriesMap.put("5", new Series(5, "Baldur's Gate"));
                    seriesMap.put("6", new Series(6, "Marvel"));
                    seriesMap.put("7", new Series(7, "The Legend of Zelda"));
                    seriesMap.put("8", new Series(8, "Little Nightmares"));
                    seriesMap.put("9", new Series(9, "Mortal Kombat"));
                    seriesMap.put("10", new Series(10, "Indiana Jones"));
                    seriesMap.put("11", new Series(11, "Final Fantasy"));
                    seriesMap.put("12", new Series(12, "Super Mario"));
                    seriesMap.put("13", new Series(13, "Kirby"));
                    seriesMap.put("14", new Series(14, "Dragon Ball Z: Budokai Tenkaichi"));
                    seriesMap.put("15", new Series(15, "Tekken"));
                    seriesMap.put("16", new Series(16, "WWE 2K"));
                    seriesMap.put("17", new Series(17, "Astro Bot"));
                    seriesMap.put("18", new Series(18, "Star Wars"));
                    seriesMap.put("19", new Series(19, "Top Spin"));
                    seriesMap.put("20", new Series(20, "Persona"));
                    seriesMap.put("21", new Series(21, "Sonic the Hedgehog"));
                    seriesMap.put("22", new Series(22, "Lego Star Wars"));
                    seriesMap.put("23", new Series(23, "Forza"));
                    seriesMap.put("24", new Series(24, "Dead Space"));
                    seriesMap.put("25", new Series(25, "Assassin's Creed"));
                    seriesMap.put("26", new Series(26, "Cyberpunk"));

                    seriesTable.setValue(seriesMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Series", "Series added successfully.");
                        } else {
                            Log.e("Series", "Failed to add seriesMap: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * The function initializes Developer table
     */
    private void initDeveloperTable()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference developerTable = database.getReference("Developer");

        developerTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Create a list of platformsMap
                    Map<String, Developer> developerMap=  new HashMap<>();
                    developerMap.put("1", new Developer(1, "Nintendo EPD"));
                    developerMap.put("2", new Developer(2, "Santa Monica Studio"));
                    developerMap.put("3", new Developer(3, "FromSoftware"));
                    developerMap.put("4", new Developer(4, "Insomniac Games"));
                    developerMap.put("5", new Developer(5, "Capcom"));
                    developerMap.put("6", new Developer(6, "Guerrilla Games"));
                    developerMap.put("7", new Developer(7, "Larian Studios"));
                    developerMap.put("8", new Developer(8, "Supermassive Games"));
                    developerMap.put("9", new Developer(9, "Respawn Entertainment"));
                    developerMap.put("10", new Developer(10, "Eidos-Montréal"));
                    developerMap.put("11", new Developer(11, "Hazelight Studios"));
                    developerMap.put("12", new Developer(12, "Tarsier Studios"));
                    developerMap.put("13", new Developer(13, "NetherRealm Studios"));
                    developerMap.put("14", new Developer(14, "MachineGames"));
                    developerMap.put("15", new Developer(15, "Square Enix Creative Business Unit I"));
                    developerMap.put("16", new Developer(16, "HAL Laboratory"));
                    developerMap.put("17", new Developer(17, "Spike Chunsoft"));
                    developerMap.put("18", new Developer(18, "Bandai Namco Studios Arika"));
                    developerMap.put("19", new Developer(19, "Visual Concepts"));
                    developerMap.put("20", new Developer(20, "Team Asobi"));
                    developerMap.put("21", new Developer(21, "Zynga NaturalMotion"));
                    developerMap.put("22", new Developer(22, "Shift Up"));
                    developerMap.put("23", new Developer(23, "Hangar 13"));
                    developerMap.put("24", new Developer(24, "CD Projekt RED"));
                    developerMap.put("25", new Developer(25, "P-Studio"));
                    developerMap.put("26", new Developer(26, "Sonic Team"));
                    developerMap.put("27", new Developer(27, "Coal Supper"));
                    developerMap.put("28", new Developer(28, "Traveller's Tales"));
                    developerMap.put("29", new Developer(29, "Playground Games"));
                    developerMap.put("30", new Developer(30, "Motive Studio"));
                    developerMap.put("31", new Developer(31, "Ubisoft Quebec"));

                    developerTable.setValue(developerMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Developer", "Developer added successfully.");
                        } else {
                            Log.e("Developer", "Failed to add developerMap: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * The function initializes Publisher table
     */
    private void initPublisherTable()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference publisherTable = database.getReference("Publisher");

        publisherTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Create a list of platformsMap
                    Map<String, Publisher> publisherMap=  new HashMap<>();
                    publisherMap.put("1", new Publisher(1, "Sony Interactive Entertainment"));
                    publisherMap.put("2", new Publisher(2, "Bandai Namco Entertainment"));
                    publisherMap.put("3", new Publisher(3, "Capcom"));
                    publisherMap.put("4", new Publisher(4, "Larian Studios"));
                    publisherMap.put("5", new Publisher(5, "2K"));
                    publisherMap.put("6", new Publisher(6, "Electronic Arts"));
                    publisherMap.put("7", new Publisher(7, "Square Enix"));
                    publisherMap.put("8", new Publisher(8, "Nintendo"));
                    publisherMap.put("9", new Publisher(9, "Warner Bros. Games"));
                    publisherMap.put("10", new Publisher(10, "Bethesda Softworks"));
                    publisherMap.put("11", new Publisher(11, "Zynga"));
                    publisherMap.put("12", new Publisher(12, "CD Projekt"));
                    publisherMap.put("13", new Publisher(13, "Sega JP: Atlus"));
                    publisherMap.put("14", new Publisher(14, "Sega"));
                    publisherMap.put("15", new Publisher(15, "Panic Inc."));
                    publisherMap.put("16", new Publisher(16, "Ubisoft"));
                    publisherMap.put("17", new Publisher(17, "Tencent"));
                    publisherMap.put("18", new Publisher(18, "Microsoft Studios"));
                    publisherMap.put("19", new Publisher(19, "NetEase Games"));
                    publisherMap.put("20", new Publisher(20, "Take-Two Interactive"));
                    publisherMap.put("21", new Publisher(21, "Nexon"));
                    publisherMap.put("22", new Publisher(22, "Amazon Games"));
                    publisherMap.put("23", new Publisher(23, "Oculus Studios"));
                    publisherMap.put("24", new Publisher(24, "Devolver Digital"));
                    publisherMap.put("25", new Publisher(25, "Saber Interactive"));
                    publisherMap.put("26", new Publisher(26, "Annapurna Interactive"));
                    publisherMap.put("27", new Publisher(27, "Scopely"));
                    publisherMap.put("28", new Publisher(28, "Xbox Game Studios"));

                    publisherTable.setValue(publisherMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Publisher", "Publisher added successfully.");
                        } else {
                            Log.e("Publisher", "Failed to add publisherMap: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * The function initializes Genre table
     */
    private void initGenreTable()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference genreTable = database.getReference("Genre");

        genreTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Create a list of platformsMap
                    Map<String, Genre> genreMap=  new HashMap<>();
                    genreMap.put("1", new Genre(1, "Action"));
                    genreMap.put("2", new Genre(2, "Adventure"));
                    genreMap.put("3", new Genre(3, "Fighting"));
                    genreMap.put("4", new Genre(4, "Platform"));
                    genreMap.put("5", new Genre(5, "Puzzle"));
                    genreMap.put("6", new Genre(6, "Racing"));
                    genreMap.put("7", new Genre(7, "Role-playing"));
                    genreMap.put("8", new Genre(8, "Shooter"));
                    genreMap.put("9", new Genre(9, "Simulation"));
                    genreMap.put("10", new Genre(10, "Sports"));
                    genreMap.put("11", new Genre(11, "Strategy"));
                    genreMap.put("12", new Genre(12, "Horror"));
                    genreMap.put("13", new Genre(13, "Interactive drama"));
                    genreMap.put("14", new Genre(14, "Hack and slash"));

                    genreTable.setValue(genreMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Genre", "Genre added successfully.");
                        } else {
                            Log.e("Genre", "Failed to add genreMap: " + Objects.requireNonNull(task.getException()).getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
