package com.varsitycollege.schedulist.ui.main.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.databinding.FragmentTasksBinding
import com.varsitycollege.schedulist.ui.adapter.MonthGridAdapter
import com.varsitycollege.schedulist.ui.adapter.TasksAdapter
import com.varsitycollege.schedulist.R
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.services.TasksApiClient
import kotlinx.coroutines.launch
import java.util.Calendar


class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var tasksApiClient : TasksApiClient
    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var monthAdapter: MonthGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        tasksApiClient = TasksApiClient(requireContext(), auth.currentUser!!.email.toString())

        return binding.root
    }

    private fun setupAddButtonListener() {
        val addTaskOverlay = binding.root.findViewById<FrameLayout>(R.id.addTaskOverlay)
        val inflater = LayoutInflater.from(requireContext())
        var addTaskView: View? = null

        binding.btnNewTask.setOnClickListener {
            addTaskOverlay.removeAllViews()
            addTaskView = inflater.inflate(R.layout.fragment_add_task, addTaskOverlay, false)
            addTaskOverlay.addView(addTaskView)

            // Get references to all the views inside the pop-up
            val tilTitle = addTaskView!!.findViewById<TextInputLayout>(R.id.tilTitle)
            val tilDescription = addTaskView!!.findViewById<TextInputLayout>(R.id.tilDescription)
            val etTitle = tilTitle.editText
            val etDescription = tilDescription.editText

            // Date & Time Picker setup...
            val btnDatePicker = addTaskView!!.findViewById<MaterialButton>(R.id.btnDatePicker)
            val btnTimePicker = addTaskView!!.findViewById<MaterialButton>(R.id.btnTimePicker)
            val calendar = Calendar.getInstance()
            // ... (rest of your date/time picker logic)

            val btnCancel = addTaskView!!.findViewById<MaterialButton>(R.id.btnCancel)
            val btnSave = addTaskView!!.findViewById<MaterialButton>(R.id.btnSaveTask)

            btnCancel.setOnClickListener {
                addTaskOverlay.visibility = View.GONE
            }

            btnSave.setOnClickListener {
                // Get user input as separate variables.
                val title = etTitle?.text.toString().trim()
                val description = etDescription?.text.toString().trim()
                val dueDate = calendar.time

                if (title.isNotEmpty()) {
                    // Call the ViewModel without the energy level.
//                    tasksViewModel.addTask(title, description, dueDate)
                    addTaskOverlay.visibility = View.GONE
                } else {
                    etTitle?.error = "Title cannot be empty"
                }
            }
            addTaskOverlay.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = TasksRepository(tasksApiClient)
        val factory = TasksViewModelFactory(repository)
        tasksViewModel = ViewModelProvider(this, factory).get(TasksViewModel::class.java)

        setupRecyclerViewAndAdapters()
        setupSpinnerListener()
        setupObservers() // Moved the observers to their own function.

        // Start loading the task data.
        lifecycleScope.launch {
            tasksViewModel.startListeningForTasks()
        }

    }

    // This function sets up both our adapters.
    private fun setupRecyclerViewAndAdapters() {
        tasksAdapter = TasksAdapter { task ->
            // tasksViewModel.onTaskCheckedChanged(task)
        }
        monthAdapter = MonthGridAdapter()
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tasksRecyclerView.adapter = tasksAdapter // Start with the default adapter
    }

    private fun setupSpinnerListener() {
        binding.spinnerViewType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedView = parent?.getItemAtPosition(position).toString()

                    if (selectedView == "Month") {
                        // If Month is selected, we switch to a 7-column grid and use the month adapter.
                        binding.tasksRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 7)
                        binding.tasksRecyclerView.adapter = monthAdapter
                    } else {
                        // For Day or Week, we use a standard vertical list and our main tasks adapter.
                        binding.tasksRecyclerView.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.tasksRecyclerView.adapter = tasksAdapter
                        tasksViewModel.setViewType(selectedView)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    // This function sets up the observers for our LiveData.
    private fun setupObservers() {
        // This observer watches the list for our Day/Week views.
        tasksViewModel.displayList.observe(viewLifecycleOwner) { taskList ->
            // We only submit to the tasksAdapter if it's the one currently in use.
            if (binding.tasksRecyclerView.adapter is TasksAdapter) {
                tasksAdapter.submitList(taskList)
            }
        }

        // This is the new observer that watches the list for our Month/Calendar view.
        tasksViewModel.monthList.observe(viewLifecycleOwner) { monthDayList ->
            // We only submit to the monthAdapter if it's the one currently in use.
            if (binding.tasksRecyclerView.adapter is MonthGridAdapter) {
                monthAdapter.submitList(monthDayList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
