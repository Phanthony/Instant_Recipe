package com.phanthony.instantrecipe.ui

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.phanthony.instantrecipe.main.IngredientAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import android.os.Bundle
import android.os.SystemClock
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.extensions.getErrorDialog
import com.phanthony.instantrecipe.extensions.px
import io.reactivex.schedulers.Schedulers
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

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
        adapter = IngredientAdapter(ingList.toMutableList())

        val ingredientList = view.findViewById<RecyclerView>(R.id.ingredientList)
        ingredientList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        ingredientList.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            val paint = Paint().apply {
                setARGB(255,255,0,0)
            }
            val icon = BitmapFactory.decodeResource(resources,R.drawable.delete_icon)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val remove = viewHolder.itemView.findViewById<AppCompatTextView>(R.id.ingredientName).text.toString()
                val newSet = viewModel.getIngList().value!!
                newSet.remove(remove)
                viewModel.setIngList(newSet)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ){
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    val itemView = viewHolder.itemView

                    c.drawRoundRect(itemView.right.toFloat() + dX,itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat(),10.0f,10.0f,paint)
                    c.drawBitmap(icon,(itemView.right - 16.px - icon.width).toFloat(), (itemView.top + ( itemView.bottom - itemView.top - icon.height)/2).toFloat(), paint)

                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

        }).attachToRecyclerView(ingredientList)

        observeList()

        nav = this.findNavController()

        var findRecipeLastClickTime: Long = 0

        val recipeButton = view.findViewById<AppCompatButton>(R.id.findRecipeButton)
        recipeButton.setOnClickListener {
            if (SystemClock.elapsedRealtime() - findRecipeLastClickTime < 1000) {
            } else {
                findRecipeLastClickTime = SystemClock.elapsedRealtime()
                Snackbar.make(activity!!.findViewById(R.id.mainLayout), R.string.search_recipes, Snackbar.LENGTH_SHORT)
                    .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                    .show()
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
                                    nav.navigate(R.id.action_ingredientFragment_to_recipeFragment)
                                }
                                2 -> {
                                    Snackbar.make(
                                        activity!!.findViewById(R.id.mainLayout),
                                        R.string.no_recipe,
                                        Snackbar.LENGTH_SHORT
                                    )
                                        .setAnchorView(activity!!.findViewById<BottomNavigationView>(R.id.bottomNav))
                                        .show()
                                }
                            }
                        }
                    },
                        onError = { result ->
                            getErrorDialog(result.message!!, this.context!!).show()
                        })
            }
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