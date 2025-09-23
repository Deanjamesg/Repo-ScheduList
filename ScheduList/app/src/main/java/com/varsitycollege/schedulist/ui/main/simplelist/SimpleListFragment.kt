package com.varsitycollege.schedulist.ui.main.simplelist

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.varsitycollege.schedulist.R

class SimpleListFragment : Fragment() {

    companion object {
        fun newInstance() = SimpleListFragment()
    }

    private val viewModel: SimpleListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_simple_list, container, false)
    }
}