package com.example.todoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity: AppCompatActivity() {
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val db = FirebaseFirestore.getInstance()
    private val categories = mutableListOf<Category>()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



        userId = intent.getStringExtra("UID")

        if (userId == null) {
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        categoryAdapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("categoryId", category.id)
            intent.putExtra("title", category.title)
            intent.putExtra("UID", userId)
            startActivity(intent)
        }
        categoryRecyclerView.layoutManager = GridLayoutManager(this, 2)
        categoryRecyclerView.adapter = categoryAdapter

        // Fetch categories in real-time
        loadCategories()

        findViewById<View>(R.id.addCategoryBtn).setOnClickListener { showAddCategoryPopup() }
        findViewById<View>(R.id.logoutIcon).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<View>(R.id.calendarBtn).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            intent.putExtra("uid", userId)
            startActivity(intent)
        }


    }

    // Fetch categories from Firestore in real-time
    private fun loadCategories() {
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching categories", e)
                    Toast.makeText(this, "Failed to load categories.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                categories.clear()
                for (document in snapshots!!) {
                    val category = document.toObject(Category::class.java)
                    categories.add(category)
                }
                categoryAdapter.notifyDataSetChanged()
            }
    }

    // Show a popup to add a new category
    private fun showAddCategoryPopup() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_category, null)

        val titleEditText = dialogView.findViewById<EditText>(R.id.categoryTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.categoryDescription)
        val colorEditText = dialogView.findViewById<EditText>(R.id.categoryColor)

        builder.setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val color = colorEditText.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty() && color.isNotEmpty()) {
                    addCategory(title, description, color)
                } else {
                    Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    // Add new category to Firestore
    private fun addCategory(title: String, description: String, color: String) {
        if (userId == null) {
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val newCategoryId = db.collection("categories").document().id
        val newCategory = Category(
            id = newCategoryId,
            userId = userId!!,
            title = title,
            description = description,
            color = color,
            textColor = "#FFFFFF"
        )

        db.collection("categories")
            .document(newCategoryId)
            .set(newCategory)
            .addOnSuccessListener {
                Log.d("Firestore", "Category added successfully: $newCategoryId")
                Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding category", e)
                Toast.makeText(this, "Failed to add category.", Toast.LENGTH_SHORT).show()
            }
    }
}


