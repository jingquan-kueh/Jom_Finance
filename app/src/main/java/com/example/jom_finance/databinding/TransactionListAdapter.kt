package com.example.jom_finance.databinding

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.models.Transaction

class TransactionListAdapter(
    private val transactionList : ArrayList<Transaction>,
    private val listener : OnItemClickListener) : RecyclerView.Adapter<TransactionListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val transaction : Transaction = transactionList[position]
        holder.title.text = transaction.transactionName.toString()
        holder.amount.text = transaction.transactionAmount.toString()

        // Set Amount Text Color
        holder.amount.setTextColor(Color.parseColor("#00A86B"))
    }

    override fun getItemCount(): Int {
        return transactionList.size
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

