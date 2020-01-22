package com.phanthony.instantrecipe.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.phanthony.instantrecipe.R

class IngredientAdapter(var ingList: MutableList<String>, private val viewModel: RecipeViewModel): RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_layout,parent,false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return ingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = ingList[position]
        holder.name.text = current
        holder.button.setOnClickListener{
            deleteIng(current)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<AppCompatTextView>(R.id.ingredientName)!!
        val button = itemView.findViewById<AppCompatImageButton>(R.id.removeButton)!!
    }

    fun addAll(set: MutableSet<String>){
        for(i in set){
            ingList.add(i)
        }
        notifyDataSetChanged()
    }

    fun clear(){
        ingList.clear()
        notifyDataSetChanged()
    }

    fun deleteIng(item : String){
        val found = ingList.find {
            item == it
        }
        val newSet = viewModel.getIngList().value!!
        newSet.remove(found)
        viewModel.setIngList(newSet)
    }
}