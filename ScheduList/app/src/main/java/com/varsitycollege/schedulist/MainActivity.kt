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
import com.google.api.services.tasks.model.TaskList
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.databinding.ActivityMainBinding
import com.varsitycollege.schedulist.services.CalendarApiClient
import com.varsitycollege.schedulist.services.TasksApiClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var tasksApi : TasksApiClient
    private lateinit var calendarApi: CalendarApiClient

    private val TAG : String = "MainActivity"

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

        auth = Firebase.auth

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

        tasksApi = TasksApiClient(this@MainActivity, auth.currentUser!!.email.toString())

        calendarApi = CalendarApiClient(this@MainActivity, auth.currentUser!!.email.toString())

//        lifecycleScope.launch {
//            testTaskListInsert()
//            testEventInsert()
//            testGetAllTaskLists()
//        }

    }

    private suspend fun testTaskListInsert() {

        Log.d(TAG, "Step 1: Launched Lifecycle Scope")
        Log.d(TAG, "Step 2: Calling Tasks API Client")
        val taskList = tasksApi.insertTaskList("ScheduList Tasks")
        Log.d(TAG, "Step 3: After Insert Called")

        testInsertTaskIntoList(taskList!!)

    }

    private suspend fun testGetAllTaskLists() {
        val list = tasksApi.getAllTaskLists()
        list.forEach { item ->
            Log.d(TAG, "${item.title}")
            var tasks = tasksApi.getAllTasksFromList(item.id)
            tasks.forEach { task ->
                Log.d(TAG, "${task.title}")
                Log.d(TAG, "${task.status}")
                tasksApi.deleteTask(item.id, task.id )
            }
        }

    }

    private suspend fun testInsertTaskIntoList(scheduList : TaskList) {
        tasksApi.insertTask(
            taskListId = scheduList.id,
            title = "Complete ScheduList setup",
            notes = "This is a sample task added during initial sign-in."
        )
    }

    private suspend fun testEventInsert() {

        Log.d(TAG, "Step 1: Insert Event Function Called")
        val calendarId = calendarApi.getOrInsertScheduListCalendar()
        val now = System.currentTimeMillis()
        val startDateTime = DateTime(now)
        val endDateTime = DateTime(now + 3600000)

        Log.d(TAG, "Step 2: Calling Insert Event via Api Client")

        calendarApi.insertEvent(
            calendarId = calendarId?.id.toString(),
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