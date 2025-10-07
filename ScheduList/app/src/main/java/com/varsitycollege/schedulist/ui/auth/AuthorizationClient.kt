package com.varsitycollege.schedulist.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.tasks.TasksScopes

class AuthorizationClient(private val resultCaller: ActivityResultCaller) {

    private val TAG = "AuthorizationClient"
    private var onAuthSuccess: ((AuthorizationResult) -> Unit)? = null
    private var onAuthFailure: ((Exception) -> Unit)? = null

    private val authorizationLauncher = resultCaller.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val authorizationResult = Identity.getAuthorizationClient(resultCaller as Activity)
                    .getAuthorizationResultFromIntent(result.data!!)
                Log.d(TAG, "User granted authorization successfully.")
                onAuthSuccess?.invoke(authorizationResult)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get authorization result from intent.", e)
                onAuthFailure?.invoke(e)
            }
        } else {
            Log.w(TAG, "Authorization was not granted by user.")
            onAuthFailure?.invoke(Exception("User did not grant authorization."))
        }
    }

    fun requestAuthorization(
        onAuthorizationSuccess: (AuthorizationResult) -> Unit,
        onAuthorizationFailure: (Exception) -> Unit
    ) {
        this.onAuthSuccess = onAuthorizationSuccess
        this.onAuthFailure = onAuthorizationFailure

        val requestedScopes: List<Scope> = listOf(
            Scope(CalendarScopes.CALENDAR),
            Scope(TasksScopes.TASKS)
        )

        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()

        Identity.getAuthorizationClient(resultCaller as Activity)
            .authorize(authorizationRequest)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    Log.d(TAG, "Authorization and user permissions required. Launching intent.")
                    val pendingIntent = authorizationResult.pendingIntent
                    val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent!!.intentSender).build()
                    authorizationLauncher.launch(intentSenderRequest)
                } else {
                    Log.d(TAG, "Authorization already obtained.")
                    onAuthSuccess?.invoke(authorizationResult)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to authorize for Google APIs", e)
                onAuthFailure?.invoke(e)
            }
    }

}