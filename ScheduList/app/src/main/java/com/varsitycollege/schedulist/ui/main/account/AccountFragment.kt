package com.varsitycollege.schedulist.ui.main.account

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.databinding.FragmentAccountBinding
import com.varsitycollege.schedulist.ui.auth.AuthActivity

class AccountFragment : Fragment() {

    lateinit var binding : FragmentAccountBinding
    lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        binding.btnSignOut.setOnClickListener {

        }

        return binding.root
    }
}