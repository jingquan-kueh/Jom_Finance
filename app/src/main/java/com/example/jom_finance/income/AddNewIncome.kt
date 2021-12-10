package com.example.jom_finance.income

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.metrics.Event
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.adapters.ViewBindingAdapter
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.R
import com.example.jom_finance.models.Account
import com.example.jom_finance.models.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storageMetadata
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.activity_add_new_income.*
import kotlinx.android.synthetic.main.activity_add_new_income.attachmentDocument_txt
import kotlinx.android.synthetic.main.activity_add_new_income.attachment_img
import kotlinx.android.synthetic.main.activity_add_new_income.progressLayout
import kotlinx.android.synthetic.main.activity_add_new_income.repeat_constraintLayout
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


private const val TRANSACTION_TYPE = "income"
private var incomeAmount: Double = 0.0
private lateinit var incomeCategory: String
private lateinit var incomeDescription: String
private lateinit var incomeAccount: String
private var incomeAttachment: Boolean = false
private lateinit var transactionTimestamp: Timestamp
private var oldIncomeAmount: Double = 0.0
private lateinit var oldIncomeAccount: String

private lateinit var fAuth: FirebaseAuth
private lateinit var fStore: FirebaseFirestore
private lateinit var userID: String

private const val IMAGE_REQUEST_CODE = 100
private const val CAMERA_REQUEST_CODE = 42
private const val DOCUMENT_REQUEST_CODE = 111

private lateinit var attachmentType: String
private const val FILE_NAME = "photo.jpg" //temporary file name
private lateinit var photoFile: File
private lateinit var imageUri: Uri
private lateinit var imageBitmap: Bitmap
private lateinit var documentUri: Uri
private lateinit var incomeID: String

private var day = 0
private var month = 0
private var year = 0
private var hour = 0
private var minute = 0

private lateinit var savedDay: String
private lateinit var savedMonth: String
private lateinit var savedYear: String
private lateinit var savedHour: String
private lateinit var savedMinute: String
private lateinit var timestampString: String

private lateinit var accountArrayList: ArrayList<Account>

class AddNewIncome : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_income)
        setupDataBase()
        accountArrayList = arrayListOf()
        //hide repeat section
        repeat_constraintLayout.visibility = View.GONE

        //hide attachment image
        attachment_img.visibility = View.GONE
        attachmentDocument_txt.visibility = View.GONE

        val editIncomeIntent = intent.getBooleanExtra("editIncome", false)
        val voiceIncomeIntent = intent.getBooleanExtra("voiceIncome", false)
        if (editIncomeIntent) {
            val amount = intent.getDoubleExtra("incomeAmount", 0.0)
            val category = intent.getStringExtra("incomeCategory")
            val description = intent.getStringExtra("incomeDescription")
            val account = intent.getStringExtra("incomeAccount")
            val attachment = intent.getBooleanExtra("incomeAttachment", false)
            incomeID = intent.getStringExtra("incomeID").toString()
            AddnewBtn.text = "Done"
            amountField.setText(amount.toString())
            DescriptionField.setText(description)
            incomeCategory_autoCompleteTextView.setText(category)
            incomeAccount_autoCompleteTextView.setText(account)
            oldIncomeAmount = amount
            oldIncomeAccount = account!!

            //Date and Time
            val date = intent?.extras?.getString("transactionDateNum").toString()
            val dateArray = date.split("-").toTypedArray()
            day = dateArray[0].toInt()
            month = dateArray[1].toInt()
            year = dateArray[2].toInt()

            val time = intent?.extras?.getString("transactionTime").toString()
            val timeArray = time.split(":").toTypedArray()
            hour = timeArray[0].toInt()
            minute = timeArray[1].toInt()

            savedDay = String.format("%02d", day)
            savedMonth = String.format("%02d", month)
            savedYear = year.toString()
            savedHour = String.format("%02d", hour)
            savedMinute = String.format("%02d", minute)
            incomeDate_edit.text = Editable.Factory.getInstance().newEditable("$date")
            incomeTime_edit.text = Editable.Factory.getInstance().newEditable("$savedHour:$savedMinute")
            timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"


            if (attachment) {
                incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24,
                    0,
                    0,
                    0)
                incomeAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
                incomeAddAttachment_btn.text = "Remove Attachment"

                val fileType = intent?.extras?.getString("attachmentType").toString()
                val filePath = intent?.extras?.getString("attachmentPath").toString()

                if(fileType == "document"){
                    attachmentDocument_txt.isVisible = true
                    val fileName = intent?.extras?.getString("attachmentName").toString()
                    attachmentDocument_txt.text = fileName
                    attachmentDocument_txt.setOnClickListener {
                        val uri = FileProvider.getUriForFile(this, this.applicationContext.packageName+".fileprovider", File(filePath))
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = uri
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        startActivity(intent)
                    }
                }
                else{
                    attachment_img.isVisible = true
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    attachment_img.setImageBitmap(bitmap)
                }
            }

        } else {
            //current date & time
            val cal = Calendar.getInstance()
            day = cal.get(Calendar.DAY_OF_MONTH)
            month = cal.get(Calendar.MONTH)
            year = cal.get(Calendar.YEAR)
            hour = cal.get(Calendar.HOUR_OF_DAY)
            minute = cal.get(Calendar.MINUTE)

            savedDay = String.format("%02d", day)
            savedMonth = String.format("%02d", month + 1)
            savedYear = year.toString()
            savedHour = String.format("%02d", hour)
            savedMinute = String.format("%02d", minute)

            incomeDate_edit.text =
                Editable.Factory.getInstance().newEditable("$savedDay-$savedMonth-$savedYear")
            incomeTime_edit.text =
                Editable.Factory.getInstance().newEditable("$savedHour:$savedMinute")
            timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"
        }

        if (voiceIncomeIntent) {
            val amount = intent.getDoubleExtra("incomeAmount", 0.0)
            val description = intent.getStringExtra("incomeDescription")
            amountField.setText(amount.toString())
            DescriptionField.setText(description)
        }

        //category drop down list
        val cat: MutableList<String> = mutableListOf()
        fStore.collection("category/$userID/category_detail")
            .get()
            .addOnSuccessListener {
                for (document in it.documents) {
                    cat.add(document.getString("category_name")!!)
                }
            }

        val incAdapter = ArrayAdapter(this, R.layout.item_dropdown, cat)
        incomeCategory_autoCompleteTextView.setAdapter(incAdapter)

        //accounts drop down list
        val acc: MutableList<String> = mutableListOf()
        fStore.collection("accounts/$userID/account_detail")
            .get()
            .addOnSuccessListener {
                for (document in it.documents) {
                    acc.add(document.getString("account_name")!!)
                }
            }
        val accAdapter = ArrayAdapter(this, R.layout.item_dropdown, acc)
        incomeAccount_autoCompleteTextView.setAdapter(accAdapter)

        incomeDate_edit.setOnClickListener {
            DatePickerDialog(this, this, year, month, day).show()
        }

        incomeTime_edit.setOnClickListener {
            TimePickerDialog(this, this, hour, minute, true).show()
        }

        backBtn.setOnClickListener{
            finish()
        }

        incomeAddAttachment_btn.setOnClickListener {
            if (attachment_img.visibility == View.GONE && attachmentDocument_txt.visibility == View.GONE) {
                openAttachmentBottomSheetDialog()
            } else {
                attachmentDocument_txt.visibility = View.GONE
                attachment_img.visibility = View.GONE
                incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24,
                    0,
                    0,
                    0)
                incomeAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
                incomeAddAttachment_btn.text = "Add Attachment"
                incomeAttachment = false
            }
        }

        incomeRepeatEdit_btn.setOnClickListener {
            openRepeatBottomSheetDialog()
        }

        incomeRepeat_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                repeat_constraintLayout.visibility = View.VISIBLE
            else
                repeat_constraintLayout.visibility = View.GONE
        }

        AddnewBtn.setOnClickListener {
            // Check Income Not Null
            if (incomeValidate()) {
                if (editIncomeIntent) {
                    editIncomeToDatabase()
                } else {
                    addIncomeToDatabase()
                }
            }
        }

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = String.format("%02d", dayOfMonth)
        savedMonth = String.format("%02d", month + 1)
        savedYear = year.toString()

        incomeDate_edit.text =
            Editable.Factory.getInstance().newEditable("$savedDay-$savedMonth-$savedYear")
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        savedHour = String.format("%02d", hour)
        savedMinute = String.format("%02d", minute)

        incomeTime_edit.text = Editable.Factory.getInstance().newEditable("$savedHour:$savedMinute")
    }

    private fun incomeValidate(): Boolean {
        if (amountField.text.equals(0) || amountField.text.isNullOrBlank()) {
            amountField.requestFocus()
            return false
        }
        if (DescriptionField.text.isNullOrEmpty() || DescriptionField.text.isNullOrBlank()) {
            DescriptionField.requestFocus()
            return false
        }
        if (incomeAccount_autoCompleteTextView.text.isNullOrEmpty() || incomeAccount_autoCompleteTextView.text.isNullOrBlank()) {
            incomeAccount_autoCompleteTextView.requestFocus()
            return false
        }
        if (incomeCategory_autoCompleteTextView.text.isNullOrEmpty() || incomeCategory_autoCompleteTextView.text.isNullOrBlank()) {
            incomeCategory_autoCompleteTextView.requestFocus()
            return false
        }
        return true
    }

    private fun editIncomeToDatabase() {
        try {
            //Set Transaction Pathway
            var documentReference =
                fStore.collection("transaction/$userID/Transaction_detail").document(incomeID)

            incomeAmount = amountField.text.toString().toDouble()
            incomeDescription = DescriptionField.text.toString()
            incomeCategory = incomeCategory_autoCompleteTextView.text.toString()
            incomeAccount = incomeAccount_autoCompleteTextView.text.toString()

            timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"
            val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm")
            val date: Date = sdf.parse(timestampString)

            transactionTimestamp = Timestamp(date)

            var transaction = Transaction(incomeID,
                incomeAmount,
                incomeAccount,
                incomeAttachment,
                incomeCategory,
                incomeDescription,
                TRANSACTION_TYPE,
                transactionTimestamp)


            fStore.collection("transaction").document(userID)
                .get()
                .addOnCompleteListener { value ->
                    val income_amount: Double =
                        value.result["Income"].toString().toDouble()
                    var newIncomeAmount: Double = income_amount + incomeAmount
                    newIncomeAmount -= oldIncomeAmount

                    //Update Income Amount
                    fStore.collection("transaction").document(userID)
                        .update("Income", newIncomeAmount)
                }

            //If the category changed
            // Deduct the old category, add to new category
            if (incomeAccount == oldIncomeAccount) {
                fStore.collection("accounts/$userID/account_detail").document(incomeAccount)
                    .get()
                    .addOnCompleteListener { value ->
                        val account_amount: Double =
                            value.result["account_amount"].toString().toDouble()
                        var newAccountAmount: Double = account_amount + incomeAmount
                        newAccountAmount -= oldIncomeAmount

                        //Update Income Amount
                        fStore.collection("accounts/$userID/account_detail").document(incomeAccount)
                            .update("account_amount", newAccountAmount)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, " " + it.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                // First get the old Account
                fStore.collection("accounts/$userID/account_detail").document(oldIncomeAccount)
                    .get()
                    .addOnCompleteListener { value ->
                        val account_amount: Double =
                            value.result["account_amount"].toString().toDouble()
                        // Deduct the old account
                        var newAccountAmount: Double = account_amount - incomeAmount

                        //Update old Account Amount
                        fStore.collection("accounts/$userID/account_detail")
                            .document(oldIncomeAccount)
                            .update("account_amount", newAccountAmount)

                        // Next get the new Account
                        fStore.collection("accounts/$userID/account_detail").document(incomeAccount)
                            .get()
                            .addOnCompleteListener { value ->
                                val account_amount: Double =
                                    value.result["account_amount"].toString().toDouble()
                                var newAccountAmount: Double = account_amount + incomeAmount

                                fStore.collection("accounts/$userID/account_detail")
                                    .document(incomeAccount)
                                    .update("account_amount", newAccountAmount)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, " " + it.message, Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this, " " + it.message, Toast.LENGTH_SHORT).show()
                    }

            }

            documentReference.set(transaction).addOnCompleteListener {
                //store attachment if necessary
                if (incomeAttachment) {

                    val storageReference = FirebaseStorage.getInstance()
                        .getReference("transaction_images/$userID/${transaction.transactionName}")
                    lateinit var uploadTask: UploadTask

                    when (attachmentType) {
                        "camera" -> {
                            val baos = ByteArrayOutputStream()
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                            val data = baos.toByteArray()
                            uploadTask = storageReference.putBytes(data, storageMetadata {
                                setCustomMetadata("file_type", attachmentType)
                            })
                        }
                        "image" -> uploadTask =
                            storageReference.putFile(imageUri, storageMetadata {
                                setCustomMetadata("file_type", attachmentType)
                            })
                        "document" -> uploadTask =
                            storageReference.putFile(documentUri, storageMetadata {
                                setCustomMetadata("file_type", attachmentType)
                                setCustomMetadata("file_name", getFileName(documentUri))
                            })
                    }

                    uploadTask
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                        }

                    val resetView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)
                    val resetViewBuilder =
                        AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(resetView)
                    val displayDialog = resetViewBuilder.show()
                    displayDialog.setOnDismissListener {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        finishAffinity()
                    }
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(this, " " + ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addIncomeToDatabase() {
        var lastIncome by Delegates.notNull<Int>()
        try {
            fStore.collection("transaction").document(userID)
                .get()
                .addOnCompleteListener {
                    lastIncome =
                        it.result["Transaction_counter"].toString().toInt() // Get lastIncome Index
                    fStore.collection("transaction/$userID/Transaction_detail")
                        .whereEqualTo("Transaction_type", "income")
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                var newIncome = lastIncome.inc() // LastIncome Increment
                                incomeAmount = amountField.text.toString().toDouble()
                                incomeDescription = DescriptionField.text.toString()
                                incomeCategory = incomeCategory_autoCompleteTextView.text.toString()
                                incomeAccount = incomeAccount_autoCompleteTextView.text.toString()

                                timestampString =
                                    "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"
                                val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm")
                                val date: Date = sdf.parse(timestampString)

                                transactionTimestamp = Timestamp(date)

                                //Set Transaction Pathway
                                var documentReference =
                                    fStore.collection("transaction/$userID/Transaction_detail")
                                        .document("transaction$newIncome")

                                var transaction =
                                    Transaction("transaction$newIncome",
                                        incomeAmount,
                                        incomeAccount,
                                        incomeAttachment,
                                        incomeCategory,
                                        incomeDescription,
                                        TRANSACTION_TYPE, transactionTimestamp)

                                //Insert Income to FireStore
                                documentReference.set(transaction).addOnSuccessListener {

                                    //store attachment if necessary
                                    if (incomeAttachment) {
                                        progressLayout.visibility = View.VISIBLE
                                        val storageReference = FirebaseStorage.getInstance()
                                            .getReference("transaction_images/$userID/transaction$newIncome")
                                        lateinit var uploadTask: UploadTask

                                        when (attachmentType) {
                                            "camera" -> {
                                                val baos = ByteArrayOutputStream()
                                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                                                val data = baos.toByteArray()
                                                uploadTask = storageReference.putBytes(data, storageMetadata {
                                                    setCustomMetadata("file_type", attachmentType)
                                                })
                                            }
                                            "image" -> uploadTask =
                                                storageReference.putFile(imageUri, storageMetadata {
                                                    setCustomMetadata("file_type", attachmentType)
                                                })
                                            "document" -> uploadTask =
                                                storageReference.putFile(documentUri, storageMetadata {
                                                    setCustomMetadata("file_type", attachmentType)
                                                    setCustomMetadata("file_name", getFileName(documentUri))
                                                })
                                        }
                                        uploadTask
                                            .addOnSuccessListener {
                                                progressLayout.visibility = View.GONE
                                                updateAccount()
                                                updateIncomeValue()
                                                //Update Transaction counter
                                                fStore.collection("transaction").document(userID)
                                                    .update("Transaction_counter", lastIncome.inc())
                                                    .addOnSuccessListener {
                                                        val resetView =
                                                            LayoutInflater.from(this)
                                                                .inflate(R.layout.activity_popup, null)
                                                        val resetViewBuilder =
                                                            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                                                                .setView(resetView)
                                                        val displayDialog = resetViewBuilder.show()
                                                        displayDialog.setOnDismissListener {
                                                            val intent = Intent(this, HomeActivity::class.java)
                                                            startActivity(intent)
                                                            overridePendingTransition(R.anim.slide_in_right,
                                                                R.anim.slide_out_left)
                                                            finishAffinity()
                                                        }
                                                    }
                                                Toast.makeText(this,
                                                    "Successfully uploaded",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                    }else{
                                        updateAccount()
                                        updateIncomeValue()
                                        //Update Transaction counter
                                        fStore.collection("transaction").document(userID)
                                            .update("Transaction_counter", lastIncome.inc())
                                            .addOnSuccessListener {
                                                val resetView =
                                                    LayoutInflater.from(this)
                                                        .inflate(R.layout.activity_popup, null)
                                                val resetViewBuilder =
                                                    AlertDialog.Builder(this, R.style.CustomAlertDialog)
                                                        .setView(resetView)
                                                val displayDialog = resetViewBuilder.show()
                                                displayDialog.setOnDismissListener {
                                                    val intent = Intent(this, HomeActivity::class.java)
                                                    startActivity(intent)
                                                    overridePendingTransition(R.anim.slide_in_right,
                                                        R.anim.slide_out_left)
                                                    finishAffinity()
                                                }
                                            }
                                    }
                                }
                                    .addOnFailureListener { ex ->
                                        Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { ex ->
                            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                        }
                }

        } catch (e: Exception) {
            Toast.makeText(this, " " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAccount() {
        fStore.collection("accounts/$userID/account_detail").document(incomeAccount)
            .get()
            .addOnCompleteListener { value ->
                val account_amount: Double =
                    value.result["account_amount"].toString().toDouble()
                val newAccountAmount: Double = account_amount + incomeAmount

                //Update Income Amount
                fStore.collection("accounts/$userID/account_detail").document(incomeAccount)
                    .update("account_amount", newAccountAmount)
                updateTotalAccountAmount()
            }

    }

    private fun updateTotalAccountAmount() {
        var totalAccountAmount = 0.0
        fStore.collection("accounts/$userID/account_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("FireStore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            accountArrayList.add(dc.document.toObject(Account::class.java))
                            totalAccountAmount += accountArrayList.last().accountAmount!!
                        }
                    }
                    if (totalAccountAmount != 0.0) {
                        fStore.collection("accounts").document(userID)
                            .update("Total", totalAccountAmount)
                    }
                }
            })

    }

    private fun updateIncomeValue() {
        fStore.collection("transaction").document(userID)
            .get()
            .addOnCompleteListener { value ->
                val income_amount: Double =
                    value.result["Income"].toString().toDouble()
                val newIncomeAmount: Double = income_amount + incomeAmount

                //Update Income Amount
                fStore.collection("transaction").document(userID)
                    .update("Income", newIncomeAmount)
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

    //For reset Income
    private fun resetIncome() {
        //Reset Income Amount
        fStore.collection("transaction").document(userID).update("Income", 0)
    }

    //After get attachment
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24,
            0,
            0,
            0)
        incomeAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
        incomeAddAttachment_btn.text = "Remove Attachment"

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            attachment_img.visibility = View.VISIBLE
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageBitmap = takenImage

            // TODO: 5/11/2021 Image orientation

            attachment_img.setImageBitmap(takenImage)

            incomeAttachment = true
            attachmentType = "camera"

        } else if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            attachment_img.visibility = View.VISIBLE
            imageUri = data?.data!!
            attachment_img.setImageURI(imageUri)

            incomeAttachment = true
            attachmentType = "image"
        } else if (requestCode == DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            attachmentDocument_txt.visibility = View.VISIBLE
            documentUri = data?.data!!
            attachmentDocument_txt.text = getFileName(documentUri)

            incomeAttachment = true
            attachmentType = "document"

        } else {
            attachmentDocument_txt.visibility = View.GONE
            attachment_img.visibility = View.GONE
            incomeAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24,
                0,
                0,
                0)
            incomeAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
            incomeAddAttachment_btn.text = "Add Attachment"
        }

    }

    private fun openAttachmentBottomSheetDialog() {
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_attachment)

        val camera = bottomSheet.findViewById<Button>(R.id.attachementCamera_btn) as Button
        val image = bottomSheet.findViewById<Button>(R.id.attachementImage_btn) as Button
        val document = bottomSheet.findViewById<Button>(R.id.attachementDocument_btn) as Button

        camera.setOnClickListener {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider =
                FileProvider.getUriForFile(this, "com.example.jom_finance.fileprovider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            if (takePictureIntent.resolveActivity(this.packageManager) != null)
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
            startActivityForResult(Intent.createChooser(pickDocumentIntent, "Select a document"),
                DOCUMENT_REQUEST_CODE)

            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    // TODO: 5/11/2021 COULD PUT THE FOLLOWING THREE FUNCTIONS IN A UTIL FILE
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, "jpg", storageDirectory)
    }

    //get PDF file name
    private fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun Context.getContentFileName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                .let(cursor::getString)
        }
    }.getOrNull()

    //REPEAT TRANSACTION
    private fun openRepeatBottomSheetDialog() {
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_repeat)

        val confirmBtn = bottomSheet.findViewById<Button>(R.id.repeatConfirm_btn) as Button
        val freqRadioGroup =
            bottomSheet.findViewById<RadioGroup>(R.id.repeatFrequency_radioBtn) as RadioGroup
        val datePicker =
            bottomSheet.findViewById<DatePicker>(R.id.repeatEnd_datePicker) as DatePicker


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