package com.phanthony.instantrecipe.ui

import main.IngredientAdapter
import main.RecipeViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R

class IngredientFragment: Fragment() {

    private lateinit var adapter: IngredientAdapter

    lateinit var ingList: MutableSet<String>
    lateinit var viewModel: RecipeViewModel
    lateinit var nav: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.ingredient_list_fragment,container,false)
        viewModel = activity!!.run {
            ViewModelProviders.of(this)[RecipeViewModel::class.java]
        }

        ingList = viewModel.getIngList().value!!
        adapter = IngredientAdapter(ingList.toMutableList(),viewModel)

        val ingredientList = view.findViewById<RecyclerView>(R.id.ingredientList)
        ingredientList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        ingredientList.adapter = adapter

        observeList()

        nav = this.activity!!.findNavController(R.id.navHostFragment)

        val button = view.findViewById<AppCompatButton>(R.id.findRecipeButton)
        button.setOnClickListener {
            viewModel.getRecipes(this.context!!)
            nav.navigate(R.id.recipeFragment)
        }

        return view
    }

    fun observeList(){
        viewModel.getIngList().observe(this, Observer { set ->
            adapter.clear()
            adapter.addAll(set)
        })
    }
}