package com.varsitycollege.schedulist.ui.main.account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.schedulist.ui.auth.AuthActivity
import com.varsitycollege.schedulist.databinding.FragmentAccountBinding
import com.varsitycollege.schedulist.ui.auth.GoogleAuthClient
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    lateinit var binding : FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        binding.btnSignOut.setOnClickListener {
            lifecycleScope.launch {
                val googleAuthClient = GoogleAuthClient(requireContext())
                googleAuthClient.signOut()
                val intent = Intent(requireContext(), AuthActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        return binding.root
    }
}