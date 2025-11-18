package com.varsitycollege.schedulist.biometrics

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    private val TAG = "BiometricHelper"

    fun isBiometricAvailable(): BiometricStatus {
        val biometricManager = BiometricManager.from(activity)
        val code = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        Log.d(TAG, "canAuthenticate result=$code")
        return when (code) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometrics available")
                BiometricStatus.AVAILABLE
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.w(TAG, "No biometric hardware")
                BiometricStatus.NO_HARDWARE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.w(TAG, "Biometric hardware unavailable")
                BiometricStatus.UNAVAILABLE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.w(TAG, "No biometrics enrolled")
                BiometricStatus.NOT_ENROLLED
            }
            else -> {
                Log.w(TAG, "Unknown biometric status: $code")
                BiometricStatus.UNAVAILABLE
            }
        }
    }

    fun authenticate(
        title: String = "Authenticate",
        subtitle: String = "Verify your identity",
        negativeButtonText: String = "Sign Out",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit,
        onFailed: () -> Unit
    ) {
        Log.d(TAG, "Starting biometric authentication (title='$title')")

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication succeeded")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Authentication error ($errorCode): $errString")
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Authentication failed attempt")
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        Log.d(TAG, "Showing biometric prompt")
        biometricPrompt.authenticate(promptInfo)
    }

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        UNAVAILABLE,
        NOT_ENROLLED
    }
}
