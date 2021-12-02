package com.example.jom_finance

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.bottomsheet_attachment.*
import kotlinx.android.synthetic.main.activity_transfer.*
import java.io.File

private lateinit var fAuth: FirebaseAuth
private lateinit var fStore: FirebaseFirestore
private lateinit var userID: String

private var transferAmount : Double = 0.0
private lateinit var transferFrom : String
private lateinit var transferTo : String
private lateinit var transferDescription : String


class TransferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)
        setupDataBase()

        //accounts drop down list
        val acc : MutableList<String> = mutableListOf()
        fStore.collection("accounts/$userID/account_detail")
            .get()
            .addOnSuccessListener {
                for (document in it.documents){
                    acc.add(document.getString("account_name")!!)
                }
            }
        val accAdapter = ArrayAdapter(this, R.layout.item_dropdown, acc)
        transferFrom_autoCompleteTextView.setAdapter(accAdapter)
        transferTo_autoCompleteTextView.setAdapter(accAdapter)


        transferConfirm_btn.setOnClickListener {
            transfer()
        }

    }

    private fun setupDataBase() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }

    private fun transfer(){
        val accountRef = fStore.collection("accounts/$userID/account_detail")

        transferAmount = transferAmount_edit.text.toString().toDouble()
        transferFrom = transferFrom_ddl.editText?.text.toString()
        transferTo = transferTo_ddl.editText?.text.toString()
        //transferDescription = transferDescription_outlinedTextField.editText?.text.toString()

        if(transferFrom != transferTo){
            accountRef.document(transferFrom)
                .get()
                .addOnSuccessListener { documentFrom ->

                    var accountFromAmount = documentFrom.data?.getValue("account_amount").toString().toDouble()

                    if (accountFromAmount >= transferAmount){
                        accountRef.document(transferTo)
                            .get()
                            .addOnSuccessListener { documentTo ->
                                var accountToAmount = documentTo.data?.getValue("account_amount").toString().toDouble()

                                accountRef.document(transferFrom)
                                    .update("account_amount",accountFromAmount - transferAmount)

                                accountRef.document(transferTo)
                                    .update("account_amount", accountToAmount + transferAmount)

                                Toast.makeText(this, "Transferred successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, AccountsListActivity::class.java)
                                startActivity(intent)
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                            }

                    }else{
                        Toast.makeText(this, "$transferFrom account has insufficient amount", Toast.LENGTH_SHORT).show()
                    }

                }
                .addOnFailureListener {
                    Log.w(ContentValues.TAG, "Error transfer", it)
                }

        }else{
            Toast.makeText(this, "Cannot transfer to the same account", Toast.LENGTH_SHORT).show()
        }


    }

}