package com.example.jom_finance.report

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dibyendu.picker.listener.PickerListener
import com.dibyendu.picker.util.PickerUtils
import com.dibyendu.picker.view.MonthYearPickerDialog
import com.example.jom_finance.HomeActivity
import com.example.jom_finance.R
import com.example.jom_finance.fragment.Line_fragment
import com.example.jom_finance.fragment.Pie_fragment
import kotlinx.android.synthetic.main.activity_report.*
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private var isLine = true
    private var isExpense = true
    private lateinit var month: String
    private lateinit var year: String
    private lateinit var reportDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        backBtn.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finishAffinity()
        }

        val monthDate = SimpleDateFormat("MMMM")
        val yearDate = SimpleDateFormat("yyyy")
        month = monthDate.format(Date())
        year = yearDate.format(Date())
        reportDate = "$month $year"
        reportDate_text.text = reportDate

        reportDate_text.setOnClickListener{
            MonthYearPickerDialog.show(this,listener = object : PickerListener{
                override fun onSetResult(calendar: Calendar) {
                    month = PickerUtils.getMonth(calendar,PickerUtils.Format.LONG)!!
                    year = PickerUtils.getYear(calendar)!!
                    reportDate = "$month $year"
                    reportDate_text.text = reportDate
                    chartGroupChange(true)
                    transactionGroupChange(true)
                }

            })
        }



        val checkedId =
            toggleChartGroup.checkedButtonId // Will return View.NO_ID if singleSelection = false
        val checkedIds = toggleChartGroup.checkedButtonIds // Potentially an empty list
        toggleChartGroup.check(R.id.lineChartBtn) // Checks a specific button
        toggleChartGroup.uncheck(R.id.pieChartBtn) // Unchecks a specific button


        toggleChartGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            isLine = (checkedId == R.id.lineChartBtn)
            chartGroupChange(false)
        }

        initialSetup()

        toggleExpenseIncomeGroup.addOnButtonCheckedListener{ group, checkedId, isChecked ->
            isExpense = (checkedId == R.id.ExpenseReportBtn)
            transactionGroupChange(false)
        }


 /*       val arguments = Bundle()
        arguments.putInt("VALUE1", 0)
        arguments.putInt("VALUE2", 100)

        val myFragment: Fragment = Report_IncomeFragment()
        myFragment.arguments = arguments

        val fm = supportFragmentManager

        fm.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
            .replace(R.id.Report_Recycle_Fragment, myFragment)
            .commit()*/


    }
    private fun initialSetup(){
        var arguments = Bundle()
        arguments.putString("Type","expense")
        arguments.putString("Date",reportDate)
        val lineFragment: Fragment = Line_fragment()
        lineFragment.arguments = arguments
        var fm = supportFragmentManager
        fm.beginTransaction()
            .add(R.id.chartFragment, lineFragment)
            .commit()

        arguments = Bundle()
        arguments.putString("Type","expense")
        arguments.putString("Date",reportDate)
        val expenseFragment: Fragment = Report_ExpenseFragment()
        expenseFragment.arguments = arguments
        fm.beginTransaction()
            .add(R.id.Report_Recycle_Fragment, expenseFragment)
            .commit()

    }
    private fun chartGroupChange(isChangeDate : Boolean){
        if(!isChangeDate){
            changeState(isLine)
        }

        if (isLine) {
            var arguments = Bundle()
            if(isExpense){
                arguments.putString("Type","expense")
                arguments.putString("Date",reportDate)
            }else{
                arguments.putString("Type","income")
                arguments.putString("Date",reportDate)
            }

            val lineFragment: Fragment = Line_fragment()
            lineFragment.arguments = arguments
            var fm = supportFragmentManager
            fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                .replace(R.id.chartFragment, lineFragment)
                .commit()

        } else {
            val arguments = Bundle()
            if(isExpense){
                arguments.putString("Type","expense")
                arguments.putString("Date",reportDate)
            }else{
                arguments.putString("Type","income")
                arguments.putString("Date",reportDate)
            }

            val pieFragment: Fragment = Pie_fragment()
            pieFragment.arguments = arguments
            val fm = supportFragmentManager
            fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                .replace(R.id.chartFragment, pieFragment)
                .commit()
        }
    }
    private fun transactionGroupChange(isChangeDate : Boolean){
        if(isExpense){
            ExpenseReportBtn.setTextColor(ContextCompat.getColor(this,R.color.iris))
            IncomeReportBtn.setTextColor(ContextCompat.getColor(this,R.color.black))

            if(!isLine){
                val arguments = Bundle()
                arguments.putString("Type","expense")
                arguments.putString("Date",reportDate)
                val pieFragment: Fragment = Pie_fragment()
                pieFragment.arguments = arguments
                val fm = supportFragmentManager

                fm.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                    .replace(R.id.chartFragment, pieFragment)
                    .commit()
            }else{
                var arguments = Bundle()
                arguments.putString("Type","expense")
                arguments.putString("Date",reportDate)
                val lineFragment: Fragment = Line_fragment()
                lineFragment.arguments = arguments
                var fm = supportFragmentManager

                fm.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                    .replace(R.id.chartFragment, lineFragment)
                    .commit()
            }

            val arguments = Bundle()
            arguments.putString("Date",reportDate)
            val expenseFragment: Fragment = Report_ExpenseFragment()
            expenseFragment.arguments = arguments
            val fm = supportFragmentManager
            fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                .replace(R.id.Report_Recycle_Fragment, expenseFragment)
                .commit()

        }else{
            ExpenseReportBtn.setTextColor(ContextCompat.getColor(this,R.color.black))
            IncomeReportBtn.setTextColor(ContextCompat.getColor(this,R.color.iris))

            val arguments = Bundle()
            arguments.putString("Date",reportDate)
            val incomeFragment: Fragment = Report_IncomeFragment()
            incomeFragment.arguments = arguments
            val fm = supportFragmentManager
            fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                .replace(R.id.Report_Recycle_Fragment, incomeFragment)
                .commit()

            if(!isLine) {
                val arguments = Bundle()
                arguments.putString("Type", "income")
                arguments.putString("Date",reportDate)
                val pieFragment: Fragment = Pie_fragment()
                pieFragment.arguments = arguments
                val fm = supportFragmentManager

                fm.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.chartFragment, pieFragment)
                    .commit()
            }else{
                var arguments = Bundle()
                arguments.putString("Type","income")
                arguments.putString("Date",reportDate)
                val lineFragment: Fragment = Line_fragment()
                lineFragment.arguments = arguments
                var fm = supportFragmentManager

                fm.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                    .replace(R.id.chartFragment, lineFragment)
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
