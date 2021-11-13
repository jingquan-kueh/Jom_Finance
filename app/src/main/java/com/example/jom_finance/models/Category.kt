package com.example.jom_finance.models

import com.google.firebase.firestore.PropertyName

data class Category(
    @get:PropertyName("category_name") @set:PropertyName("category_name")var categoryName : String?=null,
    @get:PropertyName("category_icon") @set:PropertyName("category_icon") var categoryIcon: Int?= null,
    @get:PropertyName("category_color") @set:PropertyName("category_color")var categoryColor : Int?= null
)