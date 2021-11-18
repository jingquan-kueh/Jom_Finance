package com.example.jom_finance.income

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.jom_finance.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail_income.*
import kotlin.properties.Delegates

private lateinit var fAuth : FirebaseAuth
private lateinit var userID : String
private lateinit var db : FirebaseFirestore

private var amount by Delegates.notNull<Double>()
private lateinit var category : String
private lateinit var description : String
private lateinit var account : String
private var attachment : Boolean = false

class DetailIncome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_income)
        setUpdb()
        // Get intent string from recycleview
        val transactionID = intent.getStringExtra("transactionName")
        if(transactionID != null){
            db.collection("transaction/$userID/Transaction_detail").document(transactionID)
                .get().addOnSuccessListener { document ->
                    amount = document.getDouble("Transaction_amount")!!
                    category = document.getString("Transaction_category").toString()
                    description =  document.getString("Transaction_description").toString()
                    account = document.getString("Transaction_account").toString()
                    attachment = document.getBoolean("Transaction_attachment") == true

                    amountIncome.text = "RM "+ amount
                    incomeCategoryDetail_text.text = category
                    incomeAccountDetail_text.text = account
                    incomeDescriptionDetail_text.text = description

                    if(attachment){
                        incomeAttachmentLabel_text.isVisible = true
                        attachment_img.isVisible = true
                        // TODO : Add Image from Firebase
                        // attachment_img.setImageURI()
                    }else{
                        incomeAttachmentLabel_text.isVisible = false
                        attachment_img.isVisible = false
                    }
                }
        }
        editIncome_btn.setOnClickListener{
            val intent = Intent(this, AddNewIncome::class.java)
            intent.putExtra("editIncome",true)
            intent.putExtra("incomeID",transactionID)
            intent.putExtra("incomeAmount",amount)
            intent.putExtra("incomeCategory", category)
            intent.putExtra("incomeDescription", description)
            intent.putExtra("incomeAccount", account)
            intent.putExtra("incomeAttachment", attachment)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    private fun setUpdb(){
        fAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }
}