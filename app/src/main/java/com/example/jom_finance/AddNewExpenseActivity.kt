package com.example.jom_finance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_add_new_expense.*
import kotlinx.android.synthetic.main.activity_create_budget.*

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

        expenseAddAttachment_btn.setOnClickListener {
            openBottomSheetDialog()
        }

    }

    private fun openBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_attachment_fragment)

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
}