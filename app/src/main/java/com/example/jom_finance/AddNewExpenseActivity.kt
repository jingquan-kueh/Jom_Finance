package com.example.jom_finance

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.core.content.FileProvider
import com.example.jom_finance.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.bottomsheet_attachment.*
import kotlinx.android.synthetic.main.bottomsheet_repeat.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import kotlin.properties.Delegates

private lateinit var fAuth: FirebaseAuth
private lateinit var fStore: FirebaseFirestore
private lateinit var userID: String

private var transactionNum by Delegates.notNull<Int>()

private const val TRANSACTION_TYPE = "expense"
private var transactionAmount: Double = 0.0
private lateinit var transactionCategory: String
private lateinit var transactionDescription: String
private lateinit var transactionAccount: String
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

class AddNewExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_expense)
        setupDataBase()

        //drop down list
        val exp = listOf("Shopping", "Groceries", "Transport", "Restaurant")
        val expAdapter = ArrayAdapter(this, R.layout.item_dropdown, exp)
        expenseCategory_autoCompleteTextView.setAdapter(expAdapter)

        val acc = listOf("Bank", "Cash", "Grab Pay", "Touch n Go")
        val accAdapter = ArrayAdapter(this, R.layout.item_dropdown, acc)
        expenseAccount_autoCompleteTextView.setAdapter(accAdapter)

        //hide repeat section
        repeat_constraintLayout.visibility = View.GONE

        //hide attachment image
        attachment_img.visibility = View.GONE
        attachmentDocument_txt.visibility = View.GONE

        expenseAddAttachment_btn.setOnClickListener {
            if(attachment_img.visibility == View.GONE && attachmentDocument_txt.visibility == View.GONE){
                openAttachmentBottomSheetDialog()
            }
            else{
                attachmentDocument_txt.visibility = View.GONE
                attachment_img.visibility = View.GONE
                expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24, 0, 0, 0)
                expenseAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
                expenseAddAttachment_btn.text = "Add Attachment"
                transactionAttachment = false
            }
        }

        expenseRepeatEdit_btn.setOnClickListener {
            openRepeatBottomSheetDialog()
        }

        expenseRepeat_switch.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked)
                repeat_constraintLayout.visibility = View.VISIBLE
            else
                repeat_constraintLayout.visibility = View.GONE
        }

        expenseConfirm_btn.setOnClickListener {

            // TODO: 5/11/2021 make sure all inputs are not NULL

            addIncomeToDatabase()

        }

        attachmentDocument_txt.setOnClickListener {
            // TODO: 6/11/2021 display pdf when clicked 
        }


    }

    private fun addIncomeToDatabase(){

        transactionAmount = expenseAmount_edit.text.toString().toDouble()
        transactionCategory = expenseCategory_ddl.editText?.text.toString()
        transactionDescription = expenseDescription_outlinedTextField.editText?.text.toString()
        transactionAccount = expenseAccount_ddl.editText?.text.toString()

        try{
            fStore.collection("transaction/$userID/Transaction_detail")
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        //set pathway

                        transactionNum = it.result.size().inc()

                        val documentReference = fStore.collection("transaction/$userID/Transaction_detail").document("transaction$transactionNum")

                        //transaction details
                        val transactionDetails = Transaction("transaction$transactionNum", TRANSACTION_TYPE, transactionAmount, transactionCategory, transactionDescription, transactionAccount, transactionAttachment)

                        //Insert to database
                        documentReference.set(transactionDetails).addOnCompleteListener {
                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        }
                    }

                    //store attachment if necessary
                    if(transactionAttachment){

                        val storageReference = FirebaseStorage.getInstance().getReference("transaction_images/$userID/transaction$transactionNum")
                        lateinit var uploadTask: UploadTask

                        when (attachmentType){
                            "camera" -> {
                                val baos = ByteArrayOutputStream()
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val data = baos.toByteArray()
                                uploadTask = storageReference.putBytes(data)
                            }
                            "image" -> uploadTask = storageReference.putFile(imageUri)
                            "document" -> uploadTask = storageReference.putFile(documentUri)
                        }

                        uploadTask
                            .addOnSuccessListener {
                                Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }catch (e: Exception) {
            Log.w(ContentValues.TAG, "Error adding document", e)
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

    override fun onActivityResult(requestCode:Int, resultCode: Int, data:Intent?){

        expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24, 0, 0, 0)
        expenseAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
        expenseAddAttachment_btn.text = "Remove Attachment"

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            attachment_img.visibility = View.VISIBLE
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageBitmap = takenImage

            // TODO: 5/11/2021 Image orientation

            attachment_img.setImageBitmap(takenImage)

            transactionAttachment = true
            attachmentType = "camera"

        }
        else if(requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            attachment_img.visibility = View.VISIBLE
            imageUri = data?.data!!
            attachment_img.setImageURI(imageUri)

            transactionAttachment = true
            attachmentType = "image"
        }
        else if(requestCode == DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            attachmentDocument_txt.visibility = View.VISIBLE
            documentUri = data?.data!!
            attachmentDocument_txt.text = getFileName(documentUri)

            transactionAttachment = true
            attachmentType = "document"

        }
        else{
            attachmentDocument_txt.visibility = View.GONE
            attachment_img.visibility = View.GONE
            expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24, 0, 0, 0)
            expenseAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
            expenseAddAttachment_btn.text = "Add Attachment"
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

    // TODO: 5/11/2021 COULD PUT THE FOLLOWING THREE FUNCTIONS IN A UTIL FILE
    private fun getPhotoFile(fileName: String): File{
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, "jpg", storageDirectory)
    }

    //get PDF file name
    private fun Context.getFileName(uri: Uri): String? = when(uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun Context.getContentFileName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
        }
    }.getOrNull()


    //REPEAT TRANSACTION
    private fun openRepeatBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_repeat)

        val confirmBtn = bottomSheet.findViewById<Button>(R.id.repeatConfirm_btn) as Button
        val freqRadioGroup = bottomSheet.findViewById<RadioGroup>(R.id.repeatFrequency_radioBtn) as RadioGroup
        val datePicker = bottomSheet.findViewById<DatePicker>(R.id.repeatEnd_datePicker) as DatePicker


        confirmBtn.setOnClickListener {

            val selectedBtnID = freqRadioGroup.checkedRadioButtonId
            val selectedFreq = bottomSheet.findViewById<RadioButton>(selectedBtnID)?.text
            val date: String = "${datePicker.dayOfMonth} / ${datePicker.month} / ${datePicker.year}"

            expenseFrequency_txt.text = selectedFreq
            expenseRepeatEnd_txt.text = date

            bottomSheet.dismiss()

        }

        bottomSheet.show()
    }
}