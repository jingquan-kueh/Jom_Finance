package com.example.jom_finance.databinding

import android.graphics.Color
import android.graphics.Color.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.models.Category
import com.example.jom_finance.models.Transaction
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack

class TransactionListAdapter(
    private val transactionList : ArrayList<Transaction>,
    private val categoryHash : HashMap<String,Category>,
    private val listener : OnItemClickListener) : RecyclerView.Adapter<TransactionListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val transaction : Transaction = transactionList[position]
        val amount : Double? = transaction.transactionAmount
        holder.title.text = transaction.transactionCategory.toString()

        if(transaction.transactionType.equals("income")){
            holder.amount.text = String.format("+ RM %.2f",amount)
            holder.amount.setTextColor(parseColor("#00A86B"))
        }else{
            holder.amount.text = String.format("- RM %.2f",amount)
            holder.amount.setTextColor(RED)
        }
        //holder.description.text = transaction.transactionDescription.toString()
        val IconCategory = categoryHash[transaction.transactionCategory]?.categoryIcon
        val IconColor = categoryHash[transaction.transactionCategory]?.categoryColor
        //Icon
        val loader = IconPackLoader(holder.icon.context)
        val iconPack = createDefaultIconPack(loader)
        val drawable = IconCategory?.let { iconPack.getIconDrawable(it, IconDrawableLoader(holder.icon.context)) }
        holder.icon.setImageDrawable(drawable)
        //Change icon to white color
        holder.icon.setColorFilter(WHITE)

        //Color of account
        if (IconColor != null) {
            holder.iconBg.setColorFilter(IconColor)
        }


        // Set Amount Text Color
        //holder.amount.setTextColor(Color.parseColor("#00A86B"))
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    inner class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView),View.OnClickListener{
        val title : TextView = itemView.findViewById(R.id.transaction_title)
        val amount : TextView = itemView.findViewById(R.id.transaction_amount)
        val description : TextView = itemView.findViewById(R.id.transaction_description)
        val icon : ImageView = itemView.findViewById(R.id.icon)
        val iconBg : ImageView = itemView.findViewById(R.id.icon_bg)
        val time : TextView = itemView.findViewById(R.id.transaction_time)
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

