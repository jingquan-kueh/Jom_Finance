package com.example.jom_finance.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jom_finance.R
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.fragment_line_fragment.view.*
import kotlinx.android.synthetic.main.fragment_pie_fragment.view.*

class Pie_fragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_pie_fragment, container, false)
        val p = view.pieChart
        p.setUsePercentValues(true)
        p.description.isEnabled = true
        p.setExtraOffsets(5F,10f,5f,5f)

        p.dragDecelerationFrictionCoef = 0.95f
        p.isDrawHoleEnabled = true
        p.setHoleColor(R.color.white)
        p.transparentCircleRadius = 61f

        var yValues = ArrayList<PieEntry>()
        yValues.add(PieEntry(34F,"Income"))
        yValues.add(PieEntry(13F,"Food"))
        yValues.add(PieEntry(29F,"Sample1"))
        yValues.add(PieEntry(73F,"Sample2"))
        yValues.add(PieEntry(42F,"Sample3"))
        yValues.add(PieEntry(55F,"Sample4"))
        yValues.add(PieEntry(68F,"Sample5"))

        var set1 = PieDataSet(yValues,"Data Set 1")
        set1.sliceSpace = 3f
        set1.selectionShift = 5f

        val arrayList = ColorTemplate.JOYFUL_COLORS.toCollection(ArrayList())

        set1.setColors(arrayList)

        var dataSets = PieData(set1)
        dataSets.setValueTextSize(10f)
        dataSets.setValueTextColor(Color.YELLOW)
        p.data = dataSets

        // Inflate the layout for this fragment
        return view
    }


}