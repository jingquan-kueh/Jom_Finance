package com.example.jom_finance

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.jom_finance.models.Account
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_expense_detail.*
import java.io.File
import kotlin.properties.Delegates

private lateinit var fAuth: FirebaseAuth
private lateinit var userID: String
private lateinit var db: FirebaseFirestore

private lateinit var accountArrayList : java.util.ArrayList<Account>

private lateinit var transactionID: String
private var amount by Delegates.notNull<Double>()
private lateinit var category: String
private lateinit var description: String
private lateinit var account: String
private var attachment: Boolean = false
private lateinit var fileType: String
private lateinit var localFile : File
private lateinit var fileName: String
private lateinit var dateName: String
private lateinit var dateNum: String
private lateinit var time: String

class ExpenseDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail)
        setUpdb()
        accountArrayList = arrayListOf()
        // Get intent string from recycleview
        transactionID = intent.getStringExtra("transactionName").toString()
        if (transactionID != "null") {
            amount = intent.extras?.getDouble("transactionAmount")!!
            category = intent.extras?.getString("transactionCategory").toString()
            description = intent.extras?.getString("transactionDescription").toString()
            account = intent.extras?.getString("transactionAccount").toString()
            attachment = intent.extras?.getBoolean("transactionAttachment")!!
            dateName = intent.extras?.getString("transactionDateName").toString()
            dateNum = intent.extras?.getString("transactionDateNum").toString()
            time = intent.extras?.getString("transactionTime").toString()

            expenseTimeDetail_text.text = "$dateName | $time"
            expenseAmountDetail_text.text = "RM " + String.format("%.2f", amount)
            expenseCategoryDetail_text.text = category
            expenseAccountDetail_text.text = account
            expenseDescriptionDetail_text.text = description

            expenseAttachmentLabel_text.isVisible = attachment

            expenseDetailAttachmentDocument_txt.isVisible = false
            expenseDetailAttachmentImage_img.isVisible = false


            if (attachment){
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Fetching attachment...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                val storageReference = FirebaseStorage.getInstance().getReference("transaction_images/$userID/$transactionID")


                storageReference.metadata.addOnSuccessListener { metadata->
                    fileType = metadata.getCustomMetadata("file_type")!!
                    if (fileType == "document"){
                        expenseDetailAttachmentDocument_txt.isVisible = true
                        localFile = File.createTempFile("tempDoc", ".pdf")
                        storageReference.getFile(localFile).addOnSuccessListener {
                            if(progressDialog.isShowing)
                                progressDialog.dismiss()

                            fileName = metadata.getCustomMetadata("file_name")!!
                            expenseDetailAttachmentDocument_txt.text = fileName

                            expenseDetailAttachmentDocument_txt.setOnClickListener {
                                val uri = FileProvider.getUriForFile(this, this.applicationContext.packageName+".fileprovider", localFile)
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = uri
                                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                startActivity(intent)
                            }
                        }
                    }
                    else{
                        expenseDetailAttachmentImage_img.isVisible = true
                        localFile = File.createTempFile("tempImage", "jpg")
                        storageReference.getFile(localFile).addOnSuccessListener {
                            if(progressDialog.isShowing)
                                progressDialog.dismiss()

                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            expenseDetailAttachmentImage_img.setImageBitmap(bitmap)
                        }
                    }
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

        deleteExpense_btn.setOnClickListener {
            openDeleteBottomSheetDialog()
        }
    }

    private fun openDeleteBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_delete)
        val yesBtn = bottomSheet.findViewById<Button>(R.id.removeYesbtn) as Button
        val noBtn = bottomSheet.findViewById<Button>(R.id.removeNobtn) as Button
        val title = bottomSheet.findViewById<TextView>(R.id.bottomsheetDeleteTitle_text) as TextView
        val description = bottomSheet.findViewById<TextView>(R.id.bottomsheetDeleteDesc_text) as TextView

        title.text = "Remove this transaction?"
        description.text = "Are you sure you want to remove this transaction?"

        yesBtn.setOnClickListener{
            db.collection("transaction/$userID/Transaction_detail").document(transactionID)
                .delete()
                .addOnSuccessListener {
                    updateAccount()
                    updateBudget()
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

        noBtn.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()

    }

    private fun updateAccount(){
        val accountRef = db.collection("accounts/$userID/account_detail").document(account)

        accountRef
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    var accountAmount = document.data?.getValue("account_amount").toString().toDouble()
                    accountAmount += amount
                    accountRef
                        .update("account_amount", accountAmount)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Updated account", Toast.LENGTH_SHORT).show()
                            updateTotalAccountAmount()
                        }
                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error updating account amount", it)
            }
    }

    private fun updateTotalAccountAmount(){
        var totalAccountAmount = 0.0
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
                            totalAccountAmount += accountArrayList.last().accountAmount!!
                        }
                    }
                    if(totalAccountAmount != 0.0){
                        db.collection("accounts").document(userID)
                            .update("Total",totalAccountAmount)
                    }
                }
            })
    }

    private fun updateBudget(){
        val dateArray = dateName.split(" ").toTypedArray()
        val budgetDate =  dateArray[1] + " " + dateArray[2]

        val budgetRef = db.collection("budget/$userID/budget_detail")

        budgetRef
            .whereEqualTo("budget_date", budgetDate)
            .whereEqualTo("budget_category", category)
            .get()
            .addOnSuccessListener {
                for (document in it.documents) {
                    if (document.exists()) {
                        val budgetID = document.getString("budget_id")!!
                        var budgetSpent =
                            document.data?.getValue("budget_spent").toString().toDouble()
                        budgetSpent -= amount
                        budgetRef.document(budgetID)
                            .update("budget_spent", budgetSpent)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Updated budget", Toast.LENGTH_SHORT).show()
                            }
                    }
                    else
                        Toast.makeText(this, "There is no budget for this category", Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error updating budget", it)
            }
    }


    //get PDF file name
    private fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun Context.getContentFileName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                .let(cursor::getString)
        }
    }.getOrNull()

    private fun setUpdb() {
        fAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }

}