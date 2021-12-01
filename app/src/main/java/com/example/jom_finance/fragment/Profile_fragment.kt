package com.example.jom_finance.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.preference.PreferenceManager
import com.example.jom_finance.AccountsListActivity
import com.example.jom_finance.CategoryListActivity
import com.example.jom_finance.LoginActivity
import com.example.jom_finance.R
import com.example.jom_finance.setting.SettingActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_new_income.*
import kotlinx.android.synthetic.main.bottomsheet_logout.*
import kotlinx.android.synthetic.main.fragment_profile_fragment.*
import kotlinx.android.synthetic.main.fragment_profile_fragment.view.*
import java.io.File

class Profile_fragment : Fragment() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_profile_fragment, container, false)

        val documentReference = fStore.collection("users").document(userID)
        documentReference.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    view.usernamePlaceHolder.text = document.getString("username")
                }
            }

        val storageReference = FirebaseStorage.getInstance()
            .getReference("user/$userID")

        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                view.profileCircleImageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener {
                val storageReference = FirebaseStorage.getInstance()
                    .getReference("user/sample-user.png")
                storageReference.getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    view.profileCircleImageView.setImageBitmap(bitmap)
                }
            }

        view.profileCircleImageView.setOnClickListener {
            val openGalleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(openGalleryIntent, 100)
        }

        view.accountBtn.setOnClickListener{
            requireActivity().run {
                val intent = Intent(this, AccountsListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        view.categoryBtn.setOnClickListener {
            requireActivity().run {
                val intent = Intent(this, CategoryListActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }


        view.settingBtn.setOnClickListener {
            requireActivity().run {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        view.logoutBtn.setOnClickListener {
            openAttachmentBottomSheetDialog(view)
        }
        // Inflate the layout for this fragment
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data!!
            profileCircleImageView.setImageURI(imageUri)
            val progressDialog = ProgressDialog(this.context)
            progressDialog.setMessage("Fetching image...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            val storageReference = FirebaseStorage.getInstance()
                .getReference("user/$userID")
            var uploadTask: UploadTask = storageReference.putFile(imageUri)

            uploadTask
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this.context,
                        "Successfully uploaded",
                        Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this.context, "Failed", Toast.LENGTH_SHORT)
                        .show()
                }

        }
    }

    private fun setupDataBase() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        currentUser = fAuth.currentUser!!
        userID = currentUser.uid

    }

    private fun openAttachmentBottomSheetDialog(view: View) {

        val bottomSheet = BottomSheetDialog(view.context)
        bottomSheet.setContentView(R.layout.bottomsheet_logout)

        val yesBtn = bottomSheet.findViewById<Button>(R.id.logoutYesbtn) as Button
        val noBtn = bottomSheet.findViewById<Button>(R.id.logoutNobtn) as Button
        bottomSheet.show()
        noBtn.setOnClickListener {
            bottomSheet.dismiss()
        }
        yesBtn.setOnClickListener {
            bottomSheet.dismiss()
            fAuth.signOut()
            requireActivity().run {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(applicationContext)
                // Initialize sharedPreferences to edit
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putBoolean("isLogin", false).apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finishAffinity(this)
            }
        }



    }

}