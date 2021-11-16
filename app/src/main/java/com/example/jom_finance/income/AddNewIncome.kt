package com.example.jom_finance.income

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
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
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.R
import com.example.jom_finance.models.Income
import com.example.jom_finance.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_new_income.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import kotlin.properties.Delegates


private const val TRANSACTION_TYPE = "income"
private var incomeAmount: Double = 0.0
private lateinit var incomeCategory: String
private lateinit var incomeDescription: String
private lateinit var incomeAccount: String
private var incomeAttachment: Boolean = false

private lateinit var fAuth: FirebaseAuth
private lateinit var fStore: FirebaseFirestore
private lateinit var userID: String

private const val IMAGE_REQUEST_CODE = 100
private const val CAMERA_REQUEST_CODE = 42
private const val DOCUMENT_REQUEST_CODE = 111

private lateinit var attachmentType: String
private const val FILE_NAME = "photo.jpg" //temporary file name
private lateinit var photoFile: File
private lateinit var imageUri : Uri
private lateinit var imageBitmap: Bitmap
private lateinit var documentUri : Uri
private lateinit var incomeID : String

class AddNewIncome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_income)
        setupDataBase()
        val editIncomeIntent = intent.getBooleanExtra("editIncome",false)
        if(editIncomeIntent){
            val amount = intent.getDoubleExtra("incomeAmount",0.0)
            val category = intent.getStringExtra("incomeCategory")
            val description = intent.getStringExtra("incomeDescription")
            val account = intent.getStringExtra("incomeAccount")
            val attachment = intent.getBooleanExtra("incomeAttachment",false)
            incomeID = intent.getStringExtra("incomeID").toString()
            AddnewBtn.setText("Done")
            amountField.setText(amount.toString())
            DescriptionField.setText(description)
            incomeCategory_autoCompleteTextView.setText(category)
            incomeAccount_autoCompleteTextView.setText(account)
            if(attachment){
                // Show Attachment
            }

        }

        //category drop down list
        val cat : MutableList<String> = mutableListOf()
        fStore.collection("category/$userID/Category_detail")
            .get()
            .addOnSuccessListener {
                for (document in it.documents){
                    cat.add(document.getString("category_name")!!)
                }
            }

        val incAdapter = ArrayAdapter(this, R.layout.item_dropdown, cat)
        incomeCategory_autoCompleteTextView.setAdapter(incAdapter)

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
        incomeAccount_autoCompleteTextView.setAdapter(accAdapter)

        //hide repeat section
        repeat_constraintLayout.visibility = View.GONE

        //hide attachment image
        attachment_img.visibility = View.GONE
        attachmentDocument_txt.visibility = View.GONE

        incomeAddAttachment_btn.setOnClickListener {
            if(attachment_img.visibility == View.GONE && attachmentDocument_txt.visibility == View.GONE){
                openAttachmentBottomSheetDialog()
            }
            else{
                attachmentDocument_txt.visibility = View.GONE
                attachment_img.visibility = View.GONE
                incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24, 0, 0, 0)
                incomeAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
                incomeAddAttachment_btn.text = "Add Attachment"
                incomeAttachment = false
            }
        }

        incomeRepeatEdit_btn.setOnClickListener {
            openRepeatBottomSheetDialog()
        }

        incomeRepeat_switch.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked)
                repeat_constraintLayout.visibility = View.VISIBLE
            else
                repeat_constraintLayout.visibility = View.GONE
        }

        AddnewBtn.setOnClickListener {
            // Check Income Not Null
            if(incomeValidate()){
                if(editIncomeIntent)
                    editIncomeToDatabase()
                else
                    addIncomeToDatabase()
            }
        }

        attachmentDocument_txt.setOnClickListener {
            // TODO: 6/11/2021 display pdf when clicked
        }
    }

    private fun incomeValidate() :Boolean {
        if(amountField.text.equals(0) || amountField.text.isNullOrBlank()){
            amountField.requestFocus()
            return false
        }
        if(DescriptionField.text.isNullOrEmpty() || DescriptionField.text.isNullOrBlank()){
            DescriptionField.requestFocus()
            return false
        }
        if(incomeAccount_autoCompleteTextView.text.isNullOrEmpty() || incomeAccount_autoCompleteTextView.text.isNullOrBlank()){
            incomeAccount_autoCompleteTextView.requestFocus()
            return false
        }
        if(incomeCategory_autoCompleteTextView.text.isNullOrEmpty() || incomeCategory_autoCompleteTextView.text.isNullOrBlank()){
            incomeCategory_autoCompleteTextView.requestFocus()
            return false
        }
        return true
    }

    private fun editIncomeToDatabase() {
       try{
           //Set Transaction Pathway
           var documentReference =
               fStore.collection("incomes/$userID/Income_detail").document(incomeID)
           //Get Income Detail
           var incomeDetail = Income(incomeID, incomeAmount, incomeAccount,
               incomeAttachment, incomeCategory, incomeDescription)
           documentReference.set(incomeDetail).addOnCompleteListener{
               var transaction = Transaction(incomeDetail.incomeName, incomeDetail.incomeAmount,
                   incomeDetail.incomeAccount,incomeDetail.incomeAttachment,incomeDetail.incomeCategory,
                   incomeDescription, TRANSACTION_TYPE)


               //store attachment if necessary
               if(incomeAttachment){

               }

               documentReference =
                   fStore.collection("transaction/$userID/Transaction_detail").document(incomeID)
               documentReference.set(transaction).addOnSuccessListener {
                   val resetView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)
                   val resetViewBuilder =
                       AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(resetView)
                   val displayDialog = resetViewBuilder.show()
                   displayDialog.setOnDismissListener{
                       val intent = Intent(this, HomeActivity::class.java)
                       startActivity(intent)
                       overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                       finish()
                   }
               }
           }
       }catch (ex : Exception){

       }
    }

    private fun addIncomeToDatabase(){
        var lastIncome by Delegates.notNull<Int>()
        try {
            fStore.collection("incomes/$userID/Income_detail")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        lastIncome = it.result.size()  // Get lastIncome Index
                        var newIncome = lastIncome.inc() // LastIncome Increment
                        incomeAmount = amountField.text.toString().toDouble()
                        incomeDescription = DescriptionField.text.toString()
                        incomeCategory = incomeCategory_autoCompleteTextView.text.toString()
                        incomeAccount = incomeAccount_autoCompleteTextView.text.toString()

                        //Set Transaction Pathway
                        var documentReference =
                            fStore.collection("incomes/$userID/Income_detail").document("income$newIncome")
                        //Get Income Detail
                        var incomeDetail = Income("income$newIncome", incomeAmount, incomeAccount,
                            incomeAttachment, incomeCategory, incomeDescription)

                        //TODO : Update Total Income Amount
                        //documentReference = fStore.collection("incomes").document(userID).set()

                        //Insert Income to FireStore
                        documentReference.set(incomeDetail).addOnSuccessListener {
                            var transaction = Transaction(incomeDetail.incomeName, incomeDetail.incomeAmount,
                                incomeDetail.incomeAccount,incomeDetail.incomeAttachment,incomeDetail.incomeCategory,
                                incomeDescription, TRANSACTION_TYPE)

                            //store attachment if necessary
                            if(incomeAttachment){

                                val storageReference = FirebaseStorage.getInstance().getReference("transaction_images/$userID/transaction$newIncome")
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

                            documentReference =
                                fStore.collection("transaction/$userID/Transaction_detail").document("income$newIncome")
                            documentReference.set(transaction).addOnSuccessListener {
                                val resetView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)
                                val resetViewBuilder =
                                    AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(resetView)
                                val displayDialog = resetViewBuilder.show()
                                displayDialog.setOnDismissListener{
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                    finish()
                                }
                            }

                        }
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
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
    //After get attachment
    override fun onActivityResult(requestCode:Int, resultCode: Int, data:Intent?){

        incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24, 0, 0, 0)
        incomeAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
        incomeAddAttachment_btn.text = "Remove Attachment"

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            attachment_img.visibility = View.VISIBLE
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageBitmap = takenImage

            // TODO: 5/11/2021 Image orientation

            attachment_img.setImageBitmap(takenImage)

            incomeAttachment = true
            attachmentType = "camera"

        }
        else if(requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            attachment_img.visibility = View.VISIBLE
            imageUri = data?.data!!
            attachment_img.setImageURI(imageUri)

            incomeAttachment = true
            attachmentType = "image"
        }
        else if(requestCode == DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            attachmentDocument_txt.visibility = View.VISIBLE
            documentUri = data?.data!!
            attachmentDocument_txt.text = getFileName(documentUri)

            incomeAttachment = true
            attachmentType = "document"

        }
        else{
            attachmentDocument_txt.visibility = View.GONE
            attachment_img.visibility = View.GONE
            incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24, 0, 0, 0)
            incomeAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
            incomeAddAttachment_btn.text = "Add Attachment"
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

            incomeFrequency_txt.text = selectedFreq
            incomeRepeatEnd_txt.text = date

            bottomSheet.dismiss()

        }

        bottomSheet.show()
    }

}