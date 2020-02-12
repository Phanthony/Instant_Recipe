package com.phanthony.instantrecipe.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.main.RecipeStepAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import com.phanthony.instantrecipe.main.SavedRecipeStepAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RecipeStepFragment : Fragment() {

    lateinit var viewModel: RecipeViewModel
    lateinit var adapter: RecipeStepAdapter
    var saveFragment = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recipe_step_fragment, container, false)
        viewModel = activity!!.run {
            ViewModelProviders.of(
                this,
                RecipeViewModelFactory(this.application)
            )[RecipeViewModel::class.java]
        }

        val safeArgs: RecipeStepFragmentArgs by navArgs()
        saveFragment = safeArgs.fromSaved


        adapter = RecipeStepAdapter(arrayListOf(), saveFragment)

        val recipeStepList = view.findViewById<RecyclerView>(R.id.stepList)
        recipeStepList.layoutManager =
            LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeStepList.adapter = adapter

        observeSteps()

        return view
    }

    fun observeSteps() {
        viewModel.getRecipe().observe(this, Observer { retrieveRecipe(adapter, it) })
    }

    @SuppressLint("CheckResult")
    fun retrieveRecipe(adapter: RecipeStepAdapter, id: Int) {
        viewModel.getSingleRecipe(id).subscribeOn(Schedulers.io()).map { recipeResult ->
            recipeResult
        }.subscribe { recipeInfo ->
            viewModel.getRecipeInstructions(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { recipe ->
                    adapter.clear()
                    var allIngs: String
                    if (!saveFragment) {
                        allIngs = "Missing Ingredients\n"
                        for (miss in recipeInfo.missedIngredients.indices) {
                            allIngs += if (miss == recipeInfo.missedIngredients.lastIndex) {
                                recipeInfo.missedIngredients[miss].name
                            } else {
                                "${recipeInfo.missedIngredients[miss].name}, "
                            }
                        }
                        allIngs += "-splithere-Used Ingredients\n"
                        for (use in recipeInfo.usedIngredients.indices) {
                            allIngs += if (use == recipeInfo.usedIngredients.lastIndex) {
                                recipeInfo.usedIngredients[use].name
                            } else {
                                "${recipeInfo.usedIngredients[use].name}, "
                            }
                        }
                    } else {
                        allIngs = "Ingredients For The Recipe\n"
                        for (miss in recipeInfo.missedIngredients.indices) {
                            allIngs +=
                                "${recipeInfo.missedIngredients[miss].name}, "
                        }
                        for (use in recipeInfo.usedIngredients.indices) {
                            allIngs += if (use == recipeInfo.usedIngredients.lastIndex) {
                                recipeInfo.usedIngredients[use].name
                            } else {
                                "${recipeInfo.usedIngredients[use].name}, "
                            }
                        }
                    }
                    val recipeStepList = arrayListOf<Pair<String, Boolean>>()
                    recipeStepList.add(Pair(recipeInfo.title, true))
                    recipeStepList.add(Pair(allIngs, true))
                    recipe.forEach { outerRecipeStep ->
                        recipeStepList.add(Pair(outerRecipeStep.name, true))
                        outerRecipeStep.steps.forEach { innerRecipeStep ->
                            recipeStepList.add(
                                Pair(
                                    "${innerRecipeStep.number}. ${innerRecipeStep.step}\n",
                                    false
                                )
                            )
                        }
                    }
                    adapter.addAll(recipeStepList)
                }
        }
    }


}