package com.phanthony.instantrecipe.ui

import com.phanthony.instantrecipe.main.IngredientAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import io.reactivex.schedulers.Schedulers
import com.phanthony.instantrecipe.main.RecipeViewModelFactory

class IngredientFragment : Fragment() {

    private lateinit var adapter: IngredientAdapter

    lateinit var ingList: MutableSet<String>
    lateinit var viewModel: RecipeViewModel
    lateinit var nav: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.ingredient_list_fragment, container, false)
        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        ingList = viewModel.getIngList().value!!
        adapter = IngredientAdapter(ingList.toMutableList(), viewModel)

        val ingredientList = view.findViewById<RecyclerView>(R.id.ingredientList)
        ingredientList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        ingredientList.adapter = adapter

        observeList()

        nav = this.activity!!.findNavController(R.id.navHostFragment)

        val button = view.findViewById<AppCompatButton>(R.id.findRecipeButton)
        button.setOnClickListener {
            Toast.makeText(context, R.string.search_recipes, Toast.LENGTH_SHORT).show()
            val set = viewModel.getIngList().value!!
            viewModel.getRecipes(set)
                .subscribeOn(Schedulers.io())
                .subscribe { result ->
                    if (result.isFailure) {
                        // Error Message
                    } else {
                        when(result.getOrNull()!!){
                            1 -> {
                                nav.navigate(R.id.recipeFragment)
                            }
                            2 -> {
                                Toast.makeText(context, R.string.no_recipe, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

        return view
    }

    private fun observeList() {
        viewModel.getIngList().observe(this, Observer { set ->
            adapter.clear()
            adapter.addAll(set)
        })
    }
}