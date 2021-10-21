package com.example.jom_finance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.activity_create_budget.*

class AddNewExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_expense)

        //drop down list
        val exp = listOf("Shopping", "Groceries", "Transport", "Restaurant")
        val expAdapter = ArrayAdapter(this, R.layout.item_dropdown, exp)
        expenseCategory_autoCompleteTextView.setAdapter(expAdapter)

        val acc = listOf("Bank", "Cash", "Grab Pay", "Touch n Go")
        val accAdapter = ArrayAdapter(this, R.layout.item_dropdown, acc)
        expenseAccount_autoCompleteTextView.setAdapter(accAdapter)



    }
}