package com.phanthony.instantrecipe.main

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.RecipeSteps

class RecipeStepAdapter(var stepList: ArrayList<Pair<String, Boolean>>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                holder as ViewHolderTitle
                val current = stepList[position]
                holder.recipeTitle.text = current.first
            }
            1 ->{
                holder as ViewHolderIng
                val current = stepList[position]
                val ingList = current.first.split("-splithere-")
                holder.missedIng.text = ingList[0]
                holder.usedIng.text = ingList[1]
            }
            2 -> {
                holder as ViewHolderStep
                val current = stepList[position]
                if (current.second) {
                    if (current.first.isNotBlank()) {
                        holder.steps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
                        holder.steps.text = current.first
                    } else {
                        holder.steps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0f)
                    }
                }
                else {
                    holder.steps.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f)
                    holder.steps.text = current.first
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val layout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_step_title, parent, false)
                ViewHolderTitle(layout)
            }

            1 -> {
                val layout = LayoutInflater.from(parent.context).inflate(R.layout.used_ingredients_missed_ingredients_layout, parent, false)
                ViewHolderIng(layout)
            }
            else -> {
                val layout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_step_layout, parent, false)
                ViewHolderStep(layout)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            1 -> 1
            else -> 2
        }
    }

    override fun getItemCount(): Int {
        return stepList.size
    }

    fun addAll(list: List<Pair<String, Boolean>>?) {
        if (list != null) {
            stepList.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        stepList.clear()
        notifyDataSetChanged()
    }

    class ViewHolderStep(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val steps = itemView.findViewById<AppCompatTextView>(R.id.recipeSteps)
    }

    class ViewHolderTitle(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeTitle = itemView.findViewById<AppCompatTextView>(R.id.recipeTitle)
    }

    class ViewHolderIng(itemView: View) : RecyclerView.ViewHolder(itemView){
        val missedIng = itemView.findViewById<AppCompatTextView>(R.id.missingIngredientsRecipe)
        val usedIng = itemView.findViewById<AppCompatTextView>(R.id.usedIngredientsRecipe)
    }
}