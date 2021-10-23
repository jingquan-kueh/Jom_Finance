package com.example.jom_finance.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jom_finance.R
import com.example.jom_finance.VoiceActivity
import com.example.jom_finance.report.FinancialReportActivity
import kotlinx.android.synthetic.main.fragment_transaction_fragment.*
import kotlinx.android.synthetic.main.fragment_transaction_fragment.view.*

class Transaction_fragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_transaction_fragment, container, false)

        // Inflate the layout for this fragment
        view.toFinancialReportBtn.setOnClickListener{
            requireActivity().run{
                startActivity(Intent(this, FinancialReportActivity::class.java))
                finish()
            }
        }
        return view
    }
}