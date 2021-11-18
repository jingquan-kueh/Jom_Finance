package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.databinding.AccountListAdapter
import com.example.jom_finance.models.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_accounts_list.*


class AccountsListActivity : AppCompatActivity(), AccountListAdapter.OnItemClickListener {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var userID : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var accountArrayList : ArrayList<Account>
    private lateinit var accountListAdapter : AccountListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts_list)

        backBtn_accounts.setOnClickListener {
            finish()
        }

        setUpdb()
        recyclerView = accounts_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        accountArrayList= arrayListOf()
        accountListAdapter = AccountListAdapter(accountArrayList, this)
        recyclerView.adapter = accountListAdapter
        EventChangeListener()

        transferAmount_btn.setOnClickListener {
            val intent = Intent(this, TransferActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        addNewAccount_btn.setOnClickListener {
            val intent = Intent(this, AddNewAccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("accounts/$userID/account_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            accountArrayList.add(dc.document.toObject(Account::class.java))
                        }
                    }
                    accountListAdapter.notifyDataSetChanged()
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onItemClick(position: Int) {
        val item = accountArrayList[position]
        val intent = Intent(this, AccountDetailsActivity::class.java)
        intent.putExtra("account_name", item.accountName)
        intent.putExtra("account_amount", item.accountAmount)
        intent.putExtra("account_icon", item.accountIcon)
        intent.putExtra("account_color", item.accountColor)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

}