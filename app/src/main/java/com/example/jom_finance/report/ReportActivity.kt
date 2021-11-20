package com.example.jom_finance.report

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.LoginActivity
import com.example.jom_finance.R
import com.example.jom_finance.fragment.Line_fragment
import com.example.jom_finance.fragment.Pie_fragment
import kotlinx.android.synthetic.main.activity_report.*

class ReportActivity : AppCompatActivity() {

    private var isLine = false
    private var isExpense = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        backBtn.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finishAffinity()
        }

        val checkedId =
            toggleChartGroup.checkedButtonId // Will return View.NO_ID if singleSelection = false
        val checkedIds = toggleChartGroup.checkedButtonIds // Potentially an empty list
        toggleChartGroup.check(R.id.lineChartBtn) // Checks a specific button
        toggleChartGroup.uncheck(R.id.pieChartBtn) // Unchecks a specific button

        supportFragmentManager.beginTransaction()
            .add(R.id.chartFragment, Line_fragment())
            .commit()


        toggleChartGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            isLine = (checkedId == R.id.lineChartBtn)
            if (isLine) {
                changeState(isLine)
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                    .replace(R.id.chartFragment, Line_fragment())
                    .commit()
            } else {
               changeState(isLine)
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                    .replace(R.id.chartFragment, Pie_fragment())
                    .commit()
            }
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.Report_Recycle_Fragment, Report_ExpenseFragment())
            .commit()

        toggleExpenseIncomeGroup.addOnButtonCheckedListener{ group, checkedId, isChecked ->
            isExpense = (checkedId == R.id.ExpenseReportBtn)

            if(isExpense){
                ExpenseReportBtn.setTextColor(ContextCompat.getColor(this,R.color.iris))
                IncomeReportBtn.setTextColor(ContextCompat.getColor(this,R.color.black))

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                    .replace(R.id.Report_Recycle_Fragment, Report_ExpenseFragment())
                    .commit()

            }else{
                ExpenseReportBtn.setTextColor(ContextCompat.getColor(this,R.color.black))
                IncomeReportBtn.setTextColor(ContextCompat.getColor(this,R.color.iris))

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                    .replace(R.id.Report_Recycle_Fragment, Report_IncomeFragment())
                    .commit()
            }
        }





    }
    private fun changeState(isLine : Boolean){
        if(isLine){
            lineChartBtn.setIconTintResource(R.color.iris)
            pieChartBtn.setIconTintResource(R.color.grey)

        }else{
            lineChartBtn.setIconTintResource(R.color.grey)
            pieChartBtn.setIconTintResource(R.color.iris)

        }
    }
    private fun changeStateEI(isLine : Boolean){
        if(isLine){
            lineChartBtn.setIconTintResource(R.color.iris)
            pieChartBtn.setIconTintResource(R.color.grey)

        }else{
            lineChartBtn.setIconTintResource(R.color.grey)
            pieChartBtn.setIconTintResource(R.color.iris)

        }
    }
}
