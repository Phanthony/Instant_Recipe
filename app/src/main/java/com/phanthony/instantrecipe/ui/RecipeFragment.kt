package com.phanthony.instantrecipe.ui

import main.RecipeAdapter
import main.RecipeViewModel
import database.SpoonacularResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R

class RecipeFragment : Fragment() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recipes_fragment,container,false)

        viewModel = activity!!.run {
            ViewModelProviders.of(this)[RecipeViewModel::class.java]
        }

        val nav = activity!!.findNavController(R.id.navHostFragment)

        adapter = RecipeAdapter(context!!, nav, viewModel::getRecipeInstruction)

        val recipeList = view.findViewById<RecyclerView>(R.id.recipeList)
        recipeList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeList.adapter = adapter

        observeList()

        return view
    }

    fun observeList(){
        viewModel.getRecipeList().observe(this, Observer(adapter::submitList))
    }
}