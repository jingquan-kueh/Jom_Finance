package com.example.jom_finance

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.MotionEvent
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.jom_finance.intro.IntroActivity1
import com.google.firebase.auth.ActionCodeResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception


class MainActivity :AppCompatActivity(){
    private var x1 = 0f
    private var x2 = 0f
    val MIN_DISTANCE = 150

    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)
        setupDataBase()

        /*// Initialize sharedPreferences File
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        // Initialize sharedPreferences to edit
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("isLogin",true)

        if(fAuth.currentUser != null){
            userID = fAuth.currentUser!!.uid
        }

        if(userID != null){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }*/

        handleDynamicLink()
    }

    override fun onStart() {
        super.onStart()
        handleDynamicLink()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> x1 = event.x
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                val deltaX: Float = x2 - x1
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if(x2 > x1){ //left to right (going to left)
                        //..
                    }
                    else if(x1>x2){ //right to left (going to right)
                        val intent = Intent(this, IntroActivity1::class.java)
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

    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
    }

    private fun handleDynamicLink() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener{ pendingDynamicLinkData ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val oobCode = deepLink?.getQueryParameter("oobCode")
                    if (oobCode != null) {
                        fAuth.checkActionCode(oobCode).addOnSuccessListener { result ->
                            when (result.operation) {
                                ActionCodeResult.VERIFY_EMAIL -> {
                                    fAuth.applyActionCode(oobCode)
                                        .addOnSuccessListener {
                                            finish()
                                        }.addOnFailureListener {
                                            Toast.makeText(this,result.toString(), Toast.LENGTH_LONG).show()
                                        }
                                }
                                ActionCodeResult.PASSWORD_RESET -> {
                                    val passWordResetInetemnt =
                                        Intent(this@MainActivity, ResetPassword::class.java)
                                    passWordResetInetemnt.putExtra("oobCode", oobCode)
                                    startActivity(passWordResetInetemnt)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }.addOnFailureListener { result: Exception? ->
                Toast.makeText(this,result.toString(), Toast.LENGTH_LONG).show()
            }
    }


}