package com.example.jom_finance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    var statusAddOn : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ArrayAdapter.createFromResource(
            this,
            R.array.Month,
            R.layout.spinner_list
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_list)
            // Apply the adapter to the spinner
            spinnerMonth.adapter = adapter
        }
        fab_add.setOnClickListener{
            if(statusAddOn){
                val aniRotateClk = AnimationUtils.loadAnimation(this,R.anim.rotate_anticlockwise)
                fab_add.startAnimation(aniRotateClk)
                aniRotateClk.fillAfter = true;
                statusAddOn = false
            }else{
                val aniRotateClk = AnimationUtils.loadAnimation(this,R.anim.rotate_clockwise)
                fab_add.startAnimation(aniRotateClk)
                aniRotateClk.fillAfter = true;
                statusAddOn = true
            }
        }
    }

}