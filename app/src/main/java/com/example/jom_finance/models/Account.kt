package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Account(
    @get:PropertyName("account_name") @set:PropertyName("account_name") var accountName: String?= null,
    @get:PropertyName("account_amount") @set:PropertyName("account_amount")var accountAmount : Double?=null,
    @get:PropertyName("account_icon") @set:PropertyName("account_icon")var accountIcon : Int?=null,
    @get:PropertyName("account_color") @set:PropertyName("account_color")var accountColor : Int?=null)
