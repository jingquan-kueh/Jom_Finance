package com.example.jom_finance.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.jom_finance.R


class Home_fragment : Fragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       /* ArrayAdapter.createFromResource(
            activity.getBaseContext(),
            R.array.Month,
            R.layout.spinner_list
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.spinner_list)
            // Apply the adapter to the spinner
            spinnerMonth.adapter = adapter
        }*/

        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.fragment_home_fragment, container, false)
    }


}