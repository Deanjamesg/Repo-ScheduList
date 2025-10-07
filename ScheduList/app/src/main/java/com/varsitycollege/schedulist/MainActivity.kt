package com.varsitycollege.schedulist

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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.databinding.ActivityMainBinding
import com.varsitycollege.schedulist.services.CalendarApiClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var auth: FirebaseAuth

    private val TAG : String = "MainActivity: "

    override fun onCreate(savedInstanceState: Bundle?) {
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

        setSupportActionBar(binding.toolBar)

        binding.toolBar.overflowIcon = AppCompatResources.getDrawable(this@MainActivity, R.drawable.ic_more_vert)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavBar.isItemActiveIndicatorEnabled = false

        auth = Firebase.auth

//        lifecycleScope.launch {
//            TestEventInsert()
//        }

    }

    private suspend fun TestEventInsert() {

        Log.d(TAG, "Step 1: Launched Lifecycle Scope")
        val calendarApiClient = CalendarApiClient(this@MainActivity, auth.currentUser!!.email.toString())
        Log.d(TAG, "Step 2: Creating Event")
        val now = System.currentTimeMillis()
        val startDateTime = DateTime(now)
        val endDateTime = DateTime(now + 3600000)
        Log.d(TAG, "Step 3: Calling Insert Event")
        calendarApiClient.insertEvent(
            summary = "My First ScheduList Event",
            description = "This is a test event created automatically from the app.",
            location = "V&A Waterfront, Cape Town",
            startTime = startDateTime,
            endTime = endDateTime
        )
        Log.d(TAG, "Step 4: After Insert Called")

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}