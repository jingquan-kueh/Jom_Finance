package com.example.jom_finance

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_expense_detail.*
import java.io.File
import kotlin.properties.Delegates

private lateinit var fAuth: FirebaseAuth
private lateinit var userID: String
private lateinit var db: FirebaseFirestore

private var amount by Delegates.notNull<Double>()
private lateinit var category: String
private lateinit var description: String
private lateinit var account: String
private var attachment: Boolean = false
private lateinit var date: String
private lateinit var time: String

class ExpenseDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail)
        setUpdb()
        // Get intent string from recycleview
        val transactionID = intent.getStringExtra("transactionName")
        if (transactionID != null) {
            amount = intent.extras?.getDouble("transactionAmount")!!
            category = intent.extras?.getString("transactionCategory").toString()
            description = intent.extras?.getString("transactionDescription").toString()
            account = intent.extras?.getString("transactionAccount").toString()
            attachment = intent.extras?.getBoolean("transactionAttachment")!!
            date = intent.extras?.getString("transactionDate").toString()
            time = intent.extras?.getString("transactionTime").toString()

            expenseTimeDetail_text.text = "$date | $time"
            expenseAmountDetail_text.text = "RM " + amount
            expenseCategoryDetail_text.text = category
            expenseAccountDetail_text.text = account
            expenseDescriptionDetail_text.text = description

            expenseAttachmentLabel_text.isVisible = attachment
            expenseAttachment_img.isVisible = attachment
            if (attachment){
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Fetching image...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                val storageReference = FirebaseStorage.getInstance().getReference("transaction_images/$userID/$transactionID")
                val localFile = File.createTempFile("tempImage", "jpg")
                storageReference.getFile(localFile).addOnSuccessListener {
                    if(progressDialog.isShowing)
                        progressDialog.dismiss()

                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    expenseAttachment_img.setImageBitmap(bitmap)
                }
            }
        }

        editExpense_btn.setOnClickListener {
            val intent = Intent(this, AddNewExpenseActivity::class.java)
            intent.putExtra("transactionName",transactionID)
            intent.putExtra("transactionAmount",amount)
            intent.putExtra("transactionCategory",category)
            intent.putExtra("transactionAccount",account)
            intent.putExtra("transactionDescription",description)
            intent.putExtra("transactionAttachment",attachment)
            intent.putExtra("transactionDate", date)
            intent.putExtra("transactionTime", time)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun setUpdb() {
        fAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }

}