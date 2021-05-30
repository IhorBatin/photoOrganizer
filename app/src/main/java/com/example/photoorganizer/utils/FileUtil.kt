package com.example.photoorganizer.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


open class FileUtil(private val activity: Activity, private val context: Context) {

    lateinit var currentPhotoPath: String

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmssss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Timber.tag(DEBUG_TAG).d("photoPath -> $currentPhotoPath")
        }
    }

    fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context,
                        "com.example.photoorganizer",
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


}