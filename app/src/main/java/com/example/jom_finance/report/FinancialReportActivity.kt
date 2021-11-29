package com.example.jom_finance.report

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import com.example.jom_finance.R
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.income.AddNewIncome
import com.example.jom_finance.models.Budget
import com.example.jom_finance.models.Category
import com.example.jom_finance.models.Transaction
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import com.mlsdev.animatedrv.AnimatedRecyclerView
import kotlinx.android.synthetic.main.activity_financial_report.*
import kotlinx.android.synthetic.main.budget_report.*
import kotlinx.android.synthetic.main.expenses_report.*
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import kotlinx.android.synthetic.main.income_report.*
import java.text.SimpleDateFormat

class FinancialReportActivity : AppCompatActivity() {

    private lateinit var fAuth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryHash : HashMap<String,Category>
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_financial_report)
        val myViewPagerAdapter = MyViewPagerAdapter()
        financial_view_pager.offscreenPageLimit = 4
        financial_view_pager.adapter = myViewPagerAdapter
        categoryArrayList = arrayListOf()
        categoryHash = hashMapOf()
        setUpdb()
        readDB()
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

    fun toReportDetail(view: android.view.View) {
        val intent = Intent(this, ReportActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun readDB() {
        db = FirebaseFirestore.getInstance()
        //get all category
        db.collection("category/$userID/category_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            categoryArrayList.add(dc.document.toObject(Category::class.java))
                            categoryHash[categoryArrayList.last().categoryName.toString()] = categoryArrayList.last()
                        }
                    }
                }
            })

        //get total Income & Expense
        db.collection("transaction").document(userID)
            .get()
            .addOnCompleteListener{
                val income_amount : Double = it.result["Income"].toString().toDouble()
                val expense_amount : Double = it.result["Expense"].toString().toDouble()
                amountIncomeReport.text = String.format("RM %.2f",income_amount)
                amountExpensesReport.text =String.format("RM %.2f",expense_amount)
            }

        //get highest income
        db.collection("transaction/$userID/Transaction_detail")
            .whereEqualTo("Transaction_type","income")
            .orderBy("Transaction_amount", Query.Direction.DESCENDING).limit(1)
            .get()
            .addOnCompleteListener{
                if(it.result.size()==0){
                    cardViewIncome.visibility = View.INVISIBLE
                }
                else{
                    val highestIncome = it.result.toObjects(Transaction::class.java)
                    val highestIncomeAmount = highestIncome[0].transactionAmount
                    val highestIncomeCategory = highestIncome[0].transactionCategory
                    biggestEarnText.text = highestIncomeCategory
                    biggestEarnAmount.text = "RM $highestIncomeAmount"

                    val IconIncomeCategory = categoryHash[highestIncomeCategory]?.categoryIcon
                    val IconIncomeColor = categoryHash[highestIncomeCategory]?.categoryColor
                    val loader = IconPackLoader(report_income_icon.context)
                    val iconPack = createDefaultIconPack(loader)
                    val drawable =
                        IconIncomeCategory?.let { it1 -> iconPack.getIconDrawable(it1, IconDrawableLoader(this)) }
                    report_income_icon.setImageDrawable(drawable)
                    //Change icon to white color
                    report_income_icon.setColorFilter(Color.WHITE)
                    //Color of account
                    if (IconIncomeColor != null) {
                        report_income_icon_bg.setColorFilter(IconIncomeColor)
                    }
                }
            }.addOnFailureListener{

            }

        //get highest expense
        db.collection("transaction/$userID/Transaction_detail")
            .whereEqualTo("Transaction_type","expense")
            .orderBy("Transaction_amount", Query.Direction.DESCENDING).limit(1)
            .get()
            .addOnCompleteListener{
                if(it.result.size()==0){
                    cardViewExpense.visibility = View.INVISIBLE
                }
                else{
                    val highestExpense = it.result.toObjects(Transaction::class.java)
                    val highestExpenseAmount = highestExpense[0].transactionAmount
                    val highestExpenseCategory = highestExpense[0].transactionCategory
                    biggestSpendingText.text = highestExpenseCategory
                    biggestSpendingAmount.text = "RM $highestExpenseAmount"

                    val IconExpenseCategory = categoryHash[highestExpenseCategory]?.categoryIcon
                    val IconExpenseColor = categoryHash[highestExpenseCategory]?.categoryColor
                    val loader = IconPackLoader(report_income_icon.context)
                    val iconPack = createDefaultIconPack(loader)
                    val drawable =
                        IconExpenseCategory?.let { it1 -> iconPack.getIconDrawable(it1, IconDrawableLoader(this)) }
                    report_expense_icon.setImageDrawable(drawable)
                    //Change icon to white color
                    report_expense_icon.setColorFilter(Color.WHITE)
                    //Color of account
                    if (IconExpenseColor != null) {
                        report_expense_icon_bg.setColorFilter(IconExpenseColor)
                    }
                }
            }.addOnFailureListener{

            }
        val current = Timestamp.now().toDate()
        val sdf = SimpleDateFormat("MMMM yyyy")
        val dateString = sdf.format(current)
        // Budget limit
        db.collection("budget/$userID/budget_detail").whereEqualTo("budget_date",dateString)
            .get()
            .addOnCompleteListener{
                if(it.result.size()!=0){
                    val budgetSize = it.result.size()
                    var budgetOverSize = 0
                    val budgetList : HashMap<String,Budget> = hashMapOf()
                    val overBudgetList : HashMap<String,Budget> = hashMapOf()
                    for(index in it.result){
                        var currentBudget = index.toObject(Budget::class.java)
                        budgetList[currentBudget.budgetCategory.toString()] = currentBudget
                    }
                    for(i in budgetList){
                        if(i.value.budgetSpent!!.toDouble() > i.value.budgetAmount!!.toDouble()){
                            overBudgetList[i.key] = i.value
                        }
                    }
                    budgetOverSize = overBudgetList.size

                    budgetTxt.text = "$budgetOverSize of $budgetSize Budget is exceeds the limit"
                    for(i in overBudgetList){
                        var textBudget = overBudgetTxt.text
                        overBudgetTxt.text = "$textBudget${i.key}\n"
                    }

                }
            }

    }
    private fun setUpdb(){
        fAuth = FirebaseAuth.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }
}