package com.example.jom_finance.databinding

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.income.AddNewIncome
import com.example.jom_finance.models.Income

class IncomeListAdapter(
    private val incomeList : ArrayList<Income>,
    private val listener : OnItemClickListener) : RecyclerView.Adapter<IncomeListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val income : Income = incomeList[position]
        holder.title.text = income.incomeName.toString()
        holder.amount.text = income.incomeAmount.toString()
    }

    override fun getItemCount(): Int {
        return incomeList.size
    }

    inner class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView),View.OnClickListener{
        val title : TextView = itemView.findViewById(R.id.transaction_title)
        val amount : TextView = itemView.findViewById(R.id.transaction_amount)
        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
           if(this.adapterPosition != RecyclerView.NO_POSITION){
               listener.onItemClick(this.adapterPosition)
           }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position : Int)
    }
}

