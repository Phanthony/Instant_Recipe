package com.phanthony.instantrecipe.ui

import com.phanthony.instantrecipe.main.RecipeViewModel
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.main.RecipeViewModelFactory

class FindRecipeFragment: Fragment() {
    private lateinit var viewModel: RecipeViewModel
    private val RESULT_LOAD_IMAGE = 1
    private val CAMERA_REQUREST = 2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.find_recipes_fragment,container,false)

        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        val uploadReceipt: AppCompatButton = view.findViewById(R.id.uploadReceiptButton)
        uploadReceipt.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, RESULT_LOAD_IMAGE)
        }

        val takePicture: AppCompatButton = view.findViewById(R.id.takePictureButton)
        takePicture.setOnClickListener {
            val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(i, CAMERA_REQUREST)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && data != null){
            val nav = this.activity!!.findNavController(R.id.navHostFragment)
            if(requestCode == RESULT_LOAD_IMAGE) {
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
                val selectedImage = data.extras?.get("data") as Bitmap
                viewModel.setImage(selectedImage)
                nav.navigate(R.id.selectPicFragment)
            }
        }
    }
}