package com.phanthony.instantrecipe.ui

import com.phanthony.instantrecipe.main.IngredientAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.extensions.getErrorDialog
import io.reactivex.schedulers.Schedulers
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

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

        val recipeButton = view.findViewById<AppCompatButton>(R.id.findRecipeButton)
        recipeButton.setOnClickListener {
            Toast.makeText(context, R.string.search_recipes, Toast.LENGTH_SHORT).show()
            val set = viewModel.getIngList().value!!
            viewModel.getRecipes(set)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeBy(onSuccess = { result ->
                    if (result.isFailure) {
                        getErrorDialog(result.exceptionOrNull()!!.message!!, this.context!!).show()
                    } else {
                        when (result.getOrNull()!!) {
                            1 -> {
                                nav.navigate(R.id.recipeFragment)
                            }
                            2 -> {
                                Toast.makeText(context, R.string.no_recipe, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                    onError = { result ->
                        getErrorDialog(result.message!!, this.context!!).show()
                    })
        }

        val ingButton = view.findViewById<AppCompatButton>(R.id.addIngButton)
        ingButton.setOnClickListener {
            val textBox = view.findViewById<AppCompatEditText>(R.id.ingredientTextBox)
            if (!textBox.text.isNullOrBlank()) {
                val set = viewModel.getIngList().value!!
                set.add(textBox.text.toString())
                textBox.text?.clear()
                viewModel.setIngList(set)
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