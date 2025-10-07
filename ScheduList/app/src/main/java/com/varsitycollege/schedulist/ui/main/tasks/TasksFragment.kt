package com.varsitycollege.schedulist.ui.main.tasks

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.varsitycollege.schedulist.R
import androidx.lifecycle.Observer
import com.varsitycollege.schedulist.ui.main.simplelist.TasksViewModel

class TasksFragment : Fragment() {

    companion object {
        fun newInstance() = TasksFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
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