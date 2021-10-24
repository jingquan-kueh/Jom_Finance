package com.example.jom_finance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.jom_finance.income.AddNewIncome
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_fragment.*


class HomeActivity : AppCompatActivity() {

    private var statusAddOn : Boolean = false
    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_clockwise)}
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_anticlockwise)}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim)}
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentHomeView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = bottomNav
        bottomNav.setupWithNavController(navController)

        fab_add.setOnClickListener{
            onAddButtonClicked()
            /*if(!statusAddOn){
                gradientView.foreground = R.drawable.change_gradient_drawable.toDrawable()
            }else{
                gradientView.foreground = null
            }*/

            statusAddOn = !statusAddOn
        }
        fab_income.setOnClickListener{
            val intent = Intent(this, AddNewIncome::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Toast.makeText(this, "income Clicked", Toast.LENGTH_SHORT).show()
        }
        fab_expenses.setOnClickListener{
            Toast.makeText(this, "expenses clicked", Toast.LENGTH_SHORT).show()
        }
        fab_voice.setOnClickListener{
            Toast.makeText(this, "voice clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, VoiceActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        fab_snap.setOnClickListener{
            Toast.makeText(this, "snap clicked", Toast.LENGTH_SHORT).show()
        }
    }
    private fun onAddButtonClicked() {
        setVisibility(statusAddOn)
        setAnimation(statusAddOn)
        setClickable(statusAddOn)
    }

    private fun setAnimation(statusAddOn : Boolean) {
        if(!statusAddOn){
            //home_FL.foreground = R.drawable.change_gradient_drawable.toDrawable()
            fab_add.startAnimation(rotateOpen)
            fab_income.visibility = View.VISIBLE
            fab_expenses.visibility = View.VISIBLE
            fab_voice.visibility = View.VISIBLE
            fab_snap.visibility = View.VISIBLE
        }else{
            //home_FL.foreground = null
            fab_add.startAnimation(rotateClose)
            fab_income.visibility = View.INVISIBLE
            fab_expenses.visibility = View.INVISIBLE
            fab_voice.visibility = View.INVISIBLE
            fab_snap.visibility = View.INVISIBLE
        }
    }

    private fun setVisibility(statusAddOn : Boolean) {
        if(!statusAddOn){
            fab_income.startAnimation(fromBottom)
            fab_expenses.startAnimation(fromBottom)
            fab_voice.startAnimation(fromBottom)
            fab_snap.startAnimation(fromBottom)
        }else{
            fab_income.startAnimation(toBottom)
            fab_expenses.startAnimation(toBottom)
            fab_voice.startAnimation(toBottom)
            fab_snap.startAnimation(toBottom)
        }
    }
    private fun setClickable(statusAddOn: Boolean){
        fab_income.isClickable = !statusAddOn
    }

}