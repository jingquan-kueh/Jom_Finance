package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.databinding.CategoryListAdapter
import com.example.jom_finance.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_category_list.*

class CategoryListActivity : AppCompatActivity(), CategoryListAdapter.OnItemClickListener {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var userID : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryListAdapter : CategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        addNewCategory_btn.setOnClickListener {
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        setUpdb()
        recyclerView = category_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        categoryArrayList= arrayListOf()
        categoryListAdapter = CategoryListAdapter(categoryArrayList, this)
        recyclerView.adapter = categoryListAdapter
        EventChangeListener()

        backBtn_categoryList.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("fragment_to_load", "profile")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("category/$userID/category_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            categoryArrayList.add(dc.document.toObject(Category::class.java))
                        }
                    }
                    categoryListAdapter.notifyDataSetChanged()
                }
            })
    }

    private fun setUpdb(){
        fAuth = FirebaseAuth.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }

    override fun onItemClick(position: Int) {
        val item = categoryArrayList[position]
        val intent = Intent(this, CategoryActivity::class.java)
        intent.putExtra("category_name", item.categoryName)
        intent.putExtra("category_icon", item.categoryIcon)
        intent.putExtra("category_color", item.categoryColor)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}