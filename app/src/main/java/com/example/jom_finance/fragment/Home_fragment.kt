package com.example.jom_finance.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.jom_finance.R
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.income.DetailIncome
import com.example.jom_finance.models.Category
import com.example.jom_finance.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.mlsdev.animatedrv.AnimatedRecyclerView
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import kotlinx.android.synthetic.main.fragment_home_fragment.view.*
import java.text.DecimalFormat


class Home_fragment : Fragment(),TransactionListAdapter.OnItemClickListener{

    private lateinit var fAuth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var recyclerView: AnimatedRecyclerView
    private lateinit var transactionArrayList : ArrayList<Transaction>
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryHash : HashMap<String,Category>
    private lateinit var transactionListAdapter: TransactionListAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUpdb()
        readDB()
        val view : View = inflater.inflate(R.layout.fragment_home_fragment, container, false)
        recyclerView = view.home_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        transactionArrayList = arrayListOf()
        categoryArrayList = arrayListOf()
        categoryHash = hashMapOf()
        transactionListAdapter = TransactionListAdapter(transactionArrayList,categoryHash,this)
        recyclerView.adapter = transactionListAdapter

        EventChangeListener()
        // Inflate the layout for this fragment
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun readDB() {
        db = FirebaseFirestore.getInstance()
        db.collection("transaction").document(userID).get().addOnCompleteListener{
            val income_amount : Double = it.result["Income"].toString().toDouble()
            val expense_amount : Double = it.result["Expense"].toString().toDouble()
            val balance_amount : Double = income_amount - expense_amount
            home_income_amount.text = String.format("RM %.2f",income_amount)
            home_expenses_amount.text =String.format("RM %.2f",expense_amount)
            home_balance.text = String.format("RM %.2f",balance_amount)
        }
    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("transaction/$userID/Transaction_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                                transactionArrayList.add(dc.document.toObject(Transaction::class.java))
                        }
                    }
                }
            })

        db.collection("category/$userID/Category_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            categoryArrayList.add(dc.document.toObject(Category::class.java))
                            categoryHash[categoryArrayList.last().categoryName.toString()] = categoryArrayList.last()
                        }
                    }
                    transactionListAdapter.notifyDataSetChanged()
                    recyclerView.scheduleLayoutAnimation()
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
        val item = transactionArrayList[position]
        requireActivity().run {
            val type = item.transactionType
            if(type == "income"){
                val intent = Intent(this, DetailIncome::class.java)
                intent.putExtra("transactionName",item.transactionName)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }else{
                val intent = Intent(this, DetailIncome::class.java)
                intent.putExtra("transactionName",item.transactionName)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }


}