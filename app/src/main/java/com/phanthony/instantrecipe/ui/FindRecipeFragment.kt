package com.phanthony.instantrecipe.ui

import com.phanthony.instantrecipe.main.RecipeViewModel
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FindRecipeFragment: Fragment() {
    private lateinit var viewModel: RecipeViewModel
    private val RESULT_LOAD_IMAGE = 1
    private val CAMERA_REQUREST = 2

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.WRITE_CONTACTS,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.CAMERA)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.find_recipes_fragment,container,false)

        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        if(!hasPermissions(this.requireContext(),*PERMISSIONS)){
            ActivityCompat.requestPermissions(this.requireActivity(),PERMISSIONS,1)
        }

        val uploadReceipt: AppCompatButton = view.findViewById(R.id.uploadReceiptButton)
        uploadReceipt.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, RESULT_LOAD_IMAGE)
        }

        val takePicture: AppCompatButton = view.findViewById(R.id.takePictureButton)
        takePicture.setOnClickListener {
            dispatchTakePictureIntent()
        }



        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            val nav = this.findNavController()
            if(requestCode == RESULT_LOAD_IMAGE && data != null) {
                val selectedImage = data.data!!
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = context!!.contentResolver.query(selectedImage, filePathColumn, null, null, null)!!.apply {
                    moveToFirst()
                }

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                viewModel.setImage(BitmapFactory.decodeFile(picturePath))
                nav.navigate(R.id.selectPicFragment)
            }
            if(requestCode == CAMERA_REQUREST){
                viewModel.setImage(BitmapFactory.decodeFile(currentPhotoPath))
                nav.navigate(R.id.selectPicFragment)
            }
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    lateinit var currentPhotoPath: String


    //Credit to https://developer.android.com/training/camera/photobasics.html
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    //Credit to https://developer.android.com/training/camera/photobasics.html
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
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
                        this.requireContext(),
                        "com.phanthony.instantrecipe.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUREST)
                }
            }
        }
    }
}