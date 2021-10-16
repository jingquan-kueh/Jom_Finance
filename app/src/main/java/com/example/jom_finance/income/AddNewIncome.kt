package com.example.jom_finance.income

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentTransaction
import com.example.jom_finance.PopupActivity
import com.example.jom_finance.R
import com.example.jom_finance.SignUpActivity
import kotlinx.android.synthetic.main.fragment_income.*

class AddNewIncome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_income)

    }
}