package com.example.jom_finance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottomsheet_attachment.*
import kotlinx.android.synthetic.main.activity_transfer.*

class TransferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        transferAddAttachment_btn.setOnClickListener {
            openBottomSheetDialog()
        }

    }

    private fun openBottomSheetDialog(){
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


}