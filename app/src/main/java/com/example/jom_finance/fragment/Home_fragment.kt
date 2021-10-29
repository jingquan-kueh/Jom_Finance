package com.example.jom_finance.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.Income
import com.example.jom_finance.ListAdapter

import com.example.jom_finance.R
import com.google.api.Distribution
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_home_fragment.*
import kotlinx.android.synthetic.main.fragment_home_fragment.view.*
import org.w3c.dom.Document


class Home_fragment : Fragment(){

    private lateinit var fAuth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionArrayList : ArrayList<Income>
    private lateinit var listAdapter: ListAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUpdb()
        val view : View = inflater.inflate(R.layout.fragment_home_fragment, container, false)
        recyclerView = view.home_recyclerView
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.setHasFixedSize(true)
        transactionArrayList = arrayListOf()

        listAdapter = ListAdapter(transactionArrayList)

        recyclerView.adapter = listAdapter
        EventChangeListener()

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
        return  view
    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("incomes/$userID/Income_detail")
            .addSnapshotListener(object : EventListener<QuerySnapshot>{
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if(error!=null){
                        Log.e("FireStore Error",error.message.toString())
                        return
                    }
                    for(dc : DocumentChange in value?.documentChanges!!){
                        if(dc.type == DocumentChange.Type.ADDED){
                                transactionArrayList.add(dc.document.toObject(Income::class.java))

                        }
                    }
                    listAdapter.notifyDataSetChanged()
                }
            })
    }
    private fun setUpdb(){
        fAuth = FirebaseAuth.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }


}