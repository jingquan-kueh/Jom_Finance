package com.example.jom_finance.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color.parseColor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.ExpenseDetailActivity
import com.example.jom_finance.R
import com.example.jom_finance.models.Transaction
import com.example.jom_finance.databinding.TransactionListAdapter
import com.example.jom_finance.income.DetailIncome
import com.example.jom_finance.models.Category
import com.example.jom_finance.report.FinancialReportActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.mlsdev.animatedrv.AnimatedRecyclerView
import kotlinx.android.synthetic.main.bottomsheet_delete.*
import kotlinx.android.synthetic.main.bottomsheet_filter.*
import kotlinx.android.synthetic.main.bottomsheet_filter.view.*
import kotlinx.android.synthetic.main.fragment_home_fragment.view.*
import kotlinx.android.synthetic.main.fragment_transaction_fragment.view.*
import java.text.SimpleDateFormat

class Transaction_fragment : Fragment(), TransactionListAdapter.OnItemClickListener {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var recyclerView: AnimatedRecyclerView
    private lateinit var transactionArrayList: ArrayList<Transaction>
    private lateinit var tempTransactionArrayList: ArrayList<Transaction>
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var categoryHash: HashMap<String, Category>
    private lateinit var categoryCheckedHash: HashMap<String, Boolean>
    private lateinit var transactionListAdapter: TransactionListAdapter
    private lateinit var db: FirebaseFirestore

    private var isIncome = false
    private var isExpense = false
    private var isHighest = false
    private var isLowest = false
    private var isNewest = false
    private var isOldest = false

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_transaction_fragment, container, false)

        // Inflate the layout for this fragment
        view.toFinancialReportBtn.setOnClickListener {
            requireActivity().run {
                startActivity(Intent(this, FinancialReportActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        view.filterBtn.setOnClickListener {
            openAttachmentBottomSheetDialog(it)
        }
        setUpdb()
        recyclerView = view.transaction_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        transactionArrayList = arrayListOf()
        categoryArrayList = arrayListOf()
        categoryHash = hashMapOf()
        categoryCheckedHash = hashMapOf()
        transactionListAdapter = TransactionListAdapter(transactionArrayList, categoryHash, this)

        recyclerView.adapter = transactionListAdapter
        EventChangeListener()

        // Inflate the layout for this fragment
        return view
    }

    private fun reset() {
        isIncome = false
        isExpense = false
        isHighest = false
        isLowest = false
        isOldest = false
        isNewest = false
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")

    private fun openAttachmentBottomSheetDialog(view: View) {
        val bottomSheet = BottomSheetDialog(view.context)
        bottomSheet.setContentView(R.layout.bottomsheet_filter)
        val incomeFilter = bottomSheet.findViewById<Button>(R.id.filter_income) as Button
        val expenseFilter = bottomSheet.findViewById<Button>(R.id.filter_expense) as Button
        val highestFilter = bottomSheet.findViewById<Button>(R.id.filter_highest) as Button
        val lowestFilter = bottomSheet.findViewById<Button>(R.id.filter_lowest) as Button
        val newestFilter = bottomSheet.findViewById<Button>(R.id.filter_newest) as Button
        val oldestFilter = bottomSheet.findViewById<Button>(R.id.filter_oldest) as Button
        val applyBtn = bottomSheet.findViewById<Button>(R.id.applyFilterBtn) as Button
        val resetBtn = bottomSheet.findViewById<Button>(R.id.filter_reset) as Button

        setCategoryChips(categoryArrayList, bottomSheet)

        reset()
        resetBtn.setOnClickListener {
            reset()
            incomeFilter.setTextColor(R.color.black)
            incomeFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            expenseFilter.setTextColor(R.color.black)
            expenseFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            highestFilter.setTextColor(R.color.black)
            highestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            lowestFilter.setTextColor(R.color.black)
            lowestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            newestFilter.setTextColor(R.color.black)
            newestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            oldestFilter.setTextColor(R.color.black)
            oldestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))

        }
        incomeFilter.setOnClickListener {
            if (!isIncome) {
                incomeFilter.setTextColor(R.color.iris)
                incomeFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#EEE5FF"))
                isExpense = false
            } else {
                incomeFilter.setTextColor(R.color.black)
                incomeFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            }
            isIncome = !isIncome
            expenseFilter.setTextColor(R.color.black)
            expenseFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
        }

        expenseFilter.setOnClickListener {
            if (!isExpense) {
                isIncome = false
                expenseFilter.setTextColor(R.color.iris)
                expenseFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#EEE5FF"))
            } else {
                expenseFilter.setTextColor(R.color.black)
                expenseFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            }
            isExpense = !isExpense
            incomeFilter.setTextColor(R.color.black)
            incomeFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
        }

        highestFilter.setOnClickListener {
            if (!isHighest) {
                isLowest = false
                isOldest = false
                isNewest = false
                highestFilter.setTextColor(R.color.iris)
                highestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#EEE5FF"))
            } else {
                highestFilter.setTextColor(R.color.black)
                highestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            }
            isHighest = !isHighest
            lowestFilter.setTextColor(R.color.black)
            lowestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            newestFilter.setTextColor(R.color.black)
            newestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            oldestFilter.setTextColor(R.color.black)
            oldestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
        }

        lowestFilter.setOnClickListener {
            if (!isLowest) {
                isHighest = false
                isOldest = false
                isNewest = false
                lowestFilter.setTextColor(R.color.iris)
                lowestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#EEE5FF"))
            } else {
                lowestFilter.setTextColor(R.color.black)
                lowestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            }
            isLowest = !isLowest

            highestFilter.setTextColor(R.color.black)
            highestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            newestFilter.setTextColor(R.color.black)
            newestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            oldestFilter.setTextColor(R.color.black)
            oldestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
        }
        newestFilter.setOnClickListener {
            if (!isNewest) {
                isHighest = false
                isOldest = false
                isLowest = false
                newestFilter.setTextColor(R.color.iris)
                newestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#EEE5FF"))
            } else {
                newestFilter.setTextColor(R.color.black)
                newestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            }
            isNewest = !isNewest

            lowestFilter.setTextColor(R.color.black)
            lowestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            highestFilter.setTextColor(R.color.black)
            highestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            oldestFilter.setTextColor(R.color.black)
            oldestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
        }
        oldestFilter.setOnClickListener {
            if (!isOldest) {
                isHighest = false
                isNewest = false
                isLowest = false
                oldestFilter.setTextColor(R.color.iris)
                oldestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#EEE5FF"))

            } else {
                oldestFilter.setTextColor(R.color.black)
                oldestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            }
            isOldest = !isOldest

            lowestFilter.setTextColor(R.color.black)
            lowestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            newestFilter.setTextColor(R.color.black)
            newestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
            highestFilter.setTextColor(R.color.black)
            highestFilter.backgroundTintList = ColorStateList.valueOf(parseColor("#FFFFFF"))
        }

        applyBtn.setOnClickListener {
            db = FirebaseFirestore.getInstance()

            var isEmptyHash = true

            for (i in categoryCheckedHash) {
                if (i.value) {
                    isEmptyHash = !i.value
                }
            }
            if (isEmptyHash) {
                if (isIncome) {
                    if (!(isHighest || isLowest || isNewest || isOldest)) {
                        transactionArrayList.clear()
                        db.collection("transaction/$userID/Transaction_detail")
                            .whereEqualTo("Transaction_type", "income")
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                }
                            })
                    } else {
                        when {
                            isHighest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "income")
                                    .orderBy("Transaction_amount", Query.Direction.DESCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                            isLowest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "income")
                                    .orderBy("Transaction_amount", Query.Direction.ASCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                            isNewest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "income")
                                    .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                            isOldest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "income")
                                    .orderBy("Transaction_timestamp", Query.Direction.ASCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                        }
                    }
                } else if (isExpense) {
                    if (!(isHighest || isLowest || isNewest || isOldest)) {
                        transactionArrayList.clear()
                        db.collection("transaction/$userID/Transaction_detail")
                            .whereEqualTo("Transaction_type", "expense")
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                }
                            })
                    } else {
                        when {
                            isHighest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "expense")
                                    .orderBy("Transaction_amount", Query.Direction.DESCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                            isLowest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "expense")
                                    .orderBy("Transaction_amount", Query.Direction.ASCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                            isNewest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "expense")
                                    .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                            isOldest -> {
                                transactionArrayList.clear()
                                db.collection("transaction/$userID/Transaction_detail")
                                    .whereEqualTo("Transaction_type", "expense")
                                    .orderBy("Transaction_timestamp", Query.Direction.ASCENDING)
                                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                        override fun onEvent(
                                            value: QuerySnapshot?,
                                            error: FirebaseFirestoreException?,
                                        ) {
                                            if (error != null) {
                                                Log.e("FireStore Error", error.message.toString())
                                                return
                                            }
                                            for (dc: DocumentChange in value?.documentChanges!!) {
                                                if (dc.type == DocumentChange.Type.ADDED) {
                                                    transactionArrayList.add(dc.document.toObject(
                                                        Transaction::class.java))
                                                }
                                            }
                                            transactionListAdapter.notifyDataSetChanged()
                                            recyclerView.scheduleLayoutAnimation()
                                        }
                                    })
                            }
                        }
                    }
                } else {
                    when {
                        isHighest -> {
                            transactionArrayList.clear()
                            db.collection("transaction/$userID/Transaction_detail")
                                .orderBy("Transaction_amount", Query.Direction.DESCENDING)
                                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                    override fun onEvent(
                                        value: QuerySnapshot?,
                                        error: FirebaseFirestoreException?,
                                    ) {
                                        if (error != null) {
                                            Log.e("FireStore Error", error.message.toString())
                                            return
                                        }
                                        for (dc: DocumentChange in value?.documentChanges!!) {
                                            if (dc.type == DocumentChange.Type.ADDED) {
                                                transactionArrayList.add(dc.document.toObject(
                                                    Transaction::class.java))
                                            }
                                        }
                                        transactionListAdapter.notifyDataSetChanged()
                                        recyclerView.scheduleLayoutAnimation()
                                    }
                                })
                        }
                        isLowest -> {
                            transactionArrayList.clear()
                            db.collection("transaction/$userID/Transaction_detail")
                                .orderBy("Transaction_amount", Query.Direction.ASCENDING)
                                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                    override fun onEvent(
                                        value: QuerySnapshot?,
                                        error: FirebaseFirestoreException?,
                                    ) {
                                        if (error != null) {
                                            Log.e("FireStore Error", error.message.toString())
                                            return
                                        }
                                        for (dc: DocumentChange in value?.documentChanges!!) {
                                            if (dc.type == DocumentChange.Type.ADDED) {
                                                transactionArrayList.add(dc.document.toObject(
                                                    Transaction::class.java))
                                            }
                                        }
                                        transactionListAdapter.notifyDataSetChanged()
                                        recyclerView.scheduleLayoutAnimation()
                                    }
                                })
                        }
                        isNewest -> {
                            transactionArrayList.clear()
                            db.collection("transaction/$userID/Transaction_detail")
                                .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
                                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                    override fun onEvent(
                                        value: QuerySnapshot?,
                                        error: FirebaseFirestoreException?,
                                    ) {
                                        if (error != null) {
                                            Log.e("FireStore Error", error.message.toString())
                                            return
                                        }
                                        for (dc: DocumentChange in value?.documentChanges!!) {
                                            if (dc.type == DocumentChange.Type.ADDED) {
                                                transactionArrayList.add(dc.document.toObject(
                                                    Transaction::class.java))
                                            }
                                        }
                                        transactionListAdapter.notifyDataSetChanged()
                                        recyclerView.scheduleLayoutAnimation()
                                    }
                                })
                        }
                        isOldest -> {
                            transactionArrayList.clear()
                            db.collection("transaction/$userID/Transaction_detail")
                                .orderBy("Transaction_timestamp", Query.Direction.ASCENDING)
                                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                    override fun onEvent(
                                        value: QuerySnapshot?,
                                        error: FirebaseFirestoreException?,
                                    ) {
                                        if (error != null) {
                                            Log.e("FireStore Error", error.message.toString())
                                            return
                                        }
                                        for (dc: DocumentChange in value?.documentChanges!!) {
                                            if (dc.type == DocumentChange.Type.ADDED) {
                                                transactionArrayList.add(dc.document.toObject(
                                                    Transaction::class.java))
                                            }
                                        }
                                        transactionListAdapter.notifyDataSetChanged()
                                        recyclerView.scheduleLayoutAnimation()
                                    }
                                })
                        }
                        else -> {
                            transactionArrayList.clear()
                            db.collection("transaction/$userID/Transaction_detail")
                                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                    override fun onEvent(
                                        value: QuerySnapshot?,
                                        error: FirebaseFirestoreException?,
                                    ) {
                                        if (error != null) {
                                            Log.e("FireStore Error", error.message.toString())
                                            return
                                        }
                                        for (dc: DocumentChange in value?.documentChanges!!) {
                                            if (dc.type == DocumentChange.Type.ADDED) {
                                                transactionArrayList.add(dc.document.toObject(
                                                    Transaction::class.java))
                                            }
                                        }
                                        transactionListAdapter.notifyDataSetChanged()
                                        recyclerView.scheduleLayoutAnimation()
                                    }
                                })

                        }

                    }

                }
            } else {
                categorySelectionFilter(db)
            }
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun categorySelectionFilter(db: FirebaseFirestore) {
        var path = db.collection("transaction/$userID/Transaction_detail")
        var query: Query = path
        var listCat = arrayListOf<String>()
        listCat.clear()
        Log.d("selectionFilter", "IN")

        for (i in categoryCheckedHash) {
            if (i.value) {
                listCat.add(i.key)
                Log.d("listCat", listCat.last())
            }
        }

        if (isIncome) {
            Log.d("selectionFilter", "IN income")
            if (!(isHighest || isLowest || isNewest || isOldest)) {
                transactionArrayList.clear()
                query.whereIn("Transaction_category", listCat)
                    .whereEqualTo("Transaction_type", "income")
                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                        override fun onEvent(
                            value: QuerySnapshot?,
                            error: FirebaseFirestoreException?,
                        ) {
                            if (error != null) {
                                Log.e("FireStore Error", error.message.toString())
                                return
                            }
                            for (dc: DocumentChange in value?.documentChanges!!) {
                                if (dc.type == DocumentChange.Type.ADDED) {
                                    transactionArrayList.add(dc.document.toObject(Transaction::class.java))
                                }
                            }
                            transactionListAdapter.notifyDataSetChanged()
                            recyclerView.scheduleLayoutAnimation()
                            categoryCheckedHash.clear()
                        }
                    })
            } else {
                when {
                    isHighest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "income")
                            .orderBy("Transaction_amount", Query.Direction.DESCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                    isLowest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "income")
                            .orderBy("Transaction_amount", Query.Direction.ASCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                    isNewest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "income")
                            .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                    isOldest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "income")
                            .orderBy("Transaction_timestamp", Query.Direction.ASCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                }
            }
        } else if (isExpense) {
            if (!(isHighest || isLowest || isNewest || isOldest)) {
                transactionArrayList.clear()
                query.whereIn("Transaction_category", listCat)
                    .whereEqualTo("Transaction_type", "expense")
                    .addSnapshotListener(object : EventListener<QuerySnapshot> {
                        override fun onEvent(
                            value: QuerySnapshot?,
                            error: FirebaseFirestoreException?,
                        ) {
                            if (error != null) {
                                Log.e("FireStore Error", error.message.toString())
                                return
                            }
                            for (dc: DocumentChange in value?.documentChanges!!) {
                                if (dc.type == DocumentChange.Type.ADDED) {
                                    transactionArrayList.add(dc.document.toObject(Transaction::class.java))
                                }
                            }
                            transactionListAdapter.notifyDataSetChanged()
                            recyclerView.scheduleLayoutAnimation()
                            categoryCheckedHash.clear()
                        }
                    })
            } else {
                when {
                    isHighest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "expense")
                            .orderBy("Transaction_amount", Query.Direction.DESCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                    isLowest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "expense")
                            .orderBy("Transaction_amount", Query.Direction.ASCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                    isNewest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "expense")
                            .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                    isOldest -> {
                        transactionArrayList.clear()
                        query.whereIn("Transaction_category", listCat)
                            .whereEqualTo("Transaction_type", "expense")
                            .orderBy("Transaction_timestamp", Query.Direction.ASCENDING)
                            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                                override fun onEvent(
                                    value: QuerySnapshot?,
                                    error: FirebaseFirestoreException?,
                                ) {
                                    if (error != null) {
                                        Log.e("FireStore Error", error.message.toString())
                                        return
                                    }
                                    for (dc: DocumentChange in value?.documentChanges!!) {
                                        if (dc.type == DocumentChange.Type.ADDED) {
                                            transactionArrayList.add(dc.document.toObject(
                                                Transaction::class.java))
                                        }
                                    }
                                    transactionListAdapter.notifyDataSetChanged()
                                    recyclerView.scheduleLayoutAnimation()
                                    categoryCheckedHash.clear()
                                }
                            })
                    }
                }
            }
        } else {
            when {
                isHighest -> {
                    transactionArrayList.clear()
                    query.whereIn("Transaction_category", listCat)
                        .orderBy("Transaction_amount", Query.Direction.DESCENDING)
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(
                                value: QuerySnapshot?,
                                error: FirebaseFirestoreException?,
                            ) {
                                if (error != null) {
                                    Log.e("FireStore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.ADDED) {
                                        transactionArrayList.add(dc.document.toObject(
                                            Transaction::class.java))
                                    }
                                }
                                transactionListAdapter.notifyDataSetChanged()
                                recyclerView.scheduleLayoutAnimation()
                                categoryCheckedHash.clear()
                            }
                        })
                }
                isLowest -> {
                    transactionArrayList.clear()
                    query.whereIn("Transaction_category", listCat)
                        .orderBy("Transaction_amount", Query.Direction.ASCENDING)
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(
                                value: QuerySnapshot?,
                                error: FirebaseFirestoreException?,
                            ) {
                                if (error != null) {
                                    Log.e("FireStore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.ADDED) {
                                        transactionArrayList.add(dc.document.toObject(
                                            Transaction::class.java))
                                    }
                                }
                                transactionListAdapter.notifyDataSetChanged()
                                recyclerView.scheduleLayoutAnimation()
                                categoryCheckedHash.clear()
                            }
                        })
                }
                isNewest -> {
                    transactionArrayList.clear()
                    query.whereIn("Transaction_category", listCat)
                        .orderBy("Transaction_timestamp", Query.Direction.DESCENDING)
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(
                                value: QuerySnapshot?,
                                error: FirebaseFirestoreException?,
                            ) {
                                if (error != null) {
                                    Log.e("FireStore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.ADDED) {
                                        transactionArrayList.add(dc.document.toObject(
                                            Transaction::class.java))
                                    }
                                }
                                transactionListAdapter.notifyDataSetChanged()
                                recyclerView.scheduleLayoutAnimation()
                                categoryCheckedHash.clear()
                            }
                        })
                }
                isOldest -> {
                    transactionArrayList.clear()
                    query.whereIn("Transaction_category", listCat)
                        .orderBy("Transaction_timestamp", Query.Direction.ASCENDING)
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(
                                value: QuerySnapshot?,
                                error: FirebaseFirestoreException?,
                            ) {
                                if (error != null) {
                                    Log.e("FireStore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.ADDED) {
                                        transactionArrayList.add(dc.document.toObject(
                                            Transaction::class.java))
                                    }
                                }
                                transactionListAdapter.notifyDataSetChanged()
                                recyclerView.scheduleLayoutAnimation()
                                categoryCheckedHash.clear()
                            }
                        })
                }
                else -> {
                    transactionArrayList.clear()
                    query.whereIn("Transaction_category", listCat)
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(
                                value: QuerySnapshot?,
                                error: FirebaseFirestoreException?,
                            ) {
                                if (error != null) {
                                    Log.e("FireStore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.ADDED) {
                                        transactionArrayList.add(dc.document.toObject(
                                            Transaction::class.java))
                                    }
                                }
                                transactionListAdapter.notifyDataSetChanged()
                                recyclerView.scheduleLayoutAnimation()
                                categoryCheckedHash.clear()
                            }
                        })

                }
            }

        }
    }

    fun setCategoryChips(categorys: ArrayList<Category>, bottomSheet: BottomSheetDialog) {
        for (category in categorys) {
            val mChip =
                this.layoutInflater.inflate(R.layout.item_chip_category, null, false) as Chip
            mChip.text = category.categoryName
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f,
                resources.displayMetrics
            ).toInt()
            mChip.setPadding(paddingDp, 0, paddingDp, 0)
            mChip.setOnCheckedChangeListener { compoundButton, b ->
                categoryCheckedHash[compoundButton.text.toString()] = b
            }
            val chips_group = bottomSheet.findViewById<ChipGroup>(R.id.chips_group) as ChipGroup
            chips_group.addView(mChip)
        }
    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("transaction/$userID/Transaction_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("FireStore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            transactionArrayList.add(dc.document.toObject(Transaction::class.java))
                        }
                    }
                }
            })

        db.collection("category/$userID/category_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("FireStore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            categoryArrayList.add(dc.document.toObject(Category::class.java))
                            categoryHash[categoryArrayList.last().categoryName.toString()] =
                                categoryArrayList.last()
                        }
                    }
                    transactionListAdapter.notifyDataSetChanged()
                    recyclerView.scheduleLayoutAnimation()
                }
            })

    }

    private fun setUpdb() {
        fAuth = FirebaseAuth.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }

    override fun onItemClick(position: Int) {
        val item = transactionArrayList[position]
        requireActivity().run {
            val type = item.transactionType
            val dateFormatName = SimpleDateFormat("dd MMMM yyyy")
            val dateFormatNum = SimpleDateFormat("dd-MM-yyyy")
            val timeFormat = SimpleDateFormat("hh:mm")
            val date = item.transactionTime?.toDate()
            if(type == "income"){
                val intent = Intent(this, DetailIncome::class.java)
                intent.putExtra("transactionName",item.transactionName)
                intent.putExtra("transactionName",item.transactionName)
                intent.putExtra("transactionAmount",item.transactionAmount)
                intent.putExtra("transactionCategory",item.transactionCategory)
                intent.putExtra("transactionAccount",item.transactionAccount)
                intent.putExtra("transactionDescription",item.transactionDescription)
                intent.putExtra("transactionAttachment",item.transactionAttachment)
                intent.putExtra("transactionDateName", dateFormatName.format(date))
                intent.putExtra("transactionDateNum", dateFormatNum.format(date))
                intent.putExtra("transactionTime", timeFormat.format(date))
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }else{
                val intent = Intent(this,ExpenseDetailActivity::class.java)
                intent.putExtra("transactionName",item.transactionName)
                intent.putExtra("transactionAmount",item.transactionAmount)
                intent.putExtra("transactionCategory",item.transactionCategory)
                intent.putExtra("transactionAccount",item.transactionAccount)
                intent.putExtra("transactionDescription",item.transactionDescription)
                intent.putExtra("transactionAttachment",item.transactionAttachment)
                intent.putExtra("transactionDateName", dateFormatName.format(date))
                intent.putExtra("transactionDateNum", dateFormatNum.format(date))
                intent.putExtra("transactionTime", timeFormat.format(date))
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

}