package com.varsitycollege.schedulist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.api.client.util.DateTime
import com.google.api.services.tasks.model.TaskList
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.databinding.ActivityMainBinding
import com.varsitycollege.schedulist.services.ApiClients
import com.varsitycollege.schedulist.services.CalendarApiClient
import com.varsitycollege.schedulist.services.TasksApiClient
import com.varsitycollege.schedulist.ui.auth.AuthActivity
import kotlinx.coroutines.launch
import com.varsitycollege.schedulist.data.preferences.SettingsPreferencesManager

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var tasksApi : TasksApiClient
    private lateinit var calendarApi: CalendarApiClient

    private val TAG : String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsManager = SettingsPreferencesManager(this)
        settingsManager.applyLanguage(this)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(1))

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        auth = Firebase.auth

        // Initialize API clients if needed (handles app restart)
        auth.currentUser?.let { user ->
            if (ApiClients.calendarApi == null) {
                ApiClients.initialize(this, user.email!!)
            }
        } ?: run {
            // No user logged in, redirect to auth
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setSupportActionBar(binding.toolBar)

        binding.toolBar.overflowIcon = AppCompatResources.getDrawable(this@MainActivity, R.drawable.ic_more_vert)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect BottomNavigationView with NavController for navigation
        androidx.navigation.ui.NavigationUI.setupWithNavController(binding.bottomNavBar, navController)

        binding.bottomNavBar.isItemActiveIndicatorEnabled = false

    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}