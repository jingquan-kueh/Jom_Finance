package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_forgot_password_email_send.*

class ForgotPasswordEmailSend : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_email_send)
        val intent = intent
        var email = intent.getStringExtra("emailReset")
        if(email != null){
            ForgotPasswordEmailSendText.text = "Check your email $email and follow the instructions to reset your password"
        }else{
            ForgotPasswordEmailSendText.text = "Check your email and follow the instructions to reset your password"
        }
        btn_BackToLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}