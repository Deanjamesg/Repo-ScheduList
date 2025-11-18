package com.varsitycollege.schedulist.ui.main.tasks

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.data.repository.TasksRepository
import com.varsitycollege.schedulist.databinding.FragmentTasksBinding
import com.varsitycollege.schedulist.services.TasksApiClient
import com.varsitycollege.schedulist.ui.adapter.MonthDay
import com.varsitycollege.schedulist.ui.adapter.MonthGridAdapter
import com.varsitycollege.schedulist.ui.adapter.SimpleListAdapter
import com.varsitycollege.schedulist.ui.adapter.SimpleListItem
import com.varsitycollege.schedulist.ui.adapter.TasksAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var tasksApiClient: TasksApiClient
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

            val tilTitle = addTaskView!!.findViewById<TextInputLayout>(R.id.tilTitle)
            val tilDescription = addTaskView!!.findViewById<TextInputLayout>(R.id.tilDescription)
            val etTitle = tilTitle.editText
            val etDescription = tilDescription.editText

            val btnDatePicker = addTaskView!!.findViewById<MaterialButton>(R.id.btnDatePicker)
            val btnTimePicker = addTaskView!!.findViewById<MaterialButton>(R.id.btnTimePicker)
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
                        btnTimePicker.text = timeFormat.format(calendar.time)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

            val btnCancel = addTaskView!!.findViewById<MaterialButton>(R.id.btnCancel)
            val btnSave = addTaskView!!.findViewById<MaterialButton>(R.id.btnSaveTask)

            btnCancel.setOnClickListener {
                addTaskOverlay.visibility = View.GONE
            }

            btnSave.setOnClickListener {
                val title = etTitle?.text.toString().trim()
                val description = etDescription?.text.toString().trim()
                val dueDate = calendar.time

                if (title.isNotEmpty()) {
                    tasksViewModel.addTask(title, description, dueDate)
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
        setupObservers()
        setupAddButtonListener()

        // Passing the userId here to fix the error
        lifecycleScope.launch {
            tasksViewModel.startListeningForTasks(auth.currentUser!!.uid)
        }
    }

    private fun setupRecyclerViewAndAdapters() {
        tasksAdapter = TasksAdapter { task ->
            // Checkbox logic
        }

        // Passing the showDayDetailsPopup function to the adapter so it knows what to do on click
        monthAdapter = MonthGridAdapter { monthDay ->
            showDayDetailsPopup(monthDay)
        }

        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tasksRecyclerView.adapter = tasksAdapter
    }

    // This is the function that shows the popup when you click a day
    private fun showDayDetailsPopup(day: MonthDay) {
        if (day.dayOfMonth.isEmpty()) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_day_details, null)

        val tvDateTitle = dialogView.findViewById<TextView>(R.id.tvDateTitle)
        val rvDayTasks = dialogView.findViewById<RecyclerView>(R.id.rvDayTasks)
        val btnClose = dialogView.findViewById<MaterialButton>(R.id.btnClose)

        tvDateTitle.text = "Day ${day.dayOfMonth}"

        // Using the simple list adapter to show the tasks inside the popup
        val dayListAdapter = SimpleListAdapter { _, _ -> }

        rvDayTasks.layoutManager = LinearLayoutManager(requireContext())
        rvDayTasks.adapter = dayListAdapter

        // Converting tasks to simple items for the list
        val simpleListItems = day.tasks.map { task ->
            SimpleListItem(
                id = task.id ?: "",
                title = task.title,
                date = task.dueDate,
                isCompleted = task.isCompleted
            )
        }
        dayListAdapter.submitList(simpleListItems)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
                        binding.tasksRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 7)
                        binding.tasksRecyclerView.adapter = monthAdapter
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