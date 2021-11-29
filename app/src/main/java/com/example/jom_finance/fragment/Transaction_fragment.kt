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
import com.example.jom_finance.ExpenseDetailActivity
import com.example.jom_finance.R
import com.example.jom_finance.models.Transaction
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.income.DetailIncome
import com.example.jom_finance.models.Category
import com.example.jom_finance.report.FinancialReportActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.mlsdev.animatedrv.AnimatedRecyclerView
import kotlinx.android.synthetic.main.fragment_home_fragment.view.*
import kotlinx.android.synthetic.main.fragment_transaction_fragment.view.*
import java.text.SimpleDateFormat

class Transaction_fragment : Fragment(),TransactionListAdapter.OnItemClickListener {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var recyclerView: AnimatedRecyclerView
    private lateinit var transactionArrayList : ArrayList<Transaction>
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryHash : HashMap<String, Category>
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
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        view.filterBtn.setOnClickListener{
            openAttachmentBottomSheetDialog(it)
        }
        setUpdb()
        recyclerView = view.transaction_recyclerView
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
    private fun openAttachmentBottomSheetDialog(view : View) {
        val bottomSheet = BottomSheetDialog(view.context)
        bottomSheet.setContentView(R.layout.bottomsheet_filter)
        bottomSheet.show()
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
                        if(dc.type == DocumentChange.Type.ADDED)
                            transactionArrayList.add(dc.document.toObject(Transaction::class.java))
                    }
                }
            })

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
            val dateFormatName = SimpleDateFormat("dd MMMM yyyy")
            val dateFormatNum = SimpleDateFormat("dd-MM-yyyy")
            val timeFormat = SimpleDateFormat("hh:mm")
            val date = item.transactionTime?.toDate()
            if(type == "income"){
                val intent = Intent(this, DetailIncome::class.java)
                intent.putExtra("transactionName",item.transactionName)
                intent.putExtra("transactionName",item.transactionName)
                intent.putExtra("transactionAmount",item.transactionAmount)
                intent.putExtra("transactionCategory",item.transactionCategory)
                intent.putExtra("transactionAccount",item.transactionAccount)
                intent.putExtra("transactionDescription",item.transactionDescription)
                intent.putExtra("transactionAttachment",item.transactionAttachment)
                intent.putExtra("transactionDateName", dateFormatName.format(date))
                intent.putExtra("transactionDateNum", dateFormatNum.format(date))
                intent.putExtra("transactionTime", timeFormat.format(date))
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }else{
                val intent = Intent(this,ExpenseDetailActivity::class.java)
                intent.putExtra("transactionName",item.transactionName)
                intent.putExtra("transactionAmount",item.transactionAmount)
                intent.putExtra("transactionCategory",item.transactionCategory)
                intent.putExtra("transactionAccount",item.transactionAccount)
                intent.putExtra("transactionDescription",item.transactionDescription)
                intent.putExtra("transactionAttachment",item.transactionAttachment)
                intent.putExtra("transactionDateName", dateFormatName.format(date))
                intent.putExtra("transactionDateNum", dateFormatNum.format(date))
                intent.putExtra("transactionTime", timeFormat.format(date))
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

}