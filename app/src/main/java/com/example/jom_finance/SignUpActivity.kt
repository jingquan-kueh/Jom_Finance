package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity(){
    lateinit var name : TextInputEditText
    lateinit var email : TextInputEditText
    lateinit var password : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setup()
        /*https://android--code.blogspot.com/2020/02/android-kotlin-ktx-clickablespan-example.html for the T&C spanable*/
        btn_sign_up.setOnClickListener {
            if(validate()){
                Toast.makeText(this,"Sign In !!!!", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun openLogin(view: View?) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    fun backFun(view: View?) {
        this.finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    fun setup(){
        name = findViewById<TextInputEditText>(R.id.SignUpNameField)
        email = findViewById<TextInputEditText>(R.id.SignUpNameField)
        password = findViewById<TextInputEditText>(R.id.SignUpNameField)

    }
    private fun validate() : Boolean{
        var result : Boolean = false
        if(name.text.isNullOrEmpty() && email.text.isNullOrEmpty() && password.text.isNullOrEmpty()){
            Toast.makeText(this,"Please fill in all detail", Toast.LENGTH_SHORT).show()
        }else{
            result = true
        }
        return result
    }

}

