package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*


class LoginActivity :AppCompatActivity(){

    private lateinit var FirebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
        txtForgotPassword.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btn_login.setOnClickListener{
            login()
        }
    }

    private fun login() {
        if(validate()){
            FirebaseAuth.signInWithEmailAndPassword(LoginEmailField.text.toString().trim(),LoginPasswordField.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(this,"Welcome User !!!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                .addOnFailureListener{
                    Toast.makeText(this," "+it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validate() : Boolean{
        var result : Boolean = false
        if(LoginEmailField.text.isNullOrEmpty() && LoginPasswordField.text.isNullOrEmpty()){
            Toast.makeText(this,"Please fill in all detail", Toast.LENGTH_SHORT).show()
        }else{
            result = true
        }
        return result
    }

    fun openSignUp(view: View?) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun backFun(view: View?) {
        if(isTaskRoot){
            openSignUp(view)
        }else{
            this.finish()
        }

    }
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}