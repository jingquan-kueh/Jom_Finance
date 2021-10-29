package com.example.jom_finance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val transactionList : ArrayList<Income>) : RecyclerView.Adapter<ListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListAdapter.ListViewHolder, position: Int) {

        val income : Income = transactionList[position]
        holder.title.text = income.Income_name.toString()
        holder.amount.text = income.Income_amount.toString()
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    public class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.transaction_title)
        val amount : TextView = itemView.findViewById(R.id.transaction_amount)
    }
}