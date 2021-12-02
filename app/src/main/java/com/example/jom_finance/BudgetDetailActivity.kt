package com.example.jom_finance

import android.content.Intent
import android.graphics.Color.BLACK
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import kotlinx.android.synthetic.main.activity_budget_detail.*

class BudgetDetailActivity : AppCompatActivity() {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var budgetID : String
    private var budgetAmount : Double = 0.0
    private lateinit var budgetDate : String
    private lateinit var budgetCategory : String
    private var budgetAlert : Boolean = false
    private var budgetAlertPercentage : Int = 0
    private var budgetSpent : Double = 0.0
    private var color : Int = 0
    private var icon : Int = 1

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_detail)
        setupDatabase()

        budgetCategory = intent?.extras?.getString("budget_category").toString()
        if (budgetCategory != "null"){
            budgetID = intent?.extras?.getString("budget_id")!!
            budgetAmount = intent?.extras?.getDouble("budget_amount")!!
            budgetDate = intent?.extras?.getString("budget_date").toString()
            budgetAlert = intent?.extras?.getBoolean("budget_alert")!!
            budgetAlertPercentage = intent?.extras?.getInt("budget_alert_percentage")!!
            budgetSpent = intent?.extras?.getDouble("budget_spent")!!
            color = intent?.extras?.getInt("category_color")!!
            icon = intent?.extras?.getInt("category_icon")!!


            var budgetRemaining = budgetAmount - budgetSpent
            if (budgetRemaining < 0.0)
                budgetRemaining = 0.0

            //Icon
            val loader = IconPackLoader(this)
            val iconPack = createDefaultIconPack(loader)
            val drawable = iconPack.getIconDrawable(icon, IconDrawableLoader(this))
            budgetCategory_text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            budgetCategory_text.compoundDrawables[0].setTint(color)

            budgetCategory_text.text = budgetCategory

            remainingAmount_text.text = "RM " + String.format("%.2f", budgetRemaining)
            budgetSpentDetail_text.text = "You've spent RM" + String.format("%.2f", budgetSpent) + " of RM" + String.format("%.2f", budgetAmount)

            val percentage = (budgetSpent / budgetAmount) * 100
            budgetRemaining_bar.progress = percentage.toInt()
            budgetRemaining_bar.setIndicatorColor(color)

            if(budgetSpent < budgetAmount)
                exceedLimit_text.visibility = View.GONE

            deleteBudget_btn.setOnClickListener {
                openDeleteBottomSheetDialog()
            }

            editBudget_btn.setOnClickListener {
                val intent = Intent(this, CreateBudgetActivity::class.java)
                intent.putExtra("budget_id", budgetID)
                intent.putExtra("budget_amount", budgetAmount)
                intent.putExtra("budget_date", budgetDate)
                intent.putExtra("budget_category", budgetCategory)
                intent.putExtra("budget_alert", budgetAlert)
                intent.putExtra("budget_alert_percentage", budgetAlertPercentage)
                intent.putExtra("budget_spent", budgetSpent)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            backBtn_budgetDetail.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("fragment_to_load", "budget")
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    private fun openDeleteBottomSheetDialog(){
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottomsheet_delete)
        val yesBtn = bottomSheet.findViewById<Button>(R.id.removeYesbtn) as Button
        val noBtn = bottomSheet.findViewById<Button>(R.id.removeNobtn) as Button
        val title = bottomSheet.findViewById<TextView>(R.id.bottomsheetDeleteTitle_text) as TextView
        val description = bottomSheet.findViewById<TextView>(R.id.bottomsheetDeleteDesc_text) as TextView

        title.text = "Remove this budget?"
        description.text = "Are you sure you want to remove this budget?"

        yesBtn.setOnClickListener {
            fStore.collection("budget/$userID/budget_detail").document(budgetID)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Deleted Budget Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not delete account : $it", Toast.LENGTH_SHORT).show()
                }
        }

        noBtn.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun setupDatabase() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null)
            userID = currentUser.uid

    }
}