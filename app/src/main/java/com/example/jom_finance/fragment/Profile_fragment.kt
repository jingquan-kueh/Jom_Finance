package com.example.jom_finance.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
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

        view.logoutBtn.setOnClickListener{
            fAuth.signOut()
            requireActivity().run {
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

    private fun setupDataBase(){
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        currentUser = fAuth.currentUser!!
        userID = currentUser.uid

    }
}