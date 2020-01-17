package com.phanthony.instantrecipe.ui

import android.annotation.SuppressLint
import com.phanthony.instantrecipe.main.RecipeAdapter
import com.phanthony.instantrecipe.main.RecipeViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.extensions.getErrorDialog
import com.phanthony.instantrecipe.main.RecipeViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class RecipeFragment : Fragment() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var viewModel: RecipeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.recipes_fragment, container, false)

        viewModel = activity!!.run {
            ViewModelProviders.of(this, RecipeViewModelFactory(this.application))[RecipeViewModel::class.java]
        }

        val nav = activity!!.findNavController(R.id.navHostFragment)

        adapter = RecipeAdapter(context!!, nav, this::getRecipeInstruction)

        val recipeList = view.findViewById<RecyclerView>(R.id.recipeList)
        recipeList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        recipeList.adapter = adapter

        observeList()

        return view
    }

    @SuppressLint("CheckResult")
    private fun getRecipeInstruction(recipeId: Int) {
        viewModel.getRecipeInstruction(recipeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { result ->
                if (result.isFailure) {
                    getErrorDialog(result.exceptionOrNull()!!.message!!,this.context!!).show()
                } else {
                    when(result.getOrNull()){
                        1 -> {
                            //Found instructions
                        }
                        2 -> {
                            //No instructions found
                            Toast.makeText(this.context,R.string.no_instruction,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, onError = { result ->
                getErrorDialog(result.message!!,this.context!!).show()
            })
    }

    fun observeList() {
        viewModel.getRecipeList().observe(this, Observer(adapter::submitList))
    }
}