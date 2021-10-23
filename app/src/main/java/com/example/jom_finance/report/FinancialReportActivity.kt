package com.example.jom_finance.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.example.jom_finance.R
import kotlinx.android.synthetic.main.activity_financial_report.*

class FinancialReportActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)
        setContentView(R.layout.activity_financial_report)
        val myViewPagerAdapter = MyViewPagerAdapter()
        financial_view_pager.adapter = myViewPagerAdapter

    }

    class MyViewPagerAdapter : PagerAdapter() {
        private lateinit var layoutInflater: LayoutInflater
        private var layouts = intArrayOf(
            R.layout.expenses_report,
            R.layout.income_report,
            R.layout.budget_report,
            R.layout.quote_report)
        fun MyViewPagerAdapter() {}

        override fun instantiateItem(container: ViewGroup, position: Int): Any =
            LayoutInflater.from(container.context).inflate(layouts[position], container, false).also {
                container.addView(it)
            }

        override fun getCount(): Int {
            return layouts.count()
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view: View = `object` as View
            container.removeView(view)
        }
    }
}