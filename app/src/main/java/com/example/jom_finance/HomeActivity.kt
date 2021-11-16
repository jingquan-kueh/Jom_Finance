package com.example.jom_finance

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.jom_finance.income.AddNewIncome
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import java.io.File

private const val FILE_NAME = "photo.jpg" //temporary file name
private lateinit var photoFile: File

class HomeActivity : AppCompatActivity() {

    private var statusAddOn : Boolean = false
    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_clockwise)}
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_anticlockwise)}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim)}
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim)}

    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String
    private lateinit var currentUser : FirebaseUser



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupDataBase()
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentHomeView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = bottomNav
        bottomNav.setupWithNavController(navController)

        fab_add.setOnClickListener{
            onAddButtonClicked()
            statusAddOn = !statusAddOn
        }
        fab_income.setOnClickListener{
            val intent = Intent(this, AddNewIncome::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            Toast.makeText(this, "income Clicked", Toast.LENGTH_SHORT).show()
        }
        fab_expenses.setOnClickListener{
            val intent = Intent(this, AddNewExpenseActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
            //open camera
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider = FileProvider.getUriForFile(this,"com.example.jom_finance.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if(takePictureIntent.resolveActivity(this.packageManager) != null)
                startActivityForResult(takePictureIntent, 101)
            else
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()

        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, "jpg", storageDirectory)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 101 && resultCode == Activity.RESULT_OK){
            val intent = Intent(this, AddNewExpenseActivity::class.java)
            val fileProvider = FileProvider.getUriForFile(this,"com.example.jom_finance.fileprovider", photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            intent.putExtra("image_path", photoFile.absolutePath)
            startActivity(intent)
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
        fab_expenses.isClickable = !statusAddOn
        fab_voice.isClickable = !statusAddOn
        fab_snap.isClickable = !statusAddOn
    }

    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        currentUser = fAuth.currentUser!!
        userID = currentUser.uid

    }
}