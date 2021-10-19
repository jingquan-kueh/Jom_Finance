package com.example.jom_finance.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.example.jom_finance.LoginActivity
import com.example.jom_finance.R
import com.example.jom_finance.SignUpActivity

class IntroActivity1 :AppCompatActivity(){
    private var x1 = 0f
    private var x2 = 0f
    val MIN_DISTANCE = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro1)
    }
    fun openSignUp(view: View?) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun openLogin(view: View?) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> x1 = event.x
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                val deltaX: Float = x2 - x1
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if(x2 > x1){ //left to right (going to left)
                        finish()
                    }
                    else if(x1>x2){ //right to left (going to right)
                        val intent = Intent(this, IntroActivity2::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                } else {
                    // consider as something else - a screen tap for example
                }
            }
        }
        return super.onTouchEvent(event)
    }


}