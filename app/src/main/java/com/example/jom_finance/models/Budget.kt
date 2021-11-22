package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Budget(
    @get:PropertyName("budget_id") @set:PropertyName("budget_id")var budgetID : String?=null,
    @get:PropertyName("budget_amount") @set:PropertyName("budget_amount")var budgetAmount : Double?=null,
    @get:PropertyName("budget_date") @set:PropertyName("budget_date")var budgetDate : String?=null,
    @get:PropertyName("budget_category") @set:PropertyName("budget_category") var budgetCategory: String?= null,
    @get:PropertyName("budget_color") @set:PropertyName("budget_color") var budgetColor: Int?= null,
    @get:PropertyName("budget_alert") @set:PropertyName("budget_alert")var budgetAlert : Boolean?= null,
    @get:PropertyName("budget_alert_percentage") @set:PropertyName("budget_alert_percentage")var budgetAlertPercentage : Int?=null,
    @get:PropertyName("budget_spent") @set:PropertyName("budget_spent")var budgetSpent : Double?= null,
)