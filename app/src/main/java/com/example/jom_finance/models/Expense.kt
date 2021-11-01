package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Expense(
    @get:PropertyName("Expense_name") @set:PropertyName("Expense_name") var expenseName: String?= null,
    @get:PropertyName("Expense_amount") @set:PropertyName("Expense_amount")var expenseAmount : Double?=null)
