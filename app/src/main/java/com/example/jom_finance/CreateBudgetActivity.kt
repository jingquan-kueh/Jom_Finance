package com.example.jom_finance

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.jom_finance.models.Budget
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.activity_create_budget.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class CreateBudgetActivity : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var budgetID: String
    private var budgetNum by Delegates.notNull<Int>()
    private lateinit var budgetDate: String
    private var budgetAmount: Double = 0.0
    private lateinit var budgetCategory: String
    private var budgetAlert: Boolean = false
    private var budgetAlertPercentage: Int = 0
    private var budgetSpent: Double = 0.0

    private lateinit var thumbView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget)
        setupDataBase()

        //get month from previous activity/fragment
        //set month-year text
        val monthYear = intent?.extras?.getString("budget_date")
        budgetMonthYear_text.text = monthYear
        budgetDate = monthYear!!


        //seekbar progress
        thumbView = LayoutInflater.from(this)
            .inflate(R.layout.seekbar_thumb, null, false)
        seekBar.thumb = getThumb(0)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                seekBar.thumb = getThumb(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        budgetAlert_switch.setOnCheckedChangeListener { _, isChecked ->
            budgetAlert = isChecked
            seekBar.progress = 0
            seekBar.isEnabled = isChecked
        }

        budgetID = intent?.extras?.getString("budget_id").toString()
        if(budgetID != "null"){
            budgetAmount = intent?.extras?.getDouble("budget_amount")!!
            budgetCategory = intent?.extras?.getString("budget_category").toString()
            budgetAlert = intent?.extras?.getBoolean("budget_alert")!!
            budgetAlertPercentage = intent?.extras?.getInt("budget_alert_percentage")!!
            budgetSpent = intent?.extras?.getDouble("budget_spent")!!

            createBudgetConfirm_btn.text = "Update"

            budgetAmount_edit.text = Editable.Factory.getInstance().newEditable(budgetAmount.toString())

            budgetCategory_ddl.editText?.text = Editable.Factory.getInstance().newEditable(budgetCategory)

            budgetAlert_switch.isChecked = budgetAlert

            seekBar.progress = budgetAlertPercentage

            createBudgetConfirm_btn.setOnClickListener {
                updateBudget()
            }

        }else{
            createBudgetConfirm_btn.setOnClickListener {
                addBudget()
                finish()
            }
        }

        //category drop down list
        val cat : MutableList<String> = mutableListOf()
        fStore.collection("category/$userID/category_detail")
            .get()
            .addOnSuccessListener {
                for (document in it.documents){
                    cat.add(document.getString("category_name")!!)
                }
            }
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, cat)
        budgetCategories_autoCompleteTextView.setAdapter(adapter)

    }

    private fun addBudget(){

        var lastBudget by Delegates.notNull<Int>()
        budgetCategory = budgetCategory_ddl.editText?.text.toString()
        budgetAmount = budgetAmount_edit.text.toString().toDouble()
        budgetAlertPercentage = seekBar.progress

        budgetSpent = 0.0
        fStore.collection("transaction/$userID/Transaction_detail")
            .whereEqualTo("Transaction_category", budgetCategory)
            .get()
            .addOnSuccessListener {
                for (document in it.documents){
                    val timestamp = document.getTimestamp("Transaction_timestamp")
                    val sdf = SimpleDateFormat("MMMM yyyy")
                    val date = timestamp?.toDate()
                    if (sdf.format(date) == budgetDate){
                        val transAmount = document.getDouble("Transaction_amount")!!
                        budgetSpent += transAmount
                    }
                }
            }

        try{
            fStore.collection("budget").document(userID)
                .get()
                .addOnCompleteListener { doc ->
                    lastBudget = doc.result["Budget_counter"].toString().toInt()

                    fStore.collection("budget/$userID/budget_detail")
                        .get()
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                val newBudget = lastBudget.inc()
                                val newBudgetRef = fStore.collection("budget/$userID/budget_detail").document("budget$newBudget")
                                val budgetDetail = Budget("budget$newBudget",budgetAmount, budgetDate, budgetCategory, budgetAlert, budgetAlertPercentage, budgetSpent)

                                //add budget to database
                                newBudgetRef.set(budgetDetail)
                                    .addOnCompleteListener {
                                        //update budget counter
                                        fStore.collection("budget").document(userID)
                                            .update("Budget_counter", newBudget)

                                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                }

        }catch (e: Exception) {
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun updateBudget(){
        val newCategory = budgetCategory_ddl.editText?.text.toString()

        if(budgetCategory == newCategory){
            budgetCategory = budgetCategory_ddl.editText?.text.toString()
            budgetAmount = budgetAmount_edit.text.toString().toDouble()
            budgetAlertPercentage = seekBar.progress

            budgetSpent = 0.0
            fStore.collection("transaction/$userID/Transaction_detail")
                .whereEqualTo("Transaction_category", budgetCategory)
                .get()
                .addOnSuccessListener {
                    for (document in it.documents){
                        val timestamp = document.getTimestamp("Transaction_timestamp")
                        val sdf = SimpleDateFormat("MMMM yyyy")
                        val date = timestamp?.toDate()
                        if (sdf.format(date) == budgetDate){
                            val transAmount = document.getDouble("Transaction_amount")!!
                            budgetSpent += transAmount
                        }
                    }
                }

            try{
                fStore.collection("budget").document(userID)
                    .get()
                    .addOnCompleteListener { doc ->

                        fStore.collection("budget/$userID/budget_detail")
                            .get()
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    val documentReference = fStore.collection("budget/$userID/budget_detail").document(budgetID)
                                    val budgetDetail = Budget(budgetID, budgetAmount, budgetDate, budgetCategory, budgetAlert, budgetAlertPercentage, budgetSpent)

                                    documentReference.set(budgetDetail).addOnCompleteListener {
                                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                    }

                                }
                            }
                    }

            }catch (e: Exception) {
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
        }
        else{
            budgetSpent = 0.0
            fStore.collection("transaction/$userID/Transaction_detail")
                .whereEqualTo("Transaction_category", newCategory)
                .get()
                .addOnSuccessListener {
                    for (document in it.documents){
                        val timestamp = document.getTimestamp("Transaction_timestamp")
                        val sdf = SimpleDateFormat("MMMM yyyy")
                        val date = timestamp?.toDate()
                        if (sdf.format(date) == budgetDate){
                            val transAmount = document.getDouble("Transaction_amount")!!
                            budgetSpent += transAmount
                        }
                    }
                }

            budgetCategory = budgetCategory_ddl.editText?.text.toString()
            budgetAmount = budgetAmount_edit.text.toString().toDouble()
            budgetAlertPercentage = seekBar.progress

            try{
                fStore.collection("budget/$userID/budget_detail")
                    .get()
                    .addOnCompleteListener {
                        if(it.isSuccessful){

                            val documentReference = fStore.collection("budget/$userID/budget_detail").document(budgetID)
                            val budgetDetail = Budget(budgetID, budgetAmount, budgetDate, budgetCategory, budgetAlert, budgetAlertPercentage, budgetSpent)

                            documentReference.set(budgetDetail).addOnCompleteListener {
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()

                            }
                        }
                    }
            }catch (e: Exception) {
                Log.w(ContentValues.TAG, "Error adding document", e)
            }

        }
    }

    private fun getThumb(progress: Int): Drawable? {
        (thumbView.findViewById(R.id.tvProgress) as TextView).text = "$progress%"

        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(thumbView.measuredWidth, thumbView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        thumbView.layout(0, 0, thumbView.measuredWidth, thumbView.measuredHeight)
        thumbView.draw(canvas)
        return BitmapDrawable(resources, bitmap)
    }

    private fun hideKeyboard(view: View){
        // Hide the keyboard.
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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