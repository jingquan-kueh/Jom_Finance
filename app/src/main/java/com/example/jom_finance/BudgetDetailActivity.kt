package com.example.jom_finance

import android.graphics.Color.BLACK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_budget_detail.*

class BudgetDetailActivity : AppCompatActivity() {

    private var budgetAmount : Double = 0.0
    private lateinit var budgetDate : String
    private lateinit var budgetCategory : String
    private var budgetColor : Int = BLACK
    private var budgetAlert : Boolean = false
    private var budgetAlertPercentage : Int = 0
    private var budgetSpent : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_detail)

        budgetCategory = intent?.extras?.getString("budget_category").toString()
        if (budgetCategory != "null"){
            budgetAmount = intent?.extras?.getDouble("budget_amount")!!
            budgetDate = intent?.extras?.getString("budget_date").toString()
            budgetColor = intent?.extras?.getInt("budget_color")!!
            budgetAlert = intent?.extras?.getBoolean("budget_alert")!!
            budgetAlertPercentage = intent?.extras?.getInt("budget_alert_percentage")!!
            budgetSpent = intent?.extras?.getDouble("budget_spent")!!

            var budgetRemaining = budgetAmount - budgetSpent
            if (budgetRemaining < 0.0)
                budgetRemaining = 0.0

            budgetCategory_text.text = budgetCategory
            remainingAmount_text.text = "RM " + String.format("%.2f", budgetRemaining)
            budgetSpentDetail_text.text = "You've spent RM" + String.format("%.2f", budgetSpent) + " of RM" + String.format("%.2f", budgetAmount)

            val percentage = (budgetSpent / budgetAmount) * 100
            budgetRemaining_bar.progress = percentage.toInt()

            if(budgetSpent < budgetAmount)
                exceedLimit_text.visibility = View.GONE



        }




    }
}