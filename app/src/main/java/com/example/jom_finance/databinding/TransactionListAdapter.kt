package com.example.jom_finance.databinding

import android.graphics.Color
import android.graphics.Color.*
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.income.*
import com.example.jom_finance.models.Category
import com.example.jom_finance.models.Transaction
import com.google.firebase.Timestamp
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TransactionListAdapter(
    private val transactionList: ArrayList<Transaction>,
    private val categoryHash: HashMap<String, Category>,
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<TransactionListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return ListViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val transaction : Transaction = transactionList[position]
        val amount : Double? = transaction.transactionAmount
        val time = transaction.transactionTime
        holder.title.text = transaction.transactionCategory.toString()

        val current = Timestamp.now().toDate()

        if(time!=null) {
            val date = time!!.toDate()
            when {
                isToday(date,current) -> {
                    val sdf = SimpleDateFormat("KK:mm a")
                    val dateString = sdf.format(date)
                    holder.time.text = dateString
                }
                isSameWeek(date,current) -> {
                    // WeekDay : Time
                    val sdf = SimpleDateFormat("EEE KK:mm a")
                    val dateString = sdf.format(date)
                    holder.time.text = dateString
                }
                isSameYear(date,current) -> {
                    val sdf = SimpleDateFormat("dd MMM")
                    val dateString = sdf.format(date)
                    holder.time.text = dateString
                }
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy")
                    val dateString = sdf.format(date)
                    holder.time.text = dateString

                }
            }
        }

        if(transaction.transactionType.equals("income")){
            holder.amount.text = String.format("+ RM %.2f",amount)
            holder.amount.setTextColor(parseColor("#00A86B"))
        }else{
            holder.amount.text = String.format("- RM %.2f",amount)
            holder.amount.setTextColor(RED)
        }
        holder.description.text = transaction.transactionDescription.toString()
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

    private fun isToday(date : Date,current :Date) : Boolean{
        return (date.year == current.year && date.month == current.month && date.date == current.date)
    }

    private fun isSameWeek(date : Date,current :Date) : Boolean{
        return ((kotlin.math.abs(date.date - current.date) < 7) && date.month == current.month)
    }

    private fun isSameYear(date : Date,current :Date) : Boolean{
        return (date.year == current.year)
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

