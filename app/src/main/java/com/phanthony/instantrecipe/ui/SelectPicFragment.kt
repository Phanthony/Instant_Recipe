package com.phanthony.instantrecipe.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.phanthony.instantrecipe.R
import kotlinx.android.synthetic.main.selected_pic_fragment.view.*
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory

class SelectPicFragment : Fragment() {

    var image: Bitmap? = null
    lateinit var viewModel: RecipeViewModel
    lateinit var nav: NavController
    lateinit var loading: RelativeLayout
    lateinit var scanButton: AppCompatButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.selected_pic_fragment, container, false)

        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        loading = view.findViewById(R.id.loadingPanel)
        scanButton = view.findViewById(R.id.scanImageButton)

        image = viewModel.getImage()
        view.selectedImage.setImageBitmap(image)

        nav = this.findNavController()

        view.scanImageButton.setOnClickListener {
            viewModel.addImageToQueue(image!!)
            nav.navigate(R.id.action_selectPicFragment_to_findRecipeFragment)
        }
        return view
    }
}