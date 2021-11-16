package com.example.jom_finance

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.BLACK
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
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.activity_create_budget.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class CreateBudgetActivity : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

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

        val date = monthYear?.split(" ")?.toTypedArray()
        budgetDate = "${date?.get(0)}-${date?.get(1)}"

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

        createBudgetConfirm_btn.setOnClickListener {
            addBudgetToDatabase()
        }



    }

    private fun addBudgetToDatabase(){

        budgetCategory = budgetCategory_ddl.editText?.text.toString()
        budgetAmount = budgetAmount_edit.text.toString().toDouble()
        budgetAlertPercentage = seekBar.progress

        try{
            fStore.collection("budget/$userID/$budgetDate")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val documentReference = fStore.collection("budget/$userID/$budgetDate").document(budgetCategory)
                        val budgetDetail = Budget(budgetAmount, budgetDate, budgetCategory, BLACK, budgetAlert, budgetAlertPercentage, budgetSpent)

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