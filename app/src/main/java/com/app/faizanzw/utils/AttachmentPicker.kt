package com.app.faizanzw.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.app.faizanzw.BuildConfig
import com.app.faizanzw.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AttachmentPicker constructor(
    var context: AppCompatActivity,
    private var onPickAttachmentListener: OnPickAttachmentListener
) {

    private val cameraCode = 10
    private val galleryCode = 11

    //private val cameraCodeForVideo = 12
    //private val galleryCodeForVideo = 13
    private var cropCode = 0
    private var photoFile: File? = null
    private var mCurrentPhotoPath: String = ""
    private var selectionTypeForGallery = 0
    private var selectionTypeForCamera = 0

    @SuppressLint("InflateParams")
    fun showDialogForCameraAndGallery() {
        val mBottomSheetDialog = BottomSheetDialog(context)
        val sheetView = context.layoutInflater.inflate(R.layout.bottom_dialog_image, null)
        val txtCamera = sheetView.findViewById<TextView>(R.id.txt_camera)
        val txtHeader = sheetView.findViewById<TextView>(R.id.txt_header)
        val linCamera = sheetView.findViewById<View>(R.id.linCamera) as LinearLayout
        val linGallery = sheetView.findViewById<View>(R.id.linGallery) as LinearLayout
        val linCancel = sheetView.findViewById<View>(R.id.linCancel) as LinearLayout

        linCancel.setOnClickListener { mBottomSheetDialog.dismiss() }

        linCamera.setOnClickListener {
            mBottomSheetDialog.dismiss()

            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    cameraIntent(cameraCode)
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(
                        context,
                        "Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        //Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                    .check()
            } else {
                TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(
                        //Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                    .check()
            }
        }

        linGallery.setOnClickListener {
            mBottomSheetDialog.dismiss()

            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    loadImageIntent(galleryCode)
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(
                        context,
                        "Permission Denied\n$deniedPermissions",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        //Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .check()
            } else {
                TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(
                        //Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .check()
            }
        }
        mBottomSheetDialog.setContentView(sheetView)
        mBottomSheetDialog.show()
    }

    private fun cameraIntent(code2: Int) {
        val takePictureIntent = if (code2 == cameraCode) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        } else {
            Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        }
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile()
                Log.e("ddddd : ", "${photoFile?.absolutePath} nn")
                // Continue only if the File was successfully created


                selectionTypeForCamera = code2

                val photoURI = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile!!
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraResult.launch(takePictureIntent)

            } catch (ex: Exception) {
                // Error occurred while creating the File
                ex.printStackTrace()

            }
        }
    }

    fun loadImageIntent(selectionType: Int) {
        val i = Intent()
        //i.type = "image/*"
        i.type = "image/*"
        //val extraMimeTypes = arrayOf("image/*", "application/text*")
        //i.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
        selectionTypeForGallery = selectionType
        i.action = Intent.ACTION_GET_CONTENT
        val intent = Intent.createChooser(i, "Select Picture")
        galleryResult.launch(intent)

    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }


    private fun cropIntent(imageUri: Uri, code: Int) {
        val uCrop = UCrop.of(
            imageUri, Uri.fromFile(
                File(
                    context.cacheDir, "${System.currentTimeMillis()}.jpg"
                )
            )
        )
        cropCode = code
        val option = UCrop.Options()
        option.setFreeStyleCropEnabled(true)
        option.setCompressionQuality(75)
        option.setHideBottomControls(true)
        uCrop.withOptions(option)
        uCrop.withMaxResultSize(600, 800)
        uCrop.withAspectRatio(1f, 1f)
        uCrop.start(context)
    }

    private fun startCropIntentFromCamera() {
        val fileUri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider", photoFile!!
        )
        if (fileUri != null) {
            cropCode = 10
            cropIntent(fileUri, cropCode)
        }
    }

    private var galleryResult = context.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (selectionTypeForGallery == galleryCode) {
                val profileUri: Uri? = result.data!!.data!!
                cropIntent(profileUri!!, galleryCode)
            }
        }
    }

    private var cameraResult =
        context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (selectionTypeForCamera == cameraCode) {
                    startCropIntentFromCamera()
                }
            }
        }
}

interface OnPickAttachmentListener {
    fun onPicked(path: String)
}