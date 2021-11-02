package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPassword : AppCompatActivity() {
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String
    private lateinit var currentUser : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setupDataBase()

        btn_continue.setOnClickListener{
            if(!(ForgotPasswordEmailField.text.isNullOrEmpty() && ForgotPasswordEmailField.text.isNullOrBlank())){
                val email = ForgotPasswordEmailField.text.toString().trim()
                fAuth.sendPasswordResetEmail(email,actionCodeSettings()).addOnSuccessListener {
                    var intent = Intent(this, ForgotPasswordEmailSend::class.java)
                    intent.putExtra("emailReset",ForgotPasswordEmailField.text.toString())
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                }.addOnFailureListener{
                    Toast.makeText(this,it.message, Toast.LENGTH_LONG).show()
                }

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

    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        currentUser = fAuth.currentUser!!
        userID = currentUser.uid

    }

    private fun actionCodeSettings(): ActionCodeSettings? {
        return ActionCodeSettings.newBuilder()
            .setUrl("https://jomfinance.page.link/resetPass")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                "com.example.jom_finance",
                true,  /* installIfNotAvailable */
                "1.0" /* minimumVersion */
            )
            .build()
    }

}