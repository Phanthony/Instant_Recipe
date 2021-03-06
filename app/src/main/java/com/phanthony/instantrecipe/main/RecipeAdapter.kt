package com.phanthony.instantrecipe.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.database.SpoonacularResult

class RecipeAdapter(val context: Context, val findRecipe: (recipeId: Int) -> Unit) :
    PagedListAdapter<SpoonacularResult, RecipeAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<SpoonacularResult>() {
            override fun areItemsTheSame(oldItem: SpoonacularResult, newItem: SpoonacularResult): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SpoonacularResult, newItem: SpoonacularResult): Boolean {
                return oldItem == newItem
            }
        }
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_layout, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)
        holder.name.text = current?.title ?: ""
        if (current != null) {
            Glide.with(context).load(current.image).apply(RequestOptions().circleCrop()).into(holder.image)
            holder.view.setOnClickListener {
                findRecipe(current.id)
            }
            holder.id.text = current.id.toString()
        } else {
            holder.image.background = context.getDrawable(R.drawable.placeholder)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<AppCompatTextView>(R.id.recipeName)
        val image = itemView.findViewById<AppCompatImageView>(R.id.recipeImage)
        val view = itemView.findViewById<LinearLayout>(R.id.recipeView)
        val id = itemView.findViewById<AppCompatTextView>(R.id.recipeId)
    }
}