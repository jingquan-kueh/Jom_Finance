package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Transaction(
    @get:PropertyName("Transaction_name") @set:PropertyName("Transaction_name") var transactionName: String?= null,
    @get:PropertyName("Transaction_amount") @set:PropertyName("Transaction_amount")var transactionAmount : Double?=null,
    @get:PropertyName("Transaction_type") @set:PropertyName("Transaction_type")var transactionType : String?= null)
