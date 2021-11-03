package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName
import com.google.j2objc.annotations.Property

data class Income(
    @get:PropertyName("Income_name") @set:PropertyName("Income_name") var incomeName: String?= null,
    @get:PropertyName("Income_amount") @set:PropertyName("Income_amount")var incomeAmount : Double?=null)
