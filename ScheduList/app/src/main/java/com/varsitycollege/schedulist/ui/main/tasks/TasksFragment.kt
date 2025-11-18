package com.varsitycollege.schedulist.ui.main.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.model.TaskList
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.databinding.FragmentTasksBinding
import com.varsitycollege.schedulist.services.ApiClients
import com.varsitycollege.schedulist.ui.adapter.MonthGridAdapter
import com.varsitycollege.schedulist.ui.adapter.TasksAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var monthAdapter: MonthGridAdapter

    private var taskLists = listOf<TaskList>()
    private var selectedTaskList: TaskList? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewModel
        val repository = TasksRepository()
        val factory = TasksViewModelFactory(repository)
        tasksViewModel = ViewModelProvider(this, factory).get(TasksViewModel::class.java)

        setupRecyclerViewAndAdapters()
        setupViewTypeSpinner()
        setupTaskListFilter()
        setupAddButtonListener()
        setupObservers()

        lifecycleScope.launch {
            delay(3000) // Wait 3 seconds

            Log.d("TasksFragment", "=== TESTING TASKS API ===")

            try {
                // Test if we can get task lists
                val taskLists = ApiClients.tasksApi?.getAllTaskLists()
                Log.d("TasksFragment", "Found ${taskLists?.size ?: 0} task lists from Google")
                taskLists?.forEach { list ->
                    Log.d("TasksFragment", "  - ${list.title} (ID: ${list.id})")

                    // Try to get tasks from this list
                    val tasks = ApiClients.tasksApi?.getAllTasksFromList(list.id)
                    Log.d("TasksFragment", "    Tasks in list: ${tasks?.size ?: 0}")
                    tasks?.forEach { task ->
                        Log.d("TasksFragment", "      • ${task.title}")
                    }
                }
            } catch (e: Exception) {
                Log.e("TasksFragment", "Error testing API", e)
            }

            Log.d("TasksFragment", "=== END TEST ===")
        }
    }

    private fun setupRecyclerViewAndAdapters() {
        tasksAdapter = TasksAdapter { task ->
            Log.d("TasksFragment", "Task checked callback received: ${task.title}")
            Log.d("TasksFragment", "Task ID: ${task.id}")
            Log.d("TasksFragment", "Current completion state: ${task.isCompleted}")

            tasksViewModel.toggleTaskCompletion(task.id)
        }
        monthAdapter = MonthGridAdapter()
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tasksRecyclerView.adapter = tasksAdapter
    }

    private fun setupViewTypeSpinner() {
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
                        binding.tasksRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 7)
                        binding.tasksRecyclerView.adapter = monthAdapter
                        tasksViewModel.setViewType("Month")
                    } else {
                        binding.tasksRecyclerView.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.tasksRecyclerView.adapter = tasksAdapter
                        tasksViewModel.setViewType(selectedView)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupTaskListFilter() {
        // Observe task lists for the filter spinner
        tasksViewModel.taskLists.observe(viewLifecycleOwner) { lists ->
            Log.d("TasksFragment", "Task lists updated: ${lists.size} lists")
            taskLists = lists
            updateTaskListSpinner(lists)
        }

        binding.spinnerTaskListFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.d("TasksFragment", "Task list selected at position: $position")

                    if (position == 0) {
                        // "All Tasks" selected
                        selectedTaskList = null
                        tasksViewModel.filterByTaskList(null)
                    } else {
                        // Specific task list selected
                        selectedTaskList = taskLists[position - 1]
                        tasksViewModel.filterByTaskList(selectedTaskList?.id)
                        Log.d("TasksFragment", "Filtering by task list: ${selectedTaskList?.name}")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.d("TasksFragment", "No task list selected")
                }
            }
    }

    private fun updateTaskListSpinner(lists: List<TaskList>) {
        Log.d("TasksFragment", "Updating task list spinner with ${lists.size} lists")

        val taskListNames = mutableListOf("All Tasks")
        taskListNames.addAll(lists.map { it.name })

        Log.d("TasksFragment", "Spinner items: $taskListNames")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskListNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTaskListFilter.adapter = adapter

        Log.d("TasksFragment", "Spinner adapter set with ${adapter.count} items")
    }

    private fun setupAddButtonListener() {
        val addTaskOverlay = binding.root.findViewById<FrameLayout>(R.id.addTaskOverlay)

        binding.btnNewTask.setOnClickListener {
            showAddTaskOverlay(addTaskOverlay)
        }

        binding.btnNewList.setOnClickListener {
            showCreateTaskListOverlay(addTaskOverlay)
        }
    }

    private fun showCreateTaskListOverlay(overlay: FrameLayout) {
        overlay.removeAllViews()
        val createListView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_create_task_list, overlay, false)
        overlay.addView(createListView)

        Log.d("TasksFragment", "ShowCreateTaskListOverlay")

        // Get references
        val tilNewTaskListName = createListView.findViewById<TextInputLayout>(R.id.tilNewTaskListName)
        val etTaskListName = tilNewTaskListName.editText
        val btnCancelNewList = createListView.findViewById<MaterialButton>(R.id.btnCancelNewList)
        val btnSaveNewList = createListView.findViewById<MaterialButton>(R.id.btnSaveNewList)

        btnCancelNewList.setOnClickListener {
            overlay.visibility = View.GONE
        }

        btnSaveNewList.setOnClickListener {
            val name = etTaskListName?.text.toString().trim()

            if (name.isEmpty()) {
                tilNewTaskListName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            tasksViewModel.addTaskList(name)
            overlay.visibility = View.GONE
        }

        overlay.visibility = View.VISIBLE
    }

    private fun showAddTaskOverlay(overlay: FrameLayout) {
        overlay.removeAllViews()
        val addTaskView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_add_task, overlay, false)
        overlay.addView(addTaskView)

        Log.d("TasksFragment", "ShowAddTaskOverlay")

        // Get references
        val tilTitle = addTaskView.findViewById<TextInputLayout>(R.id.tilTitle)
        val tilDescription = addTaskView.findViewById<TextInputLayout>(R.id.tilDescription)
        val etTitle = tilTitle.editText
        val etDescription = tilDescription.editText
        val spinnerTaskList = addTaskView.findViewById<Spinner>(R.id.spinnerTaskList)

        // Setup task list spinner
        val taskListNames = taskLists.map { it.name }
        val taskListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskListNames
        )
        taskListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaskList.adapter = taskListAdapter

        // Pre-select current filter
        selectedTaskList?.let { currentList ->
            val index = taskLists.indexOfFirst { it.id == currentList.id }
            if (index >= 0) {
                spinnerTaskList.setSelection(index)
            }
        }

        // Date & Time Picker
        val btnDatePicker = addTaskView.findViewById<MaterialButton>(R.id.btnDatePicker)
        val btnTimePicker = addTaskView.findViewById<MaterialButton>(R.id.btnTimePicker)
        val calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        btnDatePicker.text = dateFormat.format(calendar.time)
        btnTimePicker.text = timeFormat.format(calendar.time)

        btnDatePicker.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    btnDatePicker.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTimePicker.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    btnTimePicker.text = timeFormat.format(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }

        // "Create New List" button - switches overlay
        val btnCreateNewList = addTaskView.findViewById<MaterialButton>(R.id.btnCreateNewListOnNewTaskDialog)
        btnCreateNewList.setOnClickListener {
            // Close add task overlay and show create list overlay
            showCreateTaskListOverlay(overlay)
        }

        // Cancel & Save buttons
        val btnCancel = addTaskView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = addTaskView.findViewById<MaterialButton>(R.id.btnSaveTask)

        btnCancel.setOnClickListener {
            overlay.visibility = View.GONE
        }

        btnSave.setOnClickListener {
            val title = etTitle?.text.toString().trim()
            val description = etDescription?.text.toString().trim()
            val dueDate = calendar.time
            val selectedTaskListIndex = spinnerTaskList.selectedItemPosition

            if (title.isEmpty()) {
                tilTitle.error = "Title cannot be empty"
                return@setOnClickListener
            }

            if (selectedTaskListIndex < 0) {
                return@setOnClickListener
            }

            val taskListId = taskLists[selectedTaskListIndex].id

            tasksViewModel.addTask(title, description, dueDate, taskListId)
            overlay.visibility = View.GONE
        }

        overlay.visibility = View.VISIBLE
    }

    private fun setupObservers() {
        tasksViewModel.displayList.observe(viewLifecycleOwner) { taskList ->
            if (binding.tasksRecyclerView.adapter is TasksAdapter) {
                tasksAdapter.submitList(taskList)
            }
        }

        tasksViewModel.monthList.observe(viewLifecycleOwner) { monthDayList ->
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