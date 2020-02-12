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
import com.phanthony.instantrecipe.extensions.dp
import com.phanthony.instantrecipe.extensions.getErrorDialog
import com.phanthony.instantrecipe.extensions.px
import com.phanthony.instantrecipe.main.RecipeAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlin.math.abs

class SavedRecipeFragment: Fragment() {

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


        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            val paint = Paint().apply {
                setARGB(255,255,34,18)
            }
            val icon = BitmapFactory.decodeResource(resources,R.drawable.delete_icon)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = viewHolder.itemView.findViewById<AppCompatTextView>(R.id.recipeId).text.toString().toInt()
                viewModel.changeRecipeInformation(id,viewModel::setRecipeUnsave)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    val itemView = viewHolder.itemView

                    c.drawRoundRect(itemView.right.toFloat() + dX,itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat(),5.0f,5.0f,paint)
                    c.drawBitmap(icon,(itemView.right - 16.px - icon.width).toFloat(), (itemView.top + ( itemView.bottom - itemView.top - icon.height)/2).toFloat(), paint)

                    val alpha = 1.0f - abs(dX) / itemView.width
                    itemView.alpha = alpha
                    itemView.translationX = dX

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
        }).attachToRecyclerView(recipeList)

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
                    nav.navigate(R.id.action_savedRecipeFragment_to_recipeStepFragment)
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
        viewModel.getSavedRecipeList()?.observe(this, Observer(adapter::submitList))
    }
}