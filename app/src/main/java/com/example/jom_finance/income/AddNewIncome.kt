package com.example.jom_finance.income

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.R
import com.example.jom_finance.models.Income
import com.example.jom_finance.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_new_income.*
import java.lang.Exception
import kotlin.properties.Delegates

class AddNewIncome : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var incomeCategory: String
    private lateinit var incomeDescription: String
    private lateinit var walletType: String
    private lateinit var attachment: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_income)
        setupDataBase()

        val exp = listOf("Shopping", "Groceries", "Transport", "Restaurant")
        val expAdapter = ArrayAdapter(this, R.layout.item_dropdown, exp)
        expenseCategory_autoCompleteTextView.setAdapter(expAdapter)

        ArrayAdapter.createFromResource(
            this,
            R.array.Category,
            R.layout.spinner_list
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_list)
            // Apply the adapter to the spinner
            spinnerCategory.adapter = adapter
        }

        AddnewBtn.setOnClickListener {
            var lastIncome by Delegates.notNull<Int>()
            try {
                fStore.collection("incomes/$userID/Income_detail")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                            lastIncome = it.result.size()  // Get lastIncome Index
                            var newIncome = lastIncome.inc() // LastIncome Increment

                            //Set Transaction Pathway
                            var documentReference =
                                fStore.collection("incomes/$userID/Income_detail").document("income$newIncome")
                            //Get Income Detail
                            var incomeDetail = Income(newIncome.toString(), 100.00)
                            //TODO : Arrange constructor
                            //Insert Income to FireStore
                            documentReference.set(incomeDetail).addOnSuccessListener {
                                var transaction = Transaction(incomeDetail.incomeName,incomeDetail.incomeAmount,"income")
                                documentReference =
                                    fStore.collection("transaction/$userID/Transaction_detail").document("income$newIncome")
                                documentReference.set(transaction).addOnSuccessListener {
                                   val resetView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)
                                   val resetViewBuilder =
                                       AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(resetView)
                                   val displayDialog = resetViewBuilder.show()
                                    displayDialog.setOnDismissListener{
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                        finish()
                                    }
                               }

                            }
                        }
                    }
                //updateIncomeCounter(newIncome)
            } catch (e: Exception) {
                Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateIncomeCounter(lastIncome: Int) {
        val documentReference = fStore.collection("incomes").document(userID)
        documentReference
            .update("income", lastIncome)
            .addOnSuccessListener { document ->
                Toast.makeText(this, "Counter Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, " " + it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDataBase() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }
}