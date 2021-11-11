package com.example.jom_finance

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.jom_finance.models.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_budget.*
import java.lang.Exception

class CreateBudgetActivity : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var  budgetID: String
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

        //drop down list
        val items = listOf("Shopping", "Groceries", "Transport", "Restaurant")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, items)
        budgetCategories_autoCompleteTextView.setAdapter(adapter)

        //drop down list onClickListener
        //editTextNumberDecimal.setOnClickListener{
        //hideKeyboard(it)
        //}


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


        createBudgetConfirm_btn.setOnClickListener {
            addBudgetToDatabase()
        }




    }

    private fun addBudgetToDatabase(){

        budgetID = "testBudget2"
        budgetAmount = budgetAmount_edit.text.toString().toDouble()
        budgetCategory = budgetCategory_ddl.editText?.text.toString()
        budgetAlertPercentage = seekBar.progress

        try{
            fStore.collection("budget/$userID/budget_detail")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){

                        val documentReference = fStore.collection("budget/$userID/budget_detail").document(budgetID)

                        val budgetDetail = Budget(budgetAmount, budgetCategory, budgetAlert, budgetAlertPercentage, budgetSpent)

                        documentReference.set(budgetDetail).addOnCompleteListener {
                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }catch (e: Exception) {
            Log.w(ContentValues.TAG, "Error adding document", e)
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