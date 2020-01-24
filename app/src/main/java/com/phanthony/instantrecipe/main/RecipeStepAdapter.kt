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

class RecipeStepAdapter(var stepList: ArrayList<RecipeInstruction>): RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            0 -> {
                holder as ViewHolderTitle
                val current = stepList[position]
                holder.recipeTitle.text = current.name
            }
            1 -> {
                holder as ViewHolderStep
                val current = stepList[position]
                if(current.name.isNotBlank()) {
                    holder.stepTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
                    holder.stepTitle.text = current.name
                }
                else{
                    holder.stepTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0f)
                }
                var steps = ""
                current.steps.forEach {
                    steps += "${it.number}. ${it.step}\n"
                }
                holder.steps.text = steps}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            1 -> {
                val layout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_step_layout, parent, false)
                ViewHolderStep(layout)
            }
            else -> {
                val layout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_step_title, parent, false)
                ViewHolderTitle(layout)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0 -> 0
            else -> 1
        }
    }

    override fun getItemCount(): Int {
        return stepList.size
    }

    fun addAll(list: List<RecipeInstruction>?){
        if(list != null) {
            stepList.addAll(list)
            notifyDataSetChanged()
        }
    }

    fun clear(){
        stepList.clear()
        notifyDataSetChanged()
    }

    fun add(single: RecipeInstruction?){
        if(single != null) {
            stepList.add(single)
        }
        notifyDataSetChanged()
    }

    class ViewHolderStep(itemView: View) : RecyclerView.ViewHolder(itemView){
        val stepTitle = itemView.findViewById<AppCompatTextView>(R.id.stepTitle)
        val steps = itemView.findViewById<AppCompatTextView>(R.id.recipeSteps)
    }

    class ViewHolderTitle(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recipeTitle = itemView.findViewById<AppCompatTextView>(R.id.recipeTitle)
    }
}