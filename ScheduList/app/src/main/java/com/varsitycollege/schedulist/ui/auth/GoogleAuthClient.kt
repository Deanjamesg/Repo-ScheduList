package com.varsitycollege.schedulist.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import com.varsitycollege.schedulist.BuildConfig
import com.varsitycollege.schedulist.services.ApiClients

class GoogleAuthClient(
    private val context: Context,
) {
    private val TAG = "GoogleAuthClient"
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun isSignedIn() : Boolean {
        if (firebaseAuth.currentUser!= null) {
            return true
        }
        return false
    }

    suspend fun signIn() : Boolean {
        if (isSignedIn()) {
            return true
        }
        try {
            val result = buildCredentialRequest()
            return handleSignIn(result)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            Log.e(TAG, "SignIn Error: ${e.message}")
            return false
        }
    }

    private suspend fun handleSignIn(result : GetCredentialResponse) : Boolean {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken,
                    null
                )
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user != null

            } catch(e: GoogleIdTokenParsingException) {
                Log.e(TAG, "GoogleIdTokenParsingException: ${e.message}")
                return false
            }

        } else {
            Log.d(TAG, "Credential is not GoogleIdTokenCredential")
            return false
        }

    }

    private suspend fun buildCredentialRequest() : GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        BuildConfig.WEB_CLIENT_ID
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(
            request = request, context = context
        )
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        firebaseAuth.signOut()
        ApiClients.clear()
        Log.d(TAG, "User Signed Out.")
    }
}