package com.example.jom_finance.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.models.Transaction
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.income.DetailIncome
import com.example.jom_finance.report.FinancialReportActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_transaction_fragment.view.*

class Transaction_fragment : Fragment(),TransactionListAdapter.OnItemClickListener {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionArrayList : ArrayList<Transaction>
    private lateinit var transactionListAdapter : TransactionListAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_transaction_fragment, container, false)

        // Inflate the layout for this fragment
        view.toFinancialReportBtn.setOnClickListener{
            requireActivity().run{
                startActivity(Intent(this, FinancialReportActivity::class.java))
                finish()
            }
        }
        setUpdb()
        recyclerView = view.transaction_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        transactionArrayList = arrayListOf()

        transactionListAdapter = TransactionListAdapter(transactionArrayList,this)

        recyclerView.adapter = transactionListAdapter
        EventChangeListener()

        return view
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
                    transactionListAdapter.notifyDataSetChanged()
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
                Toast.makeText(this, "income Clicked", Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(this, DetailIncome::class.java)
                intent.putExtra("transactionName",item.transactionName)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                Toast.makeText(this, "expenses Clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

}