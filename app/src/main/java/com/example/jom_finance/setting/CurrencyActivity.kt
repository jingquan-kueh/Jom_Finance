package com.example.jom_finance.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jom_finance.R
import kotlinx.android.synthetic.main.activity_currency.*

class CurrencyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        backBtn.setOnClickListener{
            finish()
        }

        CurrencyRadioGroup.setOnCheckedChangeListener{ _, checkedID->
            when(checkedID){
                R.id.Radio_Btn_USD ->{
                    Radio_Btn_MYR.setButtonDrawable(R.drawable.empty_drawable)
                    Radio_Btn_USD.setButtonDrawable(R.drawable.selected_icon)
                }
                else ->{
                    Radio_Btn_USD.setButtonDrawable(R.drawable.empty_drawable)
                    Radio_Btn_MYR.setButtonDrawable(R.drawable.selected_icon)
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}