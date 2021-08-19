package com.example.photoorganizer.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import com.example.photoorganizer.R
import com.example.photoorganizer.ext.toggleErrorMessage
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
    fun createImageFile(dir: File): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssss").format(Date())
        //val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val storageDir: File? = dir
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            //Timber.tag(DEBUG_TAG).d("new photoPath -> $currentPhotoPath")
        }
    }

    private fun createNewDirectory(name: String, dir: File): File {
        return File(
            "${dir.path}${File.separator}$name"
        ).apply {
            Timber.tag(DEBUG_TAG).d("Creating new directory: '$name'")
            mkdir()
            Timber.tag(DEBUG_TAG).d("New folder: '${this.path}' -> created")
        }
    }

    private fun doesFileAlreadyExists(neFileName: String, dir: File) : Boolean{
        ImagesRepository.fetchAllFilesByDate(dir).also {
            it?.forEach { file ->
                if (file.name.equals(neFileName, ignoreCase = true)) return true
            }
        }
        return false
    }

    fun dispatchTakePictureIntent(dir: File) {
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

    fun readBytesFromUri(uri: Uri): ByteArray? =
        activity.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }

    fun setImageFromPath(imgFile: String, imageView: ImageView) {
        val pictureBitmap = BitmapFactory.decodeFile(imgFile)
        imageView.setImageBitmap(pictureBitmap)
    }

    @SuppressLint("InflateParams")
    fun showNewFolderAlert(vm: ImagesViewModel, dir: File) {
        val layoutInflater = LayoutInflater.from(context)
        val customView: View = layoutInflater.inflate(R.layout.edit_text_custom_alert, null)

        val dialog = MaterialAlertDialogBuilder(
            activity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        ).setCancelable(false)
            .setCustomTitle(customView)
            .setNegativeButton("Cancel") { dialog, _ ->
                // Respond to negative button press
                dialog.dismiss()
            }
            .setPositiveButton("Confirm", null)
            .create()

        dialog.setOnShowListener() {
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
                        vm.updateFiles(dir)
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
}


