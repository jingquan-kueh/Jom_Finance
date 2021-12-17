package com.example.jom_finance

import android.app.*
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
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
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.activity_add_new_expense.attachmentDocument_txt
import kotlinx.android.synthetic.main.activity_add_new_expense.attachment_img
import kotlinx.android.synthetic.main.activity_add_new_expense.progressLayout
import kotlinx.android.synthetic.main.activity_add_new_expense.repeat_constraintLayout
import kotlinx.android.synthetic.main.activity_add_new_income.*
import kotlinx.android.synthetic.main.bottomsheet_attachment.*
import kotlinx.android.synthetic.main.bottomsheet_repeat.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

private lateinit var fAuth: FirebaseAuth
private lateinit var fStore: FirebaseFirestore
private lateinit var userID: String

private lateinit var transactionID: String

private const val TRANSACTION_TYPE = "expense"
private var transactionAmount: Double = 0.0
private lateinit var transactionCategory: String
private lateinit var transactionDescription: String
private lateinit var transactionAccount: String
private lateinit var transactionTimestamp: Timestamp
private var transactionAttachment: Boolean = false

private const val IMAGE_REQUEST_CODE = 100
private const val CAMERA_REQUEST_CODE = 42
private const val DOCUMENT_REQUEST_CODE = 111

private const val CHANNEL_ID = "channel_id_budget"
private const val notificationID = 101

private lateinit var attachmentType: String
private const val FILE_NAME = "photo.jpg" //temporary file name
private lateinit var photoFile: File
private lateinit var imageUri: Uri
private lateinit var imageBitmap: Bitmap
private lateinit var documentUri: Uri

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

private lateinit var accountArrayList : java.util.ArrayList<Account>

class AddNewExpenseActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_expense)
        setupDataBase()
        accountArrayList = arrayListOf()
        //hide repeat section
        repeat_constraintLayout.visibility = View.GONE

        //hide attachment image and doc
        attachment_img.visibility = View.GONE
        attachmentDocument_txt.visibility = View.GONE

        val voiceExpenseIntent = intent.getBooleanExtra("voiceExpense", false)

        transactionID = intent?.extras?.getString("transactionName").toString()
        if (transactionID != "null") {
            //UPDATE TRANSACTION
            transactionAmount = intent?.extras?.getDouble("transactionAmount")!!
            transactionCategory = intent?.extras?.getString("transactionCategory").toString()
            transactionAccount = intent?.extras?.getString("transactionAccount").toString()
            transactionDescription = intent?.extras?.getString("transactionDescription").toString()
            transactionAttachment = intent?.extras?.getBoolean("transactionAttachment")!!

            expenseAmount_edit.text = Editable.Factory.getInstance().newEditable(String.format("%.2f", transactionAmount))
            expenseCategory_ddl.editText?.text = Editable.Factory.getInstance().newEditable(transactionCategory)
            expenseAccount_ddl.editText?.text = Editable.Factory.getInstance().newEditable(transactionAccount)
            expenseDescription_outlinedTextField.editText?.text = Editable.Factory.getInstance().newEditable(transactionDescription)

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
            expenseDate_edit.text = Editable.Factory.getInstance().newEditable("$date")
            expenseTime_edit.text = Editable.Factory.getInstance().newEditable("$savedHour:$savedMinute")
            timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"

            createNotificationChannel()
            expenseConfirm_btn.setOnClickListener {
                if (expenseValidate())
                    updateExpense()

            }

            if (transactionAttachment) {
                expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24,
                    0,
                    0,
                    0)
                expenseAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
                expenseAddAttachment_btn.text = "Remove Attachment"

                attachmentType = intent?.extras?.getString("attachmentType").toString()
                val filePath = intent?.extras?.getString("attachmentPath").toString()

                if(attachmentType == "document"){
                    attachmentDocument_txt.isVisible = true
                    val fileName = intent?.extras?.getString("attachmentName").toString()
                    attachmentDocument_txt.text = fileName
                    attachmentDocument_txt.setOnClickListener {
                        documentUri = FileProvider.getUriForFile(this, this.applicationContext.packageName+".fileprovider", File(filePath))
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = documentUri
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


        }else {
            //ADD TRANSACTION
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
            expenseDate_edit.text = Editable.Factory.getInstance().newEditable("$savedDay-$savedMonth-$savedYear")
            expenseTime_edit.text = Editable.Factory.getInstance().newEditable("$savedHour:$savedMinute")
            timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"

            createNotificationChannel()
            expenseConfirm_btn.setOnClickListener {
                if (expenseValidate()) {
                    addExpenseToDatabase()
                    updateBudget()
                }
            }

        }
        if (voiceExpenseIntent) {
            val amount = intent.getDoubleExtra("expenseAmount", 0.0)
            val description = intent.getStringExtra("expenseDescription")
            expenseAmount_edit.text = Editable.Factory.getInstance().newEditable(amount.toString())
            expenseDescription_outlinedTextField.editText?.text =
                Editable.Factory.getInstance().newEditable(description)
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
        val catAdapter = ArrayAdapter(this, R.layout.item_dropdown, cat)
        expenseCategory_autoCompleteTextView.setAdapter(catAdapter)

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
        expenseAccount_autoCompleteTextView.setAdapter(accAdapter)

        //set attachment if come from receipt scanner
        val imagePath = intent?.extras?.getString("image_path").toString()
        if (imagePath != "null") {
            expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24,
                0,
                0,
                0)
            expenseAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
            expenseAddAttachment_btn.text = "Remove Attachment"
            attachment_img.visibility = View.VISIBLE

            val image = BitmapFactory.decodeFile(imagePath)
            val bitmap = image.rotate(0f)

            imageBitmap = bitmap
            attachment_img.setImageBitmap(bitmap)
            transactionAttachment = true
            attachmentType = "camera"

            //create instance of text recognizer
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            //create InputImage object from Bitmap
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            //process the image
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    //loop through all elements and find "total"
                    var foundTotal = false
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            for (element in line.elements) {
                                val elementText = element.text
                                if (elementText.equals("total", true))
                                    foundTotal = true

                                if (foundTotal) {
                                    try {
                                        if (elementText.contains(".")) {
                                            val total = elementText.toDouble()
                                            if (total > 0.0)
                                                expenseAmount_edit.text = Editable.Factory.getInstance().newEditable(String.format("%.2f", total))
                                        }
                                    } catch (e: Exception) {

                                    }
                                }
                            }
                        }
                    }

                    //Description
                    val blocks = visionText.textBlocks
                    val linesOfFirstBlock = blocks[0].lines
                    val firstLineOfBlock = linesOfFirstBlock[0].text
                    expenseDescription_outlinedTextField.editText?.text = Editable.Factory.getInstance().newEditable(firstLineOfBlock)

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }

        }

        expenseDate_edit.setOnClickListener {
            DatePickerDialog(this, this, year, month, day).show()
        }

        expenseTime_edit.setOnClickListener {
            TimePickerDialog(this, this, hour, minute, true).show()
        }

        expenseAddAttachment_btn.setOnClickListener {
            if (attachment_img.visibility == View.GONE && attachmentDocument_txt.visibility == View.GONE) {
                openAttachmentBottomSheetDialog()
            } else {
                attachmentDocument_txt.visibility = View.GONE
                attachment_img.visibility = View.GONE
                expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24,
                    0,
                    0,
                    0)
                expenseAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
                expenseAddAttachment_btn.text = "Add Attachment"
                transactionAttachment = false
            }
        }

        expenseRepeatEdit_btn.setOnClickListener {
            openRepeatBottomSheetDialog()
        }

        expenseRepeat_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                repeat_constraintLayout.visibility = View.VISIBLE
            else
                repeat_constraintLayout.visibility = View.GONE
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = String.format("%02d", dayOfMonth)
        savedMonth = String.format("%02d", month + 1)
        savedYear = year.toString()

        expenseDate_edit.text =
            Editable.Factory.getInstance().newEditable("$savedDay-$savedMonth-$savedYear")
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        savedHour = String.format("%02d", hour)
        savedMinute = String.format("%02d", minute)

        expenseTime_edit.text =
            Editable.Factory.getInstance().newEditable("$savedHour:$savedMinute")
    }

    private fun expenseValidate(): Boolean {
        if (expenseAmount_edit.text.equals(0) || expenseAmount_edit.text.isNullOrBlank()) {
            expenseAmount_edit.requestFocus()
            return false
        }
        if (expenseDescription_outlinedTextField.editText?.text.isNullOrEmpty() || expenseDescription_outlinedTextField.editText?.text.isNullOrEmpty()) {
            expenseDescription_outlinedTextField.editText?.requestFocus()
            return false
        }
        if (expenseCategory_autoCompleteTextView.text.isNullOrEmpty() || expenseCategory_autoCompleteTextView.text.isNullOrBlank()) {
            expenseCategory_autoCompleteTextView.requestFocus()
            return false
        }
        if (expenseAccount_autoCompleteTextView.text.isNullOrEmpty() || expenseAccount_autoCompleteTextView.text.isNullOrBlank()) {
            expenseAccount_autoCompleteTextView.requestFocus()
            return false
        }
        return true
    }

    private fun addExpenseToDatabase() {
        transactionAmount = expenseAmount_edit.text.toString().toDouble()
        transactionCategory = expenseCategory_ddl.editText?.text.toString()
        transactionDescription = expenseDescription_outlinedTextField.editText?.text.toString()
        transactionAccount = expenseAccount_ddl.editText?.text.toString()

        timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm")
        val date: Date = sdf.parse(timestampString)

        transactionTimestamp = Timestamp(date)


        try {
            var lastExpense by Delegates.notNull<Int>()
            fStore.collection("transaction").document(userID)
                .get()
                .addOnCompleteListener {
                    lastExpense = it.result["Transaction_counter"].toString().toInt() // Get lastExpense Index
                    fStore.collection("transaction/$userID/Transaction_detail")
                        .whereEqualTo("Transaction_type", "expense")
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                var newExpense = lastExpense.inc() // lastExpense Increment
                                //Set Transaction Pathway
                                var documentReference =
                                    fStore.collection("transaction/$userID/Transaction_detail")
                                        .document("transaction$newExpense")

                                //transaction details
                                val transactionDetails = Transaction("transaction$newExpense",
                                    transactionAmount, transactionAccount, transactionAttachment,
                                    transactionCategory, transactionDescription, TRANSACTION_TYPE,
                                    transactionTimestamp)

                                documentReference.set(transactionDetails).addOnSuccessListener {

    //store attachment if necessary
    if (transactionAttachment) {

         progressLayout.visibility = View.VISIBLE
         val storageReference =
             FirebaseStorage.getInstance().getReference("transaction_images/$userID/transaction$newExpense")
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
              "image" ->
                  uploadTask = storageReference.putFile(imageUri, storageMetadata {
                      setCustomMetadata("file_type", attachmentType)
                  })
              "document" ->
                  uploadTask = storageReference.putFile(documentUri, storageMetadata {
                      setCustomMetadata("file_type", attachmentType)
                      setCustomMetadata("file_name", getFileName(documentUri))
                  })
          }

        uploadTask
            .addOnSuccessListener {
                progressLayout.visibility = View.GONE
                updateAccount()
                updateExpenseValue()
                //Update Transaction counter
                fStore.collection("transaction").document(userID)
                    .update("Transaction_counter", lastExpense.inc())
                Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT)
                    .show()
            }

                                    }else{
                                        updateAccount()
                                        updateExpenseValue()
                                        //Update Transaction counter
                                        fStore.collection("transaction").document(userID)
                                            .update("Transaction_counter", lastExpense.inc())
                                    }

                                    // Popout Msg
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

                }
        } catch (e: Exception) {
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun updateExpense(){

        val newAmount = expenseAmount_edit.text.toString().toDouble()
        val newCategory = expenseCategory_ddl.editText?.text.toString()
        val newAccount = expenseAccount_ddl.editText?.text.toString()
        transactionDescription = expenseDescription_outlinedTextField.editText?.text.toString()

        timestampString = "$savedDay-$savedMonth-$savedYear $savedHour:$savedMinute"
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm")
        val date: Date = sdf.parse(timestampString)

        transactionTimestamp = Timestamp(date)

        //Set Transaction Pathway
        var documentReference = fStore.collection("transaction/$userID/Transaction_detail").document(transactionID)

        //transaction details
        val transactionDetails = Transaction(transactionID, newAmount, newAccount, transactionAttachment, newCategory, transactionDescription, TRANSACTION_TYPE, transactionTimestamp)

        documentReference.set(transactionDetails).addOnSuccessListener {

            //check if category has changed
            if(newCategory != transactionCategory || newAmount != transactionAmount){

                val monthHashMap: HashMap<Int, String> = hashMapOf(
                    1 to "January",
                    2 to "February",
                    3 to "March",
                    4 to "April",
                    5 to "May",
                    6 to "June",
                    7 to "July",
                    8 to "August",
                    9 to "September",
                    10 to "October",
                    11 to "November",
                    12 to "December"
                )

                val budgetMonth = savedMonth.toInt()
                val budgetDate = monthHashMap[budgetMonth] + " $savedYear"
                val budgetRef = fStore.collection("budget/$userID/budget_detail")

                //minus old amount in old budget
                budgetRef
                    .whereEqualTo("budget_date", budgetDate)
                    .whereEqualTo("budget_category", transactionCategory)
                    .get()
                    .addOnSuccessListener {
                        for (document in it.documents)
                            if (document.exists()) {

                                val budgetID = document.getString("budget_id")!!
                                var budgetSpent = document.data?.getValue("budget_spent").toString().toDouble()
                                budgetSpent -= transactionAmount
                                budgetRef.document(budgetID)
                                    .update("budget_spent", budgetSpent)
                                    .addOnSuccessListener {
                                    }
                            } else
                                Toast.makeText(this, "There is no budget for this category", Toast.LENGTH_SHORT).show()
                    }

                //add new amount in new budget
                budgetRef
                    .whereEqualTo("budget_date", budgetDate)
                    .whereEqualTo("budget_category", newCategory)
                    .get()
                    .addOnSuccessListener {
                        for (document in it.documents)
                            if (document.exists()) {
                                val budgetID = document.getString("budget_id")!!
                                var budgetSpent = document.data?.getValue("budget_spent").toString().toDouble()
                                budgetSpent += transactionAmount
                                budgetRef.document(budgetID)
                                    .update("budget_spent", budgetSpent)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Updated budget", Toast.LENGTH_SHORT).show()
                                    }

                                val budgetAlert = document.data?.getValue("budget_alert").toString().toBoolean()
                                if(budgetAlert){
                                    val budgetAmount = document.data?.getValue("budget_amount").toString().toDouble()
                                    val budgetAlertPercentage = document.data?.getValue("budget_alert_percentage").toString().toInt()
                                    val budgetAlertAmount = budgetAmount * budgetAlertPercentage/100
                                    if(budgetSpent > budgetAlertAmount){
                                        Toast.makeText(this, "exceeded alert amount", Toast.LENGTH_SHORT).show()
                                        sendBudgetExceededNotification(budgetAlertPercentage, transactionCategory, budgetDate)
                                    }
                                }

                            } else
                                Toast.makeText(this, "There is no budget for this category", Toast.LENGTH_SHORT).show()
                    }
            }

            if(newAccount != transactionAccount || newAmount != transactionAmount){

                val accountRef = fStore.collection("accounts/$userID/account_detail")

                //add old expense amount in old account
                accountRef.document(transactionAccount)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            var accountAmount = document.data?.getValue("account_amount").toString().toDouble()
                            accountAmount += transactionAmount
                            accountRef.document(transactionAccount)
                                .update("account_amount", accountAmount)
                                .addOnSuccessListener {
                                }
                        }
                    }

                //minus new expense amount in new account
                accountRef.document(newAccount)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            var accountAmount = document.data?.getValue("account_amount").toString().toDouble()
                            accountAmount -= transactionAmount
                            accountRef.document(newAccount)
                                .update("account_amount", accountAmount)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Updated account", Toast.LENGTH_SHORT).show()
                                    if (newAmount != transactionAmount)
                                        updateTotalAccountAmount()
                                }
                        }
                    }

            }
            // Popout Msg
            val resetView = LayoutInflater.from(this).inflate(R.layout.popup_update_success, null)
            val resetViewBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog).setView(resetView)
            val displayDialog = resetViewBuilder.show()
            displayDialog.setOnDismissListener {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finishAffinity()
            }

        }

    }

    private fun updateBudget() {
        val monthHashMap: HashMap<Int, String> = hashMapOf(
            1 to "January",
            2 to "February",
            3 to "March",
            4 to "April",
            5 to "May",
            6 to "June",
            7 to "July",
            8 to "August",
            9 to "September",
            10 to "October",
            11 to "November",
            12 to "December"
        )

        val budgetMonth = savedMonth.toInt()
        val budgetDate = monthHashMap[budgetMonth] + " $savedYear"
        val budgetRef = fStore.collection("budget/$userID/budget_detail")

        budgetRef
            .whereEqualTo("budget_date", budgetDate)
            .whereEqualTo("budget_category", transactionCategory)
            .get()
            .addOnSuccessListener {
                for (document in it.documents)
                    if (document.exists()) {
                        val budgetID = document.getString("budget_id")!!
                        var budgetSpent = document.data?.getValue("budget_spent").toString().toDouble()
                        budgetSpent += transactionAmount
                        budgetRef.document(budgetID)
                            .update("budget_spent", budgetSpent)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Updated budget", Toast.LENGTH_SHORT).show()
                            }

                        val budgetAlert = document.data?.getValue("budget_alert").toString().toBoolean()
                        if(budgetAlert){
                            val budgetAmount = document.data?.getValue("budget_amount").toString().toDouble()
                            val budgetAlertPercentage = document.data?.getValue("budget_alert_percentage").toString().toInt()
                            val budgetAlertAmount = budgetAmount * budgetAlertPercentage/100
                            if(budgetSpent > budgetAlertAmount){
                                Toast.makeText(this, "exceeded alert amount", Toast.LENGTH_SHORT).show()
                                sendBudgetExceededNotification(budgetAlertPercentage, transactionCategory, budgetDate)
                            }

                        }

                    } else
                        Toast.makeText(this, "There is no budget for this category", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error updating budget", it)
            }
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "channel"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendBudgetExceededNotification(percentage: Int, category: String, date: String){

        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        intent.putExtra("fragment_to_load", "budget")

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.jomfinance_foreground)
            .setContentTitle("Budget Alert!")
            .setContentText("You've exceeded $percentage% of your budget for $category, $date")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            notify(notificationID, builder.build())
        }
    }




    private fun updateAccount() {
        val accountRef = fStore.collection("accounts/$userID/account_detail").document(transactionAccount)

        accountRef
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    var accountAmount = document.data?.getValue("account_amount").toString().toDouble()
                    accountAmount -= transactionAmount
                    accountRef
                        .update("account_amount", accountAmount)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Updated account", Toast.LENGTH_SHORT).show()
                            updateTotalAccountAmount()
                        }
                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error updating account amount", it)
            }
    }

    private fun updateTotalAccountAmount(){
        var totalAccountAmount = 0.0
        fStore.collection("accounts/$userID/account_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            accountArrayList.add(dc.document.toObject(Account::class.java))
                            totalAccountAmount += accountArrayList.last().accountAmount!!
                        }
                    }
                    if(totalAccountAmount != 0.0){
                        fStore.collection("accounts").document(userID)
                            .update("Total",totalAccountAmount)
                    }
                }
            })
    }



    private fun updateExpenseValue() {
        fStore.collection("transaction").document(userID)
            .get()
            .addOnCompleteListener { value ->
                val expense_amount: Double =
                    value.result["Expense"].toString().toDouble()
                val newExpenseAmount: Double = expense_amount + transactionAmount

                //Update Expense Amount
                fStore.collection("transaction").document(userID)
                    .update("Expense", newExpenseAmount)
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

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    //After get attachment
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_red_24,
            0,
            0,
            0)
        expenseAddAttachment_btn.setTextColor(Color.parseColor("#FD3C4A"))
        expenseAddAttachment_btn.text = "Remove Attachment"

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            attachment_img.visibility = View.VISIBLE
            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
            val bitmap = takenImage.rotate(0f)
            imageBitmap = bitmap

            attachment_img.setImageBitmap(bitmap)

            transactionAttachment = true
            attachmentType = "camera"

        } else if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            attachment_img.visibility = View.VISIBLE
            imageUri = data?.data!!
            attachment_img.setImageURI(imageUri)

            transactionAttachment = true
            attachmentType = "image"
        } else if (requestCode == DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            attachmentDocument_txt.visibility = View.VISIBLE
            documentUri = data?.data!!
            attachmentDocument_txt.text = getFileName(documentUri)

            transactionAttachment = true
            attachmentType = "document"

            attachmentDocument_txt.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = documentUri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(intent)
            }

        } else {
            attachmentDocument_txt.visibility = View.GONE
            attachment_img.visibility = View.GONE
            expenseAddAttachment_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_attachment_24,
                0,
                0,
                0)
            expenseAddAttachment_btn.setTextColor(Color.parseColor("#91919F"))
            expenseAddAttachment_btn.text = "Add Attachment"
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

            expenseFrequency_txt.text = selectedFreq
            expenseRepeatEnd_txt.text = date

            bottomSheet.dismiss()

        }

        bottomSheet.show()
    }
}