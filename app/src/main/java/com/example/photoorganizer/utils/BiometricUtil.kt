package com.example.photoorganizer.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.biometric.BiometricManager

import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricManager.from
import com.example.photoorganizer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        val biometricSetupIntent: Intent
        val alertMessage: String
        /**
         * Must check version as we don't have ACTION_FINGERPRINT_ENROLL
         * in Android prior to 9.0 and will use ACTION_SECURITY_SETTINGS.
         * Also will show different alert message for versions older than 9.0
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            alertMessage = "Setup Biometrics"
            biometricSetupIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
        } else {
            alertMessage = "Setup Biometrics"
            biometricSetupIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        MaterialAlertDialogBuilder(
            context,
            com.example.photoorganizer.R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCancelable(true)
            .setTitle("Title ")
            .setMessage(alertMessage)
            .setPositiveButton(context.getString(R.string.confirm_text)) { dialog, which ->
                context.startActivity(biometricSetupIntent)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

}