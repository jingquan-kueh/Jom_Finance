package com.example.jom_finance

import android.content.Intent
import android.graphics.Color
import android.graphics.Color.WHITE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.income.DetailIncome
import com.example.jom_finance.models.Account
import com.example.jom_finance.models.Category
import com.example.jom_finance.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import com.mlsdev.animatedrv.AnimatedRecyclerView
import kotlinx.android.synthetic.main.activity_account_details.*
import kotlinx.android.synthetic.main.fragment_home_fragment.view.*
import java.text.SimpleDateFormat

class AccountDetailsActivity : AppCompatActivity(), TransactionListAdapter.OnItemClickListener {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var accountName: String
    private var accountAmount: Double = 0.0
    private var accountIcon: Int = 278
    private var accountColor: Int = Color.BLACK

    private lateinit var accountArrayList : java.util.ArrayList<Account>

    private lateinit var recyclerView: AnimatedRecyclerView
    private lateinit var transactionArrayList : ArrayList<Transaction>
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryHash : HashMap<String, Category>
    private lateinit var transactionListAdapter: TransactionListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)
        setupDatabase()
        accountArrayList = arrayListOf()
        setFavourite_btn.visibility = View.GONE

        //Icon loader
        val loader = IconPackLoader(this)
        val iconPack = createDefaultIconPack(loader)

        accountName = intent?.extras?.getString("account_name").toString()
        if(accountName != "null") {
            accountAmount = intent?.extras?.getDouble("account_amount")!!
            accountIcon = intent?.extras?.getInt("account_icon")!!
            accountColor = intent?.extras?.getInt("account_color")!!

            accountDetails_bg.setBackgroundColor(accountColor)

            val drawable = iconPack.getIconDrawable(accountIcon, IconDrawableLoader(this))
            accountDetailIcon_img.setImageDrawable(drawable)
            accountDetailIcon_img.setColorFilter(WHITE)

            accountDetailName_text.text = accountName
            accountDetailBalance_text.text = "RM " + String.format("%.2f", accountAmount)

        }

        recyclerView = accountDetail_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        transactionArrayList = arrayListOf()
        categoryArrayList = arrayListOf()
        categoryHash = hashMapOf()
        transactionListAdapter = TransactionListAdapter(transactionArrayList,categoryHash,this)
        recyclerView.adapter = transactionListAdapter
        EventChangeListener()

        editAccount_btn.setOnClickListener {
            val intent = Intent(this, AddNewAccountActivity::class.java)
            intent.putExtra("account_name", accountName)
            intent.putExtra("account_amount", accountAmount)
            intent.putExtra("account_icon", accountIcon)
            intent.putExtra("account_color", accountColor)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        deleteAccount_btn.setOnClickListener {
            openDeleteBottomSheetDialog()
        }

        setFavourite_btn.setOnClickListener{
            setFavourite()
        }

        backBtn_AccountDetail.setOnClickListener {
            val intent = Intent(this, AccountsListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }

    private fun EventChangeListener() {
        // TODO : Set Descending Order and Limit few recent transactions
        fStore.collection("transaction/$userID/Transaction_detail")
            .whereEqualTo("Transaction_account", accountName)
            .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
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

        fStore.collection("category/$userID/category_detail")
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

    private fun openDeleteBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_delete)
        val yesBtn = bottomSheet.findViewById<Button>(R.id.removeYesbtn) as Button
        val noBtn = bottomSheet.findViewById<Button>(R.id.removeNobtn) as Button
        val title = bottomSheet.findViewById<TextView>(R.id.bottomsheetDeleteTitle_text) as TextView
        val description = bottomSheet.findViewById<TextView>(R.id.bottomsheetDeleteDesc_text) as TextView

        title.text = "Remove this account?"
        description.text = "Are you sure you want to remove this account?"

        yesBtn.setOnClickListener {
            fStore.collection("accounts/$userID/account_detail").document(accountName)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                    updateTotalAccountAmount()
                    val intent = Intent(this, AccountsListActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not delete account : $it", Toast.LENGTH_SHORT).show()
                }
        }

        noBtn.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun updateTotalAccountAmount(){
        var totalAccountAmount = 0.0
        fStore.collection("accounts/$userID/account_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            accountArrayList.add(dc.document.toObject(Account::class.java))
                            totalAccountAmount += accountArrayList.last().accountAmount!!
                        }
                    }
                    if(totalAccountAmount != 0.0){
                        fStore.collection("accounts").document(userID)
                            .update("Total",totalAccountAmount)
                    }
                }
            })
    }

    private fun setFavourite(){
        setFavourite_btn.setImageResource(R.drawable.ic_baseline_star_24)
    }

    private fun setupDatabase() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null)
            userID = currentUser.uid

    }

    override fun onItemClick(position: Int) {
        val item = transactionArrayList[position]
        val type = item.transactionType
        val dateFormatName = SimpleDateFormat("dd MMMM yyyy")
        val dateFormatNum = SimpleDateFormat("dd-MM-yyyy")
        val timeFormat = SimpleDateFormat("hh:mm")
        val date = item.transactionTime?.toDate()
        if (type == "income") {
            val intent = Intent(this, DetailIncome::class.java)
            intent.putExtra("transactionName", item.transactionName)
            intent.putExtra("transactionName", item.transactionName)
            intent.putExtra("transactionAmount", item.transactionAmount)
            intent.putExtra("transactionCategory", item.transactionCategory)
            intent.putExtra("transactionAccount", item.transactionAccount)
            intent.putExtra("transactionDescription", item.transactionDescription)
            intent.putExtra("transactionAttachment", item.transactionAttachment)
            intent.putExtra("transactionDateName", dateFormatName.format(date))
            intent.putExtra("transactionDateNum", dateFormatNum.format(date))
            intent.putExtra("transactionTime", timeFormat.format(date))
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            val intent = Intent(this, ExpenseDetailActivity::class.java)
            intent.putExtra("transactionName", item.transactionName)
            intent.putExtra("transactionAmount", item.transactionAmount)
            intent.putExtra("transactionCategory", item.transactionCategory)
            intent.putExtra("transactionAccount", item.transactionAccount)
            intent.putExtra("transactionDescription", item.transactionDescription)
            intent.putExtra("transactionAttachment", item.transactionAttachment)
            intent.putExtra("transactionDateName", dateFormatName.format(date))
            intent.putExtra("transactionDateNum", dateFormatNum.format(date))
            intent.putExtra("transactionTime", timeFormat.format(date))
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

}