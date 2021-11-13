package com.example.jom_finance.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.models.Income

class IncomeListAdapter(private val incomeList : ArrayList<Income>) : RecyclerView.Adapter<IncomeListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val income : Income = incomeList[position]
        holder.title.text = income.incomeName.toString()
        holder.amount.text = income.incomeAmount.toString()
        holder.itemView.setOnClickListener{
            val position = position
        }
    }

    override fun getItemCount(): Int {
        return incomeList.size
    }

    class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.transaction_title)
        val amount : TextView = itemView.findViewById(R.id.transaction_amount)


    }
}