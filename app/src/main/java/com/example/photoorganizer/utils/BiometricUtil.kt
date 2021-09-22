package com.example.photoorganizer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager

import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricManager.from
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.photoorganizer.R
import com.example.photoorganizer.ext.setDirectoryBiometricLocked
import com.example.photoorganizer.viewmodel.ImagesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class BiometricUtil(private val context: Context) {

    /**
     * Check to see if the android version in device is greater than
     * Marshmallow, since fingerprint authentication is only supported
     * from Android 6.0.
     */
    private val isSdkVersionSupported: Boolean
        @SuppressLint("ObsoleteSdkInt")
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private val isHardwareSupported: Boolean
        get() {
            val biometricManager: BiometricManager = from(context)
            return (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS ) ||
            (biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS )
        }

    val isBiometricSupported: Boolean
        get() {
            return isSdkVersionSupported && isHardwareSupported
        }

    val isFingerprintAvailable: Boolean
        get() {
            val biometricManager: BiometricManager = from(context)
            return (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS) ||
            (biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS)
        }

    fun showBiometricsNotSetupAlert() {
        MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCancelable(true)
            .setTitle(context.getString(R.string.biometrics_not_setup_title))
            .setMessage(context.getString(R.string.biometrics_not_setup_msg))
            .setPositiveButton(context.getString(R.string.proceed_text)) { dialog, _ ->
                context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    fun showBiometricPrompt(vm: ImagesViewModel, dir: File, type: PromptType) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            (context as FragmentActivity?)!!,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    when (type) {
                        PromptType.SETUP -> {
                            dir.setDirectoryBiometricLocked(context, true)
                            vm.refreshFiles()
                        }
                        PromptType.UNLOCK -> {
                            vm.setRootDir(dir)
                            vm.refreshFiles()
                        }
                        PromptType.DELETE -> {
                            dir.setDirectoryBiometricLocked(context, false)
                            vm.refreshFiles()
                        }
                    }
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_LOCKOUT || errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT) {
                        Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                    }
                    super.onAuthenticationError(errorCode, errString)
                }
            })
        biometricPrompt.authenticate(createPromptBuilder(context, type))
    }

    private fun createPromptBuilder(context: Context, type: PromptType): PromptInfo {
        return when (type) {
            PromptType.SETUP -> {
                PromptInfo.Builder()
                    .setTitle(context.getString(R.string.biometric_prompt_title))
                    .setSubtitle(context.getString(R.string.biometric_setup_prompt_sub_title))
                    .setNegativeButtonText(context.getString(R.string.cancel_text))
                    .build()
            }
            PromptType.UNLOCK -> {
                PromptInfo.Builder()
                    .setTitle(context.getString(R.string.biometric_prompt_title))
                    .setSubtitle(context.getString(R.string.biometric_unlock_prompt_sub_title))
                    .setNegativeButtonText(context.getString(R.string.cancel_text))
                    .build()
            }
            PromptType.DELETE -> {
                PromptInfo.Builder()
                    .setTitle(context.getString(R.string.biometric_prompt_title))
                    .setSubtitle(context.getString(R.string.biometric_delete_prompt_sub_title))
                    .setNegativeButtonText(context.getString(R.string.cancel_text))
                    .build()
            }
        }
    }

}