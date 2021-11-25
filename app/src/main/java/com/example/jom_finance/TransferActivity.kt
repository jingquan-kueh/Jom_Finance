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
private  var transactionAttachment: Boolean = false

private const val IMAGE_REQUEST_CODE = 100
private const val CAMERA_REQUEST_CODE = 42
private const val DOCUMENT_REQUEST_CODE = 111

private lateinit var attachmentType: String
private const val FILE_NAME = "photo.jpg" //temporary file name
private lateinit var photoFile: File
private lateinit var imageUri : Uri
private lateinit var imageBitmap: Bitmap
private lateinit var documentUri : Uri

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

        //hide attachment image and doc
        transferAttachment_img.visibility = View.GONE
        transferAttachmentDocument_txt.visibility = View.GONE



        transferAddAttachment_btn.setOnClickListener {
            if(transferAttachment_img.visibility == View.GONE && transferAttachmentDocument_txt.visibility == View.GONE)
                openAttachmentBottomSheetDialog()
            else{
                transferAttachment_img.visibility = View.GONE
                transferAttachmentDocument_txt.visibility = View.GONE
                transferAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24, 0, 0, 0)
                transferAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
                transferAddAttachment_btn.text = "Add Attachment"
                transactionAttachment = false

            }
        }

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

    private fun openAttachmentBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_attachment)

        val camera = bottomSheet.findViewById<Button>(R.id.attachementCamera_btn) as Button
        val image = bottomSheet.findViewById<Button>(R.id.attachementImage_btn) as Button
        val document = bottomSheet.findViewById<Button>(R.id.attachementDocument_btn) as Button


        camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider = FileProvider.getUriForFile(this,"com.example.jom_finance.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)


            if(takePictureIntent.resolveActivity(this.packageManager) != null)
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            else
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            bottomSheet.dismiss()
        }

        image.setOnClickListener {
            //open image picker
            val pickImageIntent = Intent(Intent.ACTION_PICK)
            pickImageIntent.type = "image/*"
            startActivityForResult(pickImageIntent, IMAGE_REQUEST_CODE)
            bottomSheet.dismiss()
        }

        document.setOnClickListener {
            //open document picker
            val pickDocumentIntent = Intent(Intent.ACTION_GET_CONTENT)
            pickDocumentIntent.type = "application/pdf"
            startActivityForResult(Intent.createChooser(pickDocumentIntent, "Select a document"), DOCUMENT_REQUEST_CODE)
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun getPhotoFile(fileName: String): File{
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, "jpg", storageDirectory)
    }

}