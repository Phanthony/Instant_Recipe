package com.phanthony.instantrecipe.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.RecipeSteps

class RecipeStepAdapter(var stepList: ArrayList<RecipeInstruction>): RecyclerView.Adapter<RecipeStepAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_step_layout, parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return stepList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = stepList[position]
        if(current.name.isNotBlank()) {
            holder.stepTitle.text = current.name
        }
        var steps = ""
        current.steps.forEach {
            steps += "${it.number}. ${it.step}\n"
        }
        holder.steps.text = steps
    }

    fun addAll(list: List<RecipeInstruction>){
        stepList.addAll(list)
        notifyDataSetChanged()
    }

    fun clear(){
        stepList.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val stepTitle = itemView.findViewById<AppCompatTextView>(R.id.stepTitle)
        val steps = itemView.findViewById<AppCompatTextView>(R.id.recipeSteps)
    }
}