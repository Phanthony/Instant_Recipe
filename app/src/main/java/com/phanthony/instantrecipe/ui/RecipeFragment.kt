package com.phanthony.instantrecipe.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
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
import com.phanthony.instantrecipe.extensions.px
import com.phanthony.instantrecipe.main.RecipeAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import com.phanthony.instantrecipe.service.WrappedResult
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs

class RecipeFragment : Fragment() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var viewModel: RecipeViewModel
    lateinit var nav: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recipes_fragment, container, false)

        viewModel = activity!!.run {
            ViewModelProviders.of(
                this,
                RecipeViewModelFactory(this.application)
            )[RecipeViewModel::class.java]
        }

        nav = this.findNavController()

        adapter = RecipeAdapter(context!!, this::getRecipeInstruction)

        val recipeList = view.findViewById<RecyclerView>(R.id.recipeList)
        recipeList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeList.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            val paint = Paint().apply {
                setARGB(255, 105, 255, 18)
            }
            val icon = BitmapFactory.decodeResource(resources, R.drawable.add_icon)

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView

                    c.drawRoundRect(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        5.0f,
                        5.0f,
                        paint
                    )
                    c.drawBitmap(
                        icon,
                        (itemView.right - 16.px - icon.width).toFloat(),
                        (itemView.top + (itemView.bottom - itemView.top - icon.height) / 2).toFloat(),
                        paint
                    )

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            @SuppressLint("CheckResult")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = viewHolder.itemView.findViewById<AppCompatTextView>(R.id.recipeId).text.toString().toInt()
                //checkRecipeExists(id, viewHolder.adapterPosition)
                test(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { recipeResult ->
                    if(recipeResult.result.isFailure){
                        getErrorDialog(recipeResult.result.exceptionOrNull()!!.message!!,this@RecipeFragment.requireContext()).show()
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    } else {
                        when(recipeResult.result.getOrNull()!!){
                            1 -> {
                                Snackbar.make(
                                    activity!!.findViewById(R.id.mainLayout),
                                    R.string.saved_recipe,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                    .show()
                                adapter.notifyItemChanged(viewHolder.adapterPosition)
                            }
                            2 -> {
                                Snackbar.make(
                                    activity!!.findViewById(R.id.mainLayout),
                                    R.string.not_saved,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                    .show()
                                adapter.notifyItemChanged(viewHolder.adapterPosition)
                            }
                        }
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                }
            }
        }).attachToRecyclerView(recipeList)

        observeList()

        return view
    }

    @SuppressLint("CheckResult")
    private fun test(recipeId: Int): Single<WrappedResult<Int>> {
        return checkIfRecipeInDatabase(recipeId).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).map { inDataBase ->
            if (inDataBase.result.isFailure) {
                WrappedResult(Result.failure(inDataBase.result.exceptionOrNull()!!))
            } else {
                when (inDataBase.result.getOrNull()!!) {
                    true -> { // Recipe in database already
                        viewModel.changeRecipeInformation(recipeId,viewModel::setRecipeSaveAndSeen)
                        WrappedResult(Result.success(1))
                    }
                    false -> { // Recipe isn't in database, check if api call is needed
                        WrappedResult(Result.success(2))
                    }
                }
            }
        }.map { recipeInDb ->
            if (recipeInDb.result.isFailure) {
                WrappedResult(Result.failure(recipeInDb.result.exceptionOrNull()!!))
            } else {
                when (recipeInDb.result.getOrNull()!!) {
                    1 -> recipeInDb
                    else -> {
                        val needApiCall = checkIfRecipeSeen(recipeId).blockingGet()
                        if(needApiCall.getOrNull()!!){ // Already seen
                            WrappedResult(Result.success(2))
                        } else { // never seen
                            WrappedResult(Result.success(3))
                        }
                    }
                }
            }
        }.map { needApiCall ->
            if(needApiCall.result.isFailure){
                needApiCall
            } else {
                when(needApiCall.result.getOrNull()!!){
                    1 -> needApiCall // Recipe in database already
                    2 -> needApiCall // Already check for recipe before
                    else -> { // make the api call
                        val apiResult = viewModel.getRecipeInstruction(recipeId).blockingGet()
                        if(apiResult.isFailure){
                            WrappedResult(Result.failure(apiResult.exceptionOrNull()!!))
                        } else {
                            when(apiResult.getOrNull()!!){
                                1 -> { // Instructions found
                                    viewModel.changeRecipeInformation(recipeId,viewModel::setRecipeSaveAndSeen)
                                    WrappedResult(Result.success(1))
                                }
                                else -> {// No instructions found
                                    viewModel.changeRecipeInformation(recipeId,viewModel::setRecipeSeen)
                                    WrappedResult(Result.success(2))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun checkRecipeExists(recipeId: Int, position: Int) {
        checkIfRecipeInDatabase(recipeId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                if (result.result.isFailure) {
                    getErrorDialog(
                        result.result.exceptionOrNull()!!.message!!,
                        this.requireContext()
                    ).show()
                } else {
                    when (result.result.getOrNull()!!) {
                        true -> { // Recipe in database already
                            viewModel.changeRecipeInformation(
                                recipeId,
                                viewModel::setRecipeSaveAndSeen
                            )
                            Snackbar.make(
                                activity!!.findViewById(R.id.mainLayout),
                                R.string.saved_recipe,
                                Snackbar.LENGTH_SHORT
                            )
                                .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                .show()
                            adapter.notifyItemChanged(position)
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
                                        ).setAnchorView(
                                            activity!!.findViewById<BottomNavigationView>(R.id.bottomNav)
                                        )
                                            .show()
                                        adapter.notifyItemChanged(position)
                                    } else { // make the api call
                                        viewModel.getRecipeInstruction(recipeId)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe { recipeResult ->
                                                if (recipeResult.isFailure) {
                                                    getErrorDialog(
                                                        recipeResult.exceptionOrNull()!!.message!!,
                                                        this.requireContext()
                                                    ).show()
                                                } else {
                                                    when (recipeResult.getOrNull()!!) {
                                                        1 -> { // Instructions found
                                                            viewModel.changeRecipeInformation(
                                                                recipeId,
                                                                viewModel::setRecipeSaveAndSeen
                                                            )
                                                            Snackbar.make(
                                                                activity!!.findViewById(R.id.mainLayout),
                                                                R.string.saved_recipe,
                                                                Snackbar.LENGTH_SHORT
                                                            ).setAnchorView(
                                                                activity!!.findViewById<BottomNavigationView>(
                                                                    R.id.bottomNav
                                                                )
                                                            )
                                                                .show()
                                                            adapter.notifyItemChanged(position)
                                                        }
                                                        2 -> { // No instructions found
                                                            viewModel.changeRecipeInformation(
                                                                recipeId,
                                                                viewModel::setRecipeSeen
                                                            )
                                                            Snackbar.make(
                                                                activity!!.findViewById(R.id.mainLayout),
                                                                R.string.not_saved,
                                                                Snackbar.LENGTH_SHORT
                                                            ).setAnchorView(
                                                                activity!!.findViewById<BottomNavigationView>(
                                                                    R.id.bottomNav
                                                                )
                                                            )
                                                                .show()
                                                            adapter.notifyItemChanged(position)
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
    private fun checkIfRecipeInDatabase(recipeId: Int): Single<WrappedResult<Boolean>> {
        return viewModel.getRecipeInstructions(recipeId).subscribeOn(Schedulers.io())
            .map { foundRecipes ->
                if (foundRecipes.isNotEmpty()) {
                    WrappedResult(Result.success(true))
                } else {
                    WrappedResult(Result.success(false))
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
                if (recipeSaved.result.getOrNull()!!) { // recipe is in database
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
                                )
                                    .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                    .show()
                            } else { // hasn't been checked make the api call
                                viewModel.getRecipeInstruction(recipeId)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { callResult ->
                                        if (callResult.isFailure) {
                                            getErrorDialog(
                                                callResult.exceptionOrNull()!!.message!!,
                                                this.requireContext()
                                            ).show()
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
                                                    ).setAnchorView(
                                                        activity!!.findViewById<BottomNavigationView>(
                                                            R.id.bottomNav
                                                        )
                                                    )
                                                        .show()
                                                }
                                            }
                                            viewModel.changeRecipeInformation(
                                                recipeId,
                                                viewModel::setRecipeSeen
                                            )
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