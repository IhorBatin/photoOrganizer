package com.example.photoorganizer.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.example.photoorganizer.R
import com.example.photoorganizer.adapters.CustomImageAdapter
import com.example.photoorganizer.adapters.ScreenSlidePagerAdapter
import com.example.photoorganizer.ext.*
import com.example.photoorganizer.repository.ImagesRepository
import com.example.photoorganizer.viewmodel.ImagesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

open class FileUtil(private val activity: Activity, private val context: Context) {

    lateinit var currentPhotoPath: String

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(dir: File?): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssss").format(Date())
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            dir /* directory */
        ).apply {
            // Save a file: path for use with intents
            currentPhotoPath = absolutePath
            //Timber.tag(DEBUG_TAG).d("new photoPath -> $currentPhotoPath")
        }
    }

    private fun createNewDirectory(name: String, dir: File?): File {
        return File(
            "${dir?.path}${File.separator}$name"
        ).apply {
            Timber.tag(DEBUG_TAG).d("Creating new directory: '$name'")
            mkdir()
            Timber.tag(DEBUG_TAG).d("New folder: '${this.path}' -> created")
        }
    }

    private fun doesFileAlreadyExists(neFileName: String, dir: File?) : Boolean{
        ImagesRepository.fetchAllFilesByDate(dir).also {
            it?.forEach { file ->
                if (file.name.equals(neFileName, ignoreCase = true)) return true
            }
        }
        return false
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun dispatchTakePictureIntent(dir: File?) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(dir)
                } catch (ex: IOException) {
                    Timber.tag(DEBUG_TAG).e("Exception Caught ${ex.message}")
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context,
                        context.packageName,
                        it
                    )
                    Timber.tag(DEBUG_TAG).d("Starting Picture Intent")
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    //takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 2) does not seem to work,
                    // the last camera that was open in default app will be open here too until change in default app
                    startActivityForResult(activity, takePictureIntent, REQUEST_IMAGE_CAPTURE, null)
                }
            }
        }
    }

    fun dispatchImportImagesIntent() {
        Intent(
            //Intent.ACTION_GET_CONTENT,
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        ).also { importImagesIntent ->
            importImagesIntent.type = "image/*"
            importImagesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(activity, importImagesIntent, REQUEST_IMAGE_IMPORT, null)
        }
    }

    fun dispatchShareImageIntent(image: File) {
        val uri = FileProvider.getUriForFile(context, context.packageName, image)
        val intent = ShareCompat.IntentBuilder.from(activity)
            .setStream(uri) // uri from FileProvider
            .setType("image/jpeg")
            .intent
            .setAction(Intent.ACTION_SEND) //Change if needed
            .setDataAndType(uri, "image/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // Using chooser instead of regular intent here as different ways of
        // sharing images will most likely be used.
        val chooser: Intent = Intent.createChooser(intent, context.getString(R.string.chooser_title))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Flag so intent can be launched from util class
        context.startActivity(chooser)
    }

    fun deleteImageFromImageAdapter(pos: Int, vm: ImagesViewModel, adapter: CustomImageAdapter) {
        vm.filesListLiveData.value?.get(pos)?.delete()
        adapter.notifyItemRemoved(pos)
        vm.refreshFiles()
    }

    fun deleteImageFromSliderAdapter(pos: Int, vm: ImagesViewModel, adapter: ScreenSlidePagerAdapter) {
        vm.imagesListLiveData.value?.get(pos)?.delete()
        adapter.notifyItemRemoved(pos)
        vm.refreshFiles()
    }

    fun readBytesFromUri(uri: Uri): ByteArray? =
        activity.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }


    @SuppressLint("InflateParams")
    fun showNewFolderAlert(vm: ImagesViewModel, dir: File?) {
        val layoutInflater = LayoutInflater.from(context)
        val customView: View = layoutInflater.inflate(R.layout.edit_text_custom_alert, null)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCustomTitle(customView)
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.confirm_text), null)
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            positiveBtn.setOnClickListener {
                val newDirEditText = customView.findViewById<EditText>(R.id.etDirectoryName)
                val newDirName = newDirEditText.text.toString()
                val isAlphaNumeric = newDirName.all { it.isLetterOrDigit() }
                val isTooLong = newDirName.length >= 15

                when {
                    newDirName.isNotBlank() and isAlphaNumeric and
                            !doesFileAlreadyExists(newDirName, dir)  and !isTooLong -> {
                        createNewDirectory(newDirName, dir)
                        // Updating RV UI after file is created
                        vm.refreshFiles()
                        dialog.dismiss()
                    }
                    doesFileAlreadyExists(newDirName, dir) -> {
                        Timber.tag(DEBUG_TAG).d(context.getString(R.string.Error_FileAlreadyExists))
                        customView.findViewById<TextView>(R.id.tvErrorMessage)
                            .toggleErrorMessage(R.string.Error_FileAlreadyExists)
                    }
                    newDirName.isBlank() or !isAlphaNumeric -> {
                        Timber.tag(DEBUG_TAG).d(context.getString(R.string.Error_IllegalFileName))
                        customView.findViewById<TextView>(R.id.tvErrorMessage)
                            .toggleErrorMessage(R.string.Error_IllegalFileName)
                    }
                    isTooLong -> {
                        Timber.tag(DEBUG_TAG).d(context.getString(R.string.Error_FileNameTooLong))
                        customView.findViewById<TextView>(R.id.tvErrorMessage)
                            .toggleErrorMessage(R.string.Error_FileNameTooLong)
                    }
                }
            }
        }
        dialog.show()
        // Important to clear given flags in order for keyboard to show up
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    fun showDeleteDirectoryAlert(vm: ImagesViewModel, dir: File) {
        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle(context.getString(R.string.Delete_FolderText))
            .setNegativeButton(context.getString(R.string.delete_text)) { dialog, _ ->
                // Deleting Directory
                if (!dir.deleteRecursively()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.Error_SomethingWrong),
                        Toast.LENGTH_LONG
                    ).show()
                }
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnDismissListener {
            vm.refreshFiles()
        }

        dialog.show()
    }

    @SuppressLint("InflateParams")
    fun showChooseLockTypeAlert(vm: ImagesViewModel, dir: File) {
        MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle("Select type of protection")
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setItems(getListOfOptions()) { dialog, i ->
                when (i) {
                    0 -> showPasswordSetupAlert(vm, dir)
                    1 -> handleBiometricLockOption()
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @SuppressLint("InflateParams")
    fun showPasswordSetupAlert(vm: ImagesViewModel, dir: File) {
        val layoutInflater = LayoutInflater.from(context)
        val customView: View = layoutInflater.inflate(R.layout.setup_password_custom_alert, null)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCustomTitle(customView)
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.confirm_text), null)
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val errorView = customView.findViewById<TextView>(R.id.tvPwdSetupErrorMessage)
            val pwd1 = customView.findViewById<EditText>(R.id.etPwd1)
            val pwd2 = customView.findViewById<EditText>(R.id.etPwd2)

            positiveBtn.setOnClickListener {
                if (pwd1.text.toString().isNotEmpty() &&
                    (pwd1.text.toString() == pwd2.text.toString())) {
                    errorView.visibility = GONE
                    dir.setDirectoryPassword(activity, pwd1.text.toString())
                    vm.refreshFiles()
                    dialog.dismiss()
                }
                else errorView.visibility = VISIBLE
            }
        }
        dialog.show()
        // Important to clear given flags in order for keyboard to show up
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    @SuppressLint("InflateParams")
    fun showEnterPasswordAlert(vm: ImagesViewModel, dir: File) {
        val layoutInflater = LayoutInflater.from(context)
        val customView: View = layoutInflater.inflate(R.layout.enter_password_custom_alert, null)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCustomTitle(customView)
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.confirm_text), null)
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setOnClickListener {
                val passView = customView.findViewById<EditText>(R.id.etPassword)
                val passError = customView.findViewById<TextView>(R.id.tvPwdErrorMessage)

                when (passView.text.toString() == dir.getDirectoryPassword(activity)) {
                    true -> {
                        passError.visibility = GONE
                        vm.setRootDir(dir)
                        dialog.dismiss()
                    }
                    false -> passError.visibility = VISIBLE
                }
            }
        }
        dialog.show()
        // Important to clear given flags in order for keyboard to show up
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    fun showDeletePasswordAlert(vm: ImagesViewModel, dir: File) {
        val layoutInflater = LayoutInflater.from(context)
        val customView: View = layoutInflater.inflate(R.layout.delete_password_custom_alert, null)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCustomTitle(customView)
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(context.getString(R.string.confirm_text), null)
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setOnClickListener {
                val passView = customView.findViewById<EditText>(R.id.etConfPassword)
                val passError = customView.findViewById<TextView>(R.id.tvPwdDelErrorMessage)

                when (passView.text.toString() == dir.getDirectoryPassword(activity)) {
                    true -> {
                        passError.visibility = GONE
                        dir.clearDirectoryPassword(activity)
                        dialog.dismiss()
                    }
                    false -> passError.visibility = VISIBLE
                }
            }
        }
        dialog.show()
        // Important to clear given flags in order for keyboard to show up
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }



    fun showChangeDirectoryColorAlert(vm: ImagesViewModel, dir: File) {
        val layoutInflater = LayoutInflater.from(context)
        val customView: View = layoutInflater.inflate(R.layout.change_folder_color_custom_alert, null)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setCustomTitle(customView)
            .setNegativeButton(context.getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            setupColorChangeListeners(customView, dir, dialog)
        }

        dialog.setOnDismissListener {
            vm.refreshFiles()
        }
        dialog.show()
    }

    private fun getViewColor(view: View) : Int =
        (view.background as ColorDrawable).color

    private fun setupColorChangeListeners(customView: View, dir: File, dialog: androidx.appcompat.app.AlertDialog) {
        customView.findViewById<View>(R.id.colorDefault).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorYellow).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorOrange).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorGreen).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorTeal).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorBrown).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorBlue).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
        customView.findViewById<View>(R.id.colorRed).setOnClickListener{
            dir.setDirectoryColor(activity, getViewColor(it))
            dialog.dismiss()
        }
    }

    private fun getListOfOptions() : Array<String>{
        return when(BiometricUtil(activity).isBiometricSupported) {
            true -> arrayOf("Regular Password", "Phone Biometrics")
            false -> arrayOf("Regular Password")
        }
    }

    private fun handleBiometricLockOption() {
        val biometricUtil = BiometricUtil(activity)
    }


}