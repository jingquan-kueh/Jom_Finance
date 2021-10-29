package com.example.jom_finance.income

import android.app.AlertDialog
import android.content.ClipDescription
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.LoginActivity
import com.example.jom_finance.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_new_income.*
import kotlinx.android.synthetic.main.fragment_profile_fragment.*
import java.lang.Exception
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.HashMap
import kotlin.properties.Delegates

class AddNewIncome : AppCompatActivity() {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String

    private lateinit var incomeCategory : String
    private lateinit var incomeDescription : String
    private lateinit var walletType : String
    private lateinit var attachment : String

    private var lastIncome = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_income)
        setupDataBase()
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


        AddnewBtn.setOnClickListener{
            try {

                var newIncome = lastIncome.inc()
                Toast.makeText(this,"New Income : $newIncome", Toast.LENGTH_SHORT).show()
                var documentReference = fStore.collection("incomes/$userID/Income_detail").document("income$newIncome")
                var income = HashMap<String,String>()
                income["Income_Name"] = "$newIncome"
                income["Income_Amount"] = "100.000f"
                documentReference.set(income).addOnSuccessListener {
                    val resetView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)
                    val resetViewBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog).setView(resetView)
                    //show dialog
                    val displayDialog = resetViewBuilder.show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                updateIncomeCounter(newIncome)
            }catch (e : Exception){
                Toast.makeText(this," "+e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateIncomeCounter(lastIncome : Int){
        val documentReference = fStore.collection("incomes").document(userID)
        documentReference
            .update("income",lastIncome)
            .addOnSuccessListener { document ->
                Toast.makeText(this,"Counter Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this," "+it.message, Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
        // Get Last Income Index
        var documentReference = fStore.collection("incomes").document(userID)
        documentReference.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val data = document.data
                    if (data != null) {
                        lastIncome = data.get("income").toString().toInt()
                    }
                }
            }

    }

}