package com.varsitycollege.schedulist.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.varsitycollege.schedulist.data.model.User
import com.varsitycollege.schedulist.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    lateinit var binding : FragmentLoginBinding

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickListener {

            val user = User(
                userEmail = binding.etEmail.text.toString(),
                userPassword = binding.etPassword.text.toString()
            )

            db.collection("users").add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firebase Database", "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(requireContext(), "You have Successfully Signed In!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Log.w("Firebase Database", "Error adding document", e)
                    Toast.makeText(requireContext(), "Error, Failed Inserting User.", Toast.LENGTH_LONG).show()
                }

//            val direction = LoginFragmentDirections.actionLoginFragmentToGetStartFragment()
//            findNavController().navigate(direction)
        }

        return binding.root
    }
}