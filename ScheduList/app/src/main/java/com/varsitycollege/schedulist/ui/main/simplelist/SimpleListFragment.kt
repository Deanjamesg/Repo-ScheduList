package com.varsitycollege.schedulist.ui.main.simplelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.varsitycollege.schedulist.ui.adapter.SimpleListItem
import com.varsitycollege.schedulist.data.repository.SimpleListRepository
import com.varsitycollege.schedulist.databinding.FragmentSimpleListBinding
import com.varsitycollege.schedulist.ui.adapter.SimpleListAdapter

// This is the Fragment for the Simple List screen. It sets up the UI and
// observes the ViewModel for the final list to display.

class SimpleListFragment : Fragment() {

    private var _binding: FragmentSimpleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var simpleListViewModel: SimpleListViewModel
    private lateinit var simpleListAdapter: SimpleListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Set up the Repository, Factory, and ViewModel.
        val repository = SimpleListRepository()
        val factory = SimpleListViewModelFactory(repository)
        simpleListViewModel = ViewModelProvider(this, factory).get(SimpleListViewModel::class.java)

        // 2. Set up the RecyclerView and Adapter.
        setupRecyclerView()

        // 3. Observe the 'combinedList' from the ViewModel.
        // This will run when the data is ready.
        simpleListViewModel.combinedList.observe(viewLifecycleOwner) { combinedList ->
            // We got the sorted list, so we submit it to our adapter.
            simpleListAdapter.submitList(combinedList)
        }
    }

    private fun setupRecyclerView() {
        simpleListAdapter = SimpleListAdapter { item, isChecked ->
            simpleListViewModel.onItemCheckedChanged(item, isChecked)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = simpleListAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}