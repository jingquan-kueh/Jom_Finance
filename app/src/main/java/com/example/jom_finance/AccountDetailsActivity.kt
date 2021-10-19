package com.example.jom_finance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_account_details.*

class AccountDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        favourite.setOnClickListener{
            setFavourite()
        }

        //check if favourite

    }
    private fun setFavourite(){
        favourite.setImageResource(R.drawable.ic_baseline_star_24)
    }
}