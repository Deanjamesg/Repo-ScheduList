package com.varsitycollege.schedulist.ui.main.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import com.varsitycollege.schedulist.ui.main.simplelist.TasksViewModel

// This is our TasksFragment. It's in charge of the UI.
// It sets up the views, listens for user input (like spinner clicks),
// and observes the ViewModel to update the list.

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var monthAdapter: MonthGridAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // First, we set up our Repository, Factory, and ViewModel.
        val repository = TasksRepository()
        val factory = TasksViewModelFactory(repository)
        tasksViewModel = ViewModelProvider(this, factory).get(TasksViewModel::class.java)

        // Then we set up our adapters and the default RecyclerView layout.
        setupRecyclerView()

        // We listen for when the user selects a different view type.
        setupSpinnerListener()

        // We start observing the 'displayList' from the ViewModel. This code
        // will now run automatically whenever the data changes in the ViewModel.
        tasksViewModel.displayList.observe(viewLifecycleOwner) { taskList ->
            // When we get the new list, we give it to our adapter to display.
            tasksAdapter.submitList(taskList)
        }

        // Finally, we tell the ViewModel to start loading the task data.
        tasksViewModel.startListeningForTasks("sampleUserId")
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter()
        monthAdapter = MonthGridAdapter()
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tasksRecyclerView.adapter = tasksAdapter
    }

    private fun setupSpinnerListener() {
        binding.spinnerViewType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedView = parent?.getItemAtPosition(position).toString()

                if (selectedView == "Month") {
                    binding.tasksRecyclerView.layoutManager = GridLayoutManager(context, 7)
                    binding.tasksRecyclerView.adapter = monthAdapter
                    // TODO: Tell ViewModel to format data for the month adapter.
                } else {
                    binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.tasksRecyclerView.adapter = tasksAdapter
                    // Tell the ViewModel which view to prepare the data for.
                    tasksViewModel.setViewType(selectedView)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spinnerTaskList = view.findViewById<android.widget.Spinner>(R.id.spinnerTaskList)
        val spinnerViewType = view.findViewById<android.widget.Spinner>(R.id.spinnerViewType)

        val viewTypeItems = listOf("List View", "Grid View")
        val viewTypeAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewTypeItems)
        viewTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerViewType.adapter = viewTypeAdapter

        // Adapter for task list, initially empty
        val taskListAdapter = android.widget.ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, mutableListOf())
        taskListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaskList.adapter = taskListAdapter

        // Observe the ViewModel's task list names
        viewModel.taskListNames.observe(viewLifecycleOwner, Observer { newList ->
            taskListAdapter.clear()
            taskListAdapter.addAll(newList)
            taskListAdapter.notifyDataSetChanged()
        })
    }
}