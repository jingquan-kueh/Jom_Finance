package com.example.jom_finance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.bottomsheet_attachment.*
import kotlinx.android.synthetic.main.bottomsheet_repeat.*

class AddNewExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_expense)

        //drop down list
        val exp = listOf("Shopping", "Groceries", "Transport", "Restaurant")
        val expAdapter = ArrayAdapter(this, R.layout.item_dropdown, exp)
        expenseCategory_autoCompleteTextView.setAdapter(expAdapter)

        val acc = listOf("Bank", "Cash", "Grab Pay", "Touch n Go")
        val accAdapter = ArrayAdapter(this, R.layout.item_dropdown, acc)
        expenseAccount_autoCompleteTextView.setAdapter(accAdapter)

        //hide repeat
        repeat_constraintLayout.visibility = View.GONE


        expenseAddAttachment_btn.setOnClickListener {
            openAttachmentBottomSheetDialog()
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

    }

    private fun openAttachmentBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_attachment)

        val camera = bottomSheet.findViewById<Button>(R.id.attachementCamera_btn) as Button
        val image = bottomSheet.findViewById<Button>(R.id.attachementImage_btn) as Button
        val document = bottomSheet.findViewById<Button>(R.id.attachementDocument_btn) as Button

        camera.setOnClickListener {
            Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show()
            //bottomSheet.dismiss()
        }

        image.setOnClickListener {
            Toast.makeText(this, "Image", Toast.LENGTH_SHORT).show()
        }

        document.setOnClickListener {
            Toast.makeText(this, "Document", Toast.LENGTH_SHORT).show()
        }

        bottomSheet.show()
    }

    private fun openRepeatBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_repeat)

        val freq = listOf("Daily", "Weekly", "Monthly", "Yearly")
        val freqAdapter = ArrayAdapter(this, R.layout.item_dropdown, freq)
        //repeatFrequency_autoCompleteTextView.setAdapter(freqAdapter)

        bottomSheet.show()
    }
}