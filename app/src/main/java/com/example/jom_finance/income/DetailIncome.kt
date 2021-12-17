package com.example.jom_finance.income

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.R
import com.example.jom_finance.models.Account
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_detail_income.*
import kotlinx.android.synthetic.main.activity_expense_detail.*
import java.io.File
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

private lateinit var fAuth: FirebaseAuth
private lateinit var userID: String
private lateinit var db: FirebaseFirestore

private lateinit var transactionID: String
private var amount by Delegates.notNull<Double>()
private lateinit var category : String
private lateinit var description : String
private lateinit var account : String
private var attachment : Boolean = false
private lateinit var fileType: String
private lateinit var localFile : File
private lateinit var fileName: String
private lateinit var dateName: String
private lateinit var dateNum: String
private lateinit var time : String

class DetailIncome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_income)
        setUpdb()
        // Get intent string from recycleview
        val transactionID = intent.getStringExtra("transactionName")
        if(transactionID != "null") {
            amount = intent.extras?.getDouble("transactionAmount")!!
            category = intent.extras?.getString("transactionCategory").toString()
            description = intent.extras?.getString("transactionDescription").toString()
            account = intent.extras?.getString("transactionAccount").toString()
            attachment = intent.extras?.getBoolean("transactionAttachment")!!
            dateName = intent.extras?.getString("transactionDateName").toString()
            dateNum = intent.extras?.getString("transactionDateNum").toString()
            time = intent.extras?.getString("transactionTime").toString()


            dateIncome.text = "$dateName | "
            timeIncome.text = "$time"
            amountIncome.text = "RM " + amount
            incomeCategoryDetail_text.text = category
            incomeAccountDetail_text.text = account
            incomeDescriptionDetail_text.text = description

            incomeAttachmentLabel_text.isVisible = attachment

            incomeDetailAttachmentDocument_txt.isVisible = false
            attachment_img.isVisible = false

            if (attachment) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Fetching attachment...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                val storageReference = FirebaseStorage.getInstance().getReference("transaction_images/$userID/$transactionID")

                storageReference.metadata.addOnSuccessListener { metadata->
                    fileType = metadata.getCustomMetadata("file_type")!!
                    if (fileType == "document"){
                        incomeDetailAttachmentDocument_txt.isVisible = true
                        localFile = File.createTempFile("tempDoc", ".pdf")
                        storageReference.getFile(localFile).addOnSuccessListener {
                            if(progressDialog.isShowing)
                                progressDialog.dismiss()

                            fileName = metadata.getCustomMetadata("file_name")!!
                            incomeDetailAttachmentDocument_txt.text = fileName

                            incomeDetailAttachmentDocument_txt.setOnClickListener {
                                val uri = FileProvider.getUriForFile(this, this.applicationContext.packageName+".fileprovider", localFile)
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = uri
                                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                startActivity(intent)
                            }
                        }
                    }
                    else{
                        attachment_img.isVisible = true
                        localFile = File.createTempFile("tempImage", "jpg")
                        storageReference.getFile(localFile).addOnSuccessListener {
                            if(progressDialog.isShowing)
                                progressDialog.dismiss()

                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            attachment_img.setImageBitmap(bitmap)
                        }
                    }
                }
            }
        }
        backBtn.setOnClickListener{
            finish()
        }

        editIncome_btn.setOnClickListener {
            val intent = Intent(this, AddNewIncome::class.java)
            intent.putExtra("editIncome", true)
            intent.putExtra("incomeID", transactionID)
            intent.putExtra("incomeAmount", amount)
            intent.putExtra("incomeCategory", category)
            intent.putExtra("incomeDescription", description)
            intent.putExtra("incomeAccount", account)
            intent.putExtra("incomeAttachment", attachment)
            intent.putExtra("transactionDateNum", attachment)
            intent.putExtra("transactionTime", attachment)
            intent.putExtra("transactionDateNum", dateNum)
            intent.putExtra("transactionTime", time)
            if (attachment){
                intent.putExtra("attachmentType", fileType)
                intent.putExtra("attachmentPath", localFile.absolutePath)
                if (fileType == "document")
                    intent.putExtra("attachmentName", fileName)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        deleteIncomeBtn.setOnClickListener {
            if (transactionID != null) {
                openDeleteBottomSheetDialog(transactionID)
            }

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

    private fun openDeleteBottomSheetDialog(transactionID: String) {
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_delete)
        val yesBtn = bottomSheet.findViewById<Button>(R.id.removeYesbtn) as Button
        val noBtn = bottomSheet.findViewById<Button>(R.id.removeNobtn) as Button

        yesBtn.setOnClickListener {
            if (transactionID != null) {
                bottomSheet.dismiss()
                db.collection("transaction/$userID/Transaction_detail").document(transactionID)
                    .delete()
                    .addOnCompleteListener {
                        updateIncomeValue()
                        updateAccount()

                        val resetView =
                            LayoutInflater.from(this).inflate(R.layout.popup_remove_success, null)
                        val resetViewBuilder =
                            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                                .setView(resetView)
                        val displayDialog = resetViewBuilder.show()
                        displayDialog.setOnDismissListener {
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left)
                            finishAffinity()
                        }


                    }

            }
        }

        noBtn.setOnClickListener {
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

    private fun updateAccount() {
        db.collection("accounts/$userID/account_detail").document(account)
            .get()
            .addOnCompleteListener { value ->
                val account_amount: Double =
                    value.result["account_amount"].toString().toDouble()
                val newAccountAmount: Double = account_amount - amount

                //Update Income Amount
                db.collection("accounts/$userID/account_detail").document(account)
                    .update("account_amount", newAccountAmount)
                updateTotalAccountAmount()
            }

    }

    private fun updateTotalAccountAmount() {
        var totalAccountAmount = 0.0
        db.collection("accounts/$userID/account_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("FireStore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            totalAccountAmount += dc.document.toObject(Account::class.java).accountAmount!!
                        }
                    }
                    if (totalAccountAmount != 0.0) {
                        db.collection("accounts").document(userID)
                            .update("Total", totalAccountAmount)
                    }
                }
            })

    }

    private fun updateIncomeValue() {
        db.collection("transaction").document(userID)
            .get()
            .addOnCompleteListener { value ->
                val income_amount: Double =
                    value.result["Income"].toString().toDouble()
                val newIncomeAmount: Double = income_amount - amount

                //Update Income Amount
                db.collection("transaction").document(userID)
                    .update("Income", newIncomeAmount)
            }

    }
}