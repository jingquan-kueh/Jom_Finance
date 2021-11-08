package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName
import com.google.j2objc.annotations.Property

data class Income(
    @get:PropertyName("Income_name") @set:PropertyName("Income_name") var incomeName: String?= null,
    @get:PropertyName("Income_amount") @set:PropertyName("Income_amount") var incomeAmount : Double?=null,
    @get:PropertyName("Income_account") @set:PropertyName("Income_account")var incomeAccount : String?= null,
    @get:PropertyName("Income_attachment") @set:PropertyName("Income_attachment")var incomeAttachment : Boolean?= null,
    @get:PropertyName("Income_category") @set:PropertyName("Income_category")var incomeCategory : String?= null,
    @get:PropertyName("Income_description") @set:PropertyName("Income_description")var incomeDescription : String?= null)
