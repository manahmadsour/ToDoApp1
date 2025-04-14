package com.example.todoapp

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)

        fun bind(category: Category) {
            titleTextView.text = category.title
            descriptionTextView.text = category.description

            // Safely parse textColor
            val safeTextColor = try {
                android.graphics.Color.parseColor(category.textColor)
            } catch (e: IllegalArgumentException) {
                android.util.Log.e(
                    "CategoryAdapter",
                    "Invalid textColor: '${category.textColor}'",
                    e
                )
                android.graphics.Color.BLACK // default fallback
            }
            titleTextView.setTextColor(safeTextColor)
            descriptionTextView.setTextColor(safeTextColor)

            // Safely parse background color
            val backgroundColor = try {
                android.graphics.Color.parseColor(category.color)
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("CategoryAdapter", "Invalid color: '${category.color}'", e)
                android.graphics.Color.LTGRAY // fallback color
            }

            val drawable: Drawable = itemView.context.getDrawable(R.drawable.category_card_bg)!!
            val newDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(newDrawable, backgroundColor)
            itemView.background = newDrawable

            // Load icon safely
            val iconResource = itemView.context.resources.getIdentifier(
                category.iconUrl, "drawable", itemView.context.packageName
            )
            if (iconResource != 0) {
                iconImageView.setImageResource(iconResource)
            } else {
                iconImageView.setImageResource(R.drawable.category_ic)
            }

            // Click event
            itemView.setOnClickListener { onCategoryClick(category) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }


}
