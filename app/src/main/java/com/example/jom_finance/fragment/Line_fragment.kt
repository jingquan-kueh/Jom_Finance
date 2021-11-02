package com.example.jom_finance.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jom_finance.R
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_line_fragment.view.*



class Line_fragment : Fragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_line_fragment, container, false)

        view.lineChart.isDragEnabled = true
        view.lineChart.setScaleEnabled(true)

        var yValues = ArrayList<Entry>()
        yValues.add(Entry(0F,60f))
        yValues.add(Entry(1F,50f))
        yValues.add(Entry(2F,80f))
        yValues.add(Entry(3F,70f))
        yValues.add(Entry(4F,60f))
        yValues.add(Entry(5F,10f))
        yValues.add(Entry(6F,90f))
        var set1 = LineDataSet(yValues,"Data Set 1")
        set1.fillAlpha = 110
        set1.lineWidth =3f

        var dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set1)
        var data = LineData(dataSets)
        view.lineChart.setDrawGridBackground(false)
        view.lineChart.data = data

        // Inflate the layout for this fragment
        return view
    }

}