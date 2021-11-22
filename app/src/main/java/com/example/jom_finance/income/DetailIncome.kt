package com.example.jom_finance.income

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.core.view.isVisible
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.R
import com.google.android.material.bottomsheet.BottomSheetDialog
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

        deleteIncomeBtn.setOnClickListener{
            if (transactionID != null) {
                openDeleteBottomSheetDialog(transactionID)
            }

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
    private fun openDeleteBottomSheetDialog(transactionID : String) {
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_delete)
        val yesBtn = bottomSheet.findViewById<Button>(R.id.removeYesbtn) as Button
        val noBtn = bottomSheet.findViewById<Button>(R.id.removeNobtn) as Button

        yesBtn.setOnClickListener{
            if (transactionID != null) {
                bottomSheet.dismiss()
                db.collection("transaction/$userID/Transaction_detail").document(transactionID)
                    .delete()
                    .addOnCompleteListener{
                        db.collection("transaction").document(userID)
                            .get()
                            .addOnCompleteListener{ value ->
                                val income_amount : Double = value.result["Income"].toString().toDouble()
                                val newIncomeAmount : Double = income_amount - amount

                                //Update Income Amount
                                db.collection("transaction").document(userID)
                                    .update("Income",newIncomeAmount)
                                    .addOnCompleteListener{
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

            }
        }

        noBtn.setOnClickListener{
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

}