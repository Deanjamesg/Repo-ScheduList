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
    lateinit var accountViewModel: AccountViewModel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountViewModel = AccountViewModel()
        accountViewModel.loadUserData()
        accountViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Split displayName into first and last name
                val nameParts = user.displayName?.split(" ") ?: listOf("")
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else ""
                binding.tvNameValue.text = firstName
                binding.tvSurnameValue.text = lastName
                binding.tvEmailValue.text = user.email ?: ""
            } else {
                binding.tvNameValue.text = ""
                binding.tvSurnameValue.text = ""
                binding.tvEmailValue.text = ""
            }
        }
    }
}