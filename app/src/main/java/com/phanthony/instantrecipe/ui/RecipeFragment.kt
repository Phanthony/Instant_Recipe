package com.phanthony.instantrecipe.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.database.SpoonacularResult
import com.phanthony.instantrecipe.extensions.getErrorDialog
import com.phanthony.instantrecipe.main.RecipeAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.Maybe
import io.reactivex.Single
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

        adapter = RecipeAdapter(context!!, this::getRecipeInstruction)

        val recipeList = view.findViewById<RecyclerView>(R.id.recipeList)
        recipeList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeList.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = viewHolder.itemView.findViewById<AppCompatTextView>(R.id.recipeId).text.toString().toInt()
                checkRecipeExists(id)
                adapter.notifyItemChanged(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recipeList)

        observeList()

        return view
    }


    @SuppressLint("CheckResult")
    private fun checkRecipeExists(recipeId: Int) {
        checkIfRecipeInDatabase(recipeId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                if (result.isFailure) {
                    getErrorDialog(result.exceptionOrNull()!!.message!!,this.requireContext()).show()
                } else {
                    when (result.getOrNull()!!) {
                        true -> { // Recipe in database already
                            viewModel.changeRecipeInformation(recipeId, viewModel::setRecipeSaveAndSeen)
                            Snackbar.make(
                                activity!!.findViewById(R.id.mainLayout),
                                R.string.saved_recipe,
                                Snackbar.LENGTH_SHORT
                            ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                .show()
                        }
                        false -> { // Recipe isn't in database, check if api call is needed
                            checkIfRecipeSeen(recipeId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { seenResult ->
                                    if (seenResult.getOrNull()!!) { // already checked, no recipe instruction
                                        Snackbar.make(
                                            activity!!.findViewById(R.id.mainLayout),
                                            R.string.not_saved,
                                            Snackbar.LENGTH_SHORT
                                        ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                            .show()
                                    } else { // make the api call
                                        viewModel.getRecipeInstruction(recipeId).subscribe { recipeResult ->
                                            if (recipeResult.isFailure) {
                                                getErrorDialog(recipeResult.exceptionOrNull()!!.message!!,this.requireContext()).show()
                                            } else {
                                                when (recipeResult.getOrNull()!!) {
                                                    1 -> { // Instructions found
                                                        viewModel.changeRecipeInformation(recipeId, viewModel::setRecipeSaveAndSeen)
                                                        Snackbar.make(
                                                            activity!!.findViewById(R.id.mainLayout),
                                                            R.string.saved_recipe,
                                                            Snackbar.LENGTH_SHORT
                                                        ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                                            .show()
                                                    }
                                                    2 -> { // No instructions found
                                                        viewModel.changeRecipeInformation(recipeId, viewModel::setRecipeSeen)
                                                        Snackbar.make(
                                                            activity!!.findViewById(R.id.mainLayout),
                                                            R.string.not_saved,
                                                            Snackbar.LENGTH_SHORT
                                                        ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                                            .show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }

    @SuppressLint("CheckResult")
    private fun checkIfRecipeInDatabase(recipeId: Int): Single<Result<Boolean>> {
        return viewModel.getRecipeInstructions(recipeId).subscribeOn(Schedulers.io()).map { foundRecipes ->
            if (foundRecipes.isNotEmpty()) {
                Result.success(true)
            } else {
                Result.success(false)
            }
        }
    }

    private fun checkIfRecipeSeen(recipeId: Int): Maybe<Result<Boolean>> {
        return viewModel.getSingleRecipe(recipeId)
            .subscribeOn(Schedulers.io())
            .map { foundRecipe ->
                Result.success(foundRecipe.seen)
            }
    }

    var recipeStepLastClickTime: Long = 0

    @SuppressLint("CheckResult")
    private fun getRecipeInstruction(recipeId: Int) {
        if (SystemClock.elapsedRealtime() - recipeStepLastClickTime < 1000) {
            return
        }
        recipeStepLastClickTime = SystemClock.elapsedRealtime()
        // check if Recipe is in the database
        checkIfRecipeInDatabase(recipeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { recipeSaved ->
                if (recipeSaved.getOrNull()!!) { // recipe is in database
                    viewModel.setRecipe(recipeId)
                    nav.navigate(R.id.action_recipeFragment_to_recipeStepFragment)
                } else { // recipe is not in database, check if previously seen to remove redundant calls
                    checkIfRecipeSeen(recipeId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { seenResult ->
                            if (seenResult.getOrNull()!!) { // already checked, no recipe instruction
                                Snackbar.make(
                                    activity!!.findViewById(R.id.mainLayout),
                                    R.string.not_saved,
                                    Snackbar.LENGTH_SHORT
                                ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                    .show()
                            } else { // hasn't been checked make the api call
                                viewModel.getRecipeInstruction(recipeId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { callResult ->
                                        if (callResult.isFailure) {
                                            Snackbar.make(
                                                activity!!.findViewById(R.id.mainLayout),
                                                callResult.exceptionOrNull()!!.message!!,
                                                Snackbar.LENGTH_SHORT
                                            ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                                .show()
                                        } else {
                                            when (callResult.getOrNull()!!) {
                                                1 -> { // call found instructions
                                                    viewModel.setRecipe(recipeId)
                                                    nav.navigate(R.id.action_recipeFragment_to_recipeStepFragment)
                                                }
                                                2 -> { // call found no instructions
                                                    Snackbar.make(
                                                        activity!!.findViewById(R.id.mainLayout),
                                                        R.string.no_instruction,
                                                        Snackbar.LENGTH_SHORT
                                                    ).setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                                        .show()
                                                }
                                            }
                                            viewModel.changeRecipeInformation(recipeId, viewModel::setRecipeSeen)
                                        }
                                    }
                            }
                        }
                }
            }
    }

    fun observeList() {
        viewModel.getRecipeList()?.observe(this, Observer(adapter::submitList))
    }
}