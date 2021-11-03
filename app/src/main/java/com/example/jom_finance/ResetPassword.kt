package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlin.math.log

class ResetPassword : AppCompatActivity() {
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        setupDataBase()
        val code : String = intent.getStringExtra("oobCode").toString()

        backBtn.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finishAffinity()
        }
        btn_continue.setOnClickListener {
            if (code != null) {
                val pass1 = NewResetPasswordField.text.toString()
                var pass2 = RetypePasswordField.text.toString()
                if (pass1 == pass2) {
                    fAuth.verifyPasswordResetCode(code)
                    fAuth.confirmPasswordReset(code, pass1)
                    Toast.makeText(this,
                        "Password Modified",
                        Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finishAffinity()

                } else {
                    RetypePasswordField.requestFocus()
                    Toast.makeText(this,
                        "Password are not same.. Please Make sure both are same.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
    }


}