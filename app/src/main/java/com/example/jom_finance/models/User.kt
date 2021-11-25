package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("email") @set:PropertyName("email")var email : String?="",
    @get:PropertyName("username") @set:PropertyName("username") var username: String?= "")
