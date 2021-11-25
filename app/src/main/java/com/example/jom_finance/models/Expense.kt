package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Expense(
    @get:PropertyName("Expense_name") @set:PropertyName("Expense_name") var expenseName: String?= null,
    @get:PropertyName("Expense_amount") @set:PropertyName("Expense_amount") var expenseAmount : Double?=null,
    @get:PropertyName("Expense_account") @set:PropertyName("Expense_account")var expenseAccount : String?= null,
    @get:PropertyName("Expense_attachment") @set:PropertyName("Expense_attachment")var expenseAttachment : Boolean?= null,
    @get:PropertyName("Expense_category") @set:PropertyName("Expense_category")var expenseCategory : String?= null,
    @get:PropertyName("Expense_description") @set:PropertyName("Expense_description")var expenseDescription : String?= null)
