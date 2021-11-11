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
import com.example.jom_finance.AccountsListActivity
import com.example.jom_finance.CreateBudgetActivity
import com.example.jom_finance.R
import com.example.jom_finance.databinding.BudgetListAdapter
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.models.Budget
import com.example.jom_finance.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_budget_fragment.view.*
import kotlinx.android.synthetic.main.fragment_transaction_fragment.view.*

class Budget_fragment : Fragment() {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var recyclerView : RecyclerView
    private lateinit var budgetArrayList : ArrayList<Budget>
    private lateinit var budgetListAdapter : BudgetListAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_budget_fragment, container, false)

        view.createBudget_btn.setOnClickListener {
            requireActivity().run{
                val intent = Intent(this, CreateBudgetActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        setUpdb()
        recyclerView = view.budget_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        budgetArrayList = arrayListOf()

        budgetListAdapter = BudgetListAdapter(budgetArrayList)

        recyclerView.adapter = budgetListAdapter
        EventChangeListener()

        return view

    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("budget/$userID/budget_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            budgetArrayList.add(dc.document.toObject(Budget::class.java))
                        }
                    }
                    budgetListAdapter.notifyDataSetChanged()
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
}