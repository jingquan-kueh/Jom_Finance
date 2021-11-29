package com.example.jom_finance.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dibyendu.picker.listener.PickerListener
import com.dibyendu.picker.util.PickerUtils
import com.dibyendu.picker.view.MonthYearPickerDialog
import com.example.jom_finance.BudgetDetailActivity
import com.example.jom_finance.CreateBudgetActivity
import com.example.jom_finance.R
import com.example.jom_finance.databinding.BudgetListAdapter
import com.example.jom_finance.models.Budget
import com.example.jom_finance.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.fragment_budget_fragment.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Budget_fragment : Fragment(), BudgetListAdapter.OnItemClickListener {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userID: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var budgetArrayList: ArrayList<Budget>
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryHash : HashMap<String, Category>
    private lateinit var budgetListAdapter: BudgetListAdapter

    private lateinit var month: String
    private lateinit var year: String
    private lateinit var budgetDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_budget_fragment, container, false)

        val monthDate = SimpleDateFormat("MMMM")
        val yearDate = SimpleDateFormat("yyyy")
        month = monthDate.format(Date())
        year = yearDate.format(Date())
        budgetDate = "$month $year"
        view.budgetDate_text.text = budgetDate

        setupDatabase()
        recyclerView = view.budget_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        budgetArrayList = arrayListOf()
        categoryArrayList = arrayListOf()
        categoryHash = hashMapOf()
        budgetListAdapter = BudgetListAdapter(budgetArrayList, categoryHash, this)
        recyclerView.adapter = budgetListAdapter
        eventChangeListener()

        view.budgetDate_text.setOnClickListener {
            requireActivity().run {
                MonthYearPickerDialog.show(context = this, listener = object : PickerListener {
                    override fun onSetResult(calendar: Calendar) {
                        month = PickerUtils.getMonth(calendar, PickerUtils.Format.LONG)!!
                        year = PickerUtils.getYear(calendar)!!
                        budgetDate = "$month $year"
                        view.budgetDate_text.text = budgetDate
                        budgetArrayList.clear()
                        eventChangeListener()
                        if (budgetArrayList.isEmpty())
                            budgetListAdapter.notifyDataSetChanged()
                    }
                })
            }
        }

        view.createBudget_btn.setOnClickListener {
            requireActivity().run {
                val intent = Intent(this, CreateBudgetActivity::class.java)
                intent.putExtra("budget_date", budgetDate)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        return view
    }


    private fun eventChangeListener() {
        db.collection("budget/$userID/budget_detail").whereEqualTo("budget_date", budgetDate)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?,
                ) {
                    if (error != null) {
                        Log.e("FireStore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED)
                            budgetArrayList.add(dc.document.toObject(Budget::class.java))
                        else if (dc.type == DocumentChange.Type.REMOVED)
                            budgetArrayList.remove(dc.document.toObject(Budget::class.java))

                        budgetListAdapter.notifyDataSetChanged()
                    }
                }
            })

        db.collection("category/$userID/category_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error != null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            categoryArrayList.add(dc.document.toObject(Category::class.java))
                            categoryHash[categoryArrayList.last().categoryName.toString()] = categoryArrayList.last()
                        }
                    }
                    budgetListAdapter.notifyDataSetChanged()
                }
            })
    }

    private fun setupDatabase() {
        fAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }

    override fun onItemClick(position: Int) {
        val item = budgetArrayList[position]
        val category = categoryHash[item.budgetCategory]
        requireActivity().run{
            val intent = Intent(this, BudgetDetailActivity::class.java)
            intent.putExtra("budget_id", item.budgetID)
            intent.putExtra("budget_amount", item.budgetAmount)
            intent.putExtra("budget_date", item.budgetDate)
            intent.putExtra("budget_category", item.budgetCategory)
            intent.putExtra("budget_alert", item.budgetAlert)
            intent.putExtra("budget_alert_percentage", item.budgetAlertPercentage)
            intent.putExtra("budget_spent", item.budgetSpent)
            intent.putExtra("category_color", category?.categoryColor)
            intent.putExtra("category_icon", category?.categoryIcon)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

}
