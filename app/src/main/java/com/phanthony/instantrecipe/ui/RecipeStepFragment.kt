package com.phanthony.instantrecipe.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.main.RecipeStepAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.ingredient_list_fragment.*

class RecipeStepFragment: Fragment() {

    lateinit var viewModel: RecipeViewModel
    lateinit var adapter: RecipeStepAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recipe_step_fragment, container, false)
        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        adapter = RecipeStepAdapter(arrayListOf())

        val recipeStepList = view.findViewById<RecyclerView>(R.id.stepList)
        recipeStepList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeStepList.adapter = adapter

        val recipeTitle = view.findViewById<AppCompatTextView>(R.id.recipeTitle)

        observeSteps(recipeTitle)

        return view
    }

    fun observeSteps(view: AppCompatTextView){
        viewModel.getRecipe().observe(this, Observer { retrieveRecipe(adapter,it,view) })
    }

    @SuppressLint("CheckResult")
    fun retrieveRecipe(adapter: RecipeStepAdapter, id: Int, view: AppCompatTextView){
        viewModel.getSingleRecipe(id)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe { result ->
            view.text = result.title
        }
        viewModel.getRecipeInstructions(id)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe{ result ->
            adapter.clear()
            adapter.addAll(result)
        }

    }

}