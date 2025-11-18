package com.varsitycollege.schedulist.ui.main.account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.varsitycollege.schedulist.R
import com.varsitycollege.schedulist.ui.auth.AuthActivity
import com.varsitycollege.schedulist.databinding.FragmentAccountBinding
import com.varsitycollege.schedulist.ui.auth.GoogleAuthClient
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var accountViewModel: AccountViewModel

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
                val displayName = user.displayName ?: ""
                val nameParts = displayName.split(" ")
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

                binding.tvNameValue.text = firstName.ifEmpty { getString(R.string.profile_no_name_set) }
                binding.tvSurnameValue.text = lastName.ifEmpty { getString(R.string.profile_no_surname_set) }
                binding.tvEmailValue.text = user.email ?: getString(R.string.profile_no_email_set)
            } else {
                binding.tvNameValue.text = getString(R.string.profile_no_name_set)
                binding.tvSurnameValue.text = getString(R.string.profile_no_surname_set)
                binding.tvEmailValue.text = getString(R.string.profile_no_email_set)
            }
        }
    }
}
