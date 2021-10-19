package com.example.jom_finance.income

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import com.example.jom_finance.R
import kotlinx.android.synthetic.main.activity_add_new_income.*

class AddNewIncome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_income)

        ArrayAdapter.createFromResource(
            this,
            R.array.Category,
            R.layout.spinner_list
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_list)
            // Apply the adapter to the spinner
            spinnerCategory.adapter = adapter
        }

        AddnewBtn.setOnClickListener{
            val resetView = LayoutInflater.from(this).inflate(R.layout.activity_popup, null)
            val resetViewBuilder = AlertDialog.Builder(this,R.style.CustomAlertDialog).setView(resetView)
            //show dialog
            val displayDialog = resetViewBuilder.show()
        }
    }

}