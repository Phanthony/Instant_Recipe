package com.phanthony.instantrecipe.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.extensions.getErrorDialog
import com.phanthony.instantrecipe.main.RecipeAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class RecipeFragment : Fragment() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var viewModel: RecipeViewModel
    lateinit var nav: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recipes_fragment, container, false)

        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        nav = this.findNavController()

        adapter = RecipeAdapter(context!!, this::checkIfRecipeExists)

        val recipeList = view.findViewById<RecyclerView>(R.id.recipeList)
        recipeList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeList.adapter = adapter

        observeList()

        return view
    }

    var recipeStepLastClickTime: Long = 0

    @SuppressLint("CheckResult")
    private fun checkIfRecipeExists(recipeId: Int) {
        if (SystemClock.elapsedRealtime() - recipeStepLastClickTime < 1000) {
            return
        }
        recipeStepLastClickTime = SystemClock.elapsedRealtime();
        viewModel.getRecipeInstructions(recipeId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                if (result.isNotEmpty()) {
                    viewModel.setRecipe(recipeId)
                    nav.navigate(R.id.action_recipeFragment_to_recipeStepFragment)
                } else {
                    getRecipeInstruction(recipeId)
                }
            }
    }

    @SuppressLint("CheckResult")
    private fun getRecipeInstruction(recipeId: Int) {
        viewModel.getRecipeInstruction(recipeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { result ->
                if (result.isFailure) {
                    getErrorDialog(result.exceptionOrNull()!!.message!!, this.context!!).show()
                } else {
                    when (result.getOrNull()) {
                        1 -> {
                            viewModel.setRecipe(recipeId)
                            nav.navigate(R.id.action_recipeFragment_to_recipeStepFragment)
                        }
                        2 -> {
                            //No instructions found
                            Snackbar.make(
                                activity!!.findViewById(R.id.mainLayout),
                                R.string.no_instruction,
                                Snackbar.LENGTH_SHORT
                            )
                                .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                .show()
                        }
                    }
                }
            }, onError = { result ->
                getErrorDialog(result.message!!, this.context!!).show()
            })
    }

    fun observeList() {
        viewModel.getRecipeList()?.observe(this, Observer(adapter::submitList))
    }
}