package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        btn_continue.setOnClickListener{
            if(!(ForgotPasswordEmailField.text.isNullOrEmpty() && ForgotPasswordEmailField.text.isNullOrBlank())){
                var intent = Intent(this, ForgotPasswordEmailSend::class.java)
                intent.putExtra("emailReset",ForgotPasswordEmailField.text.toString())
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }else{
                Toast.makeText(this,"Hey.. Enter the email please.. Without the Email i can't reset the password", Toast.LENGTH_LONG).show()
            }

        }
    }

    fun backFun(view: View?) {
        this.finish()
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}