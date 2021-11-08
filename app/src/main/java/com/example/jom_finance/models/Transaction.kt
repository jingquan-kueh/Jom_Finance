package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Transaction(
    @get:PropertyName("Transaction_name") @set:PropertyName("Transaction_name") var transactionName: String?= null,
    @get:PropertyName("Transaction_amount") @set:PropertyName("Transaction_amount")var transactionAmount : Double?=null,
    @get:PropertyName("Transaction_account") @set:PropertyName("Transaction_account")var transactionAccount : String?= null,
    @get:PropertyName("Transaction_attachment") @set:PropertyName("Transaction_attachment")var transactionAttachment : Boolean?= null,
    @get:PropertyName("Transaction_category") @set:PropertyName("Transaction_category")var transactionCategory : String?= null,
    @get:PropertyName("Transaction_description") @set:PropertyName("Transaction_description")var transactionDescription : String?= null,
    @get:PropertyName("Transaction_type") @set:PropertyName("Transaction_type")var transactionType : String?= null)

