package com.example.jom_finance.fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.preference.PreferenceManager
import com.example.jom_finance.LoginActivity
import com.example.jom_finance.R
import com.example.jom_finance.setting.SettingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile_fragment.*
import kotlinx.android.synthetic.main.fragment_profile_fragment.view.*

class Profile_fragment : Fragment() {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String
    private lateinit var currentUser : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBase()
        val documentReference = fStore.collection("users").document(userID)
        documentReference.get()
            .addOnSuccessListener { document ->
                if(document!=null){
                    usernamePlaceHolder.text = document.getString("username")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view :View = inflater.inflate(R.layout.fragment_profile_fragment, container, false)

        view.profileCircleImageView.setOnClickListener{
            val openGalleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(openGalleryIntent,100)
        }

        view.logoutBtn.setOnClickListener{
            fAuth.signOut()
            requireActivity().run {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                // Initialize sharedPreferences to edit
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putBoolean("isLogin",false).apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finishAffinity(this)
            }
        }
        view.settingBtn.setOnClickListener{
            requireActivity().run {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        // Inflate the layout for this fragment
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data!!
            profileCircleImageView.setImageURI(imageUri)
        }
    }

    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        currentUser = fAuth.currentUser!!
        userID = currentUser.uid

    }
}