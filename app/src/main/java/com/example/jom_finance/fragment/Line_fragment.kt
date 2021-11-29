package com.example.jom_finance.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import com.example.jom_finance.R
import com.example.jom_finance.models.Category
import com.example.jom_finance.models.Transaction
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.fragment_line_fragment.view.*
import kotlinx.android.synthetic.main.fragment_pie_fragment.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Line_fragment : Fragment(){

    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore
    private lateinit var userID : String
    private lateinit var currentUser : FirebaseUser
    private lateinit var transactionArrayList : ArrayList<Transaction>
    private lateinit var categoryArrayList : ArrayList<Category>
    private lateinit var categoryHash : HashMap<String,Int>
    private lateinit var totalHash : HashMap<String,ArrayList<Transaction>>
    private lateinit var type : String
    private lateinit var date : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_line_fragment, container, false)

        val args = arguments
        type = args!!.getString("Type").toString()
        date = args!!.getString("Date").toString()

        val line = view.lineChart
        line.isDragEnabled = true
        line.setScaleEnabled(true)
        line.xAxis.valueFormatter = MyCustomFormatter()

        transactionArrayList = arrayListOf()
        categoryArrayList = arrayListOf()
        categoryHash = hashMapOf()
        totalHash = hashMapOf()
        setupDB()
        if(type!=null){
            readDB(line,type)
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun readDB(line : LineChart,type : String) {
        fStore = FirebaseFirestore.getInstance()
        fStore.collection("transaction/$userID/Transaction_detail").whereEqualTo("Transaction_type",type)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                            var tempTransaciton = dc.document.toObject(Transaction::class.java)
                            var tempTime = tempTransaciton.transactionTime
                            val sdf = SimpleDateFormat("MMMM yyyy")
                            val dateString = sdf.format(tempTime!!.toDate())
                            if(dateString == date){
                                transactionArrayList.add(dc.document.toObject(Transaction::class.java))
                            }
                        }
                    }
                    fStore.collection("category/$userID/category_detail")
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                if(error!=null){
                                    Log.e("FireStore Error",error.message.toString())
                                    return
                                }
                                for(dc : DocumentChange in value?.documentChanges!!){
                                    if(dc.type == DocumentChange.Type.ADDED){
                                        categoryArrayList.add(dc.document.toObject(Category::class.java))
                                        // remove it
                                        totalHash[categoryArrayList.last().categoryName.toString()] = arrayListOf()
                                    }
                                }
                                for(item in transactionArrayList){
                                    var tempArray = arrayListOf<Transaction>()
                                    if(totalHash.containsKey(item.transactionCategory)){
                                        for(i in totalHash[item.transactionCategory.toString()]!!){
                                            tempArray.add(i)
                                        }
                                        tempArray.add(item)
                                        totalHash[item.transactionCategory.toString()] = tempArray
                                    }
                                }

                                var lineHash : HashMap<String,ArrayList<Entry>> = hashMapOf()

                                for(category in totalHash){
                                    var value = category.value
                                    var key = category.key
                                    var yValues : ArrayList<Entry> = arrayListOf()
                                    for(i in value){
                                        var totalAmount = i.transactionAmount!!.toFloat()
                                        var time = i.transactionTime!!.toDate().time.toFloat()
                                        yValues.add(
                                            Entry(
                                                time,
                                                totalAmount
                                            )
                                        )
                                    }
                                    lineHash[key] = yValues
                                }
                                var keyRemove : ArrayList<String> = arrayListOf()
                                for(i in lineHash){
                                    if(i.value.isEmpty()){
                                        keyRemove.add(i.key)
                                    }
                                }
                                if(keyRemove != null){
                                    for(i in keyRemove){
                                        lineHash.remove(i)
                                    }
                                }
                                createLine(lineHash,line)
                            }
                        })
                }
            })

    }
    private fun createLine(lineHash : HashMap<String,ArrayList<Entry>>,line: LineChart) {
        var dataSets = ArrayList<ILineDataSet>()
        for(i in lineHash){
            val rnds = (0..4).random()
            var set1 = LineDataSet(i.value,"${i.key}")
            set1.setColors(ColorTemplate.COLORFUL_COLORS.get(rnds))
            set1.fillAlpha = 110
            set1.lineWidth =3f
            dataSets.add(set1)
        }
        line.xAxis.position = XAxis.XAxisPosition.BOTTOM
        // For loop to loop all category into dataSets
        var data = LineData(dataSets)
        line.setDrawGridBackground(false)
        line.data = data
        line.notifyDataSetChanged()
        line.invalidate()
    }

    private fun setupDB(){
        fAuth = FirebaseAuth.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }

    }
}

class MyCustomFormatter() : IAxisValueFormatter{
    override fun getFormattedValue(value: Float,axis : AxisBase? ): String {
        val dateInMillis = value.toLong()
        val date = Calendar.getInstance().apply{
            timeInMillis = dateInMillis
        }.time
        return SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    }
}