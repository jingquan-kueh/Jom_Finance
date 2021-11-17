package com.example.jom_finance.databinding

import android.graphics.Color.BLACK
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.models.Budget
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class BudgetListAdapter (private val budgetList : ArrayList<Budget>, private val listener : OnItemClickListener) : RecyclerView.Adapter<BudgetListAdapter.ListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetListAdapter.ListViewHolder{
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_budget,parent,false)
        return ListViewHolder(itemView)

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val budget : Budget = budgetList[position]

        holder.category.text = budget.budgetCategory.toString()

        holder.category.compoundDrawables[0].setTint(budget.budgetColor!!)

        val budgetAmount =  budget.budgetAmount!!
        val budgetSpent = budget.budgetSpent!!
        var budgetRemaining = budgetAmount - budgetSpent
        if (budgetRemaining < 0.0)
            budgetRemaining = 0.0

        val budgetAmountText =  String.format("%.2f", budgetAmount)
        val budgetSpentText = String.format("%.2f", budgetSpent)
        val budgetRemainingText = String.format("%.2f", budgetRemaining)


        holder.remaining.text = "Remaining RM $budgetRemainingText"

        val percentage = (budgetSpent / budgetAmount) * 100
        holder.remainingBar.progress = percentage.toInt()

        holder.remainingBar.setIndicatorColor(budget.budgetColor!!)

        holder.details.text = "RM$budgetSpentText of RM$budgetAmountText"

        if(budgetSpent < budgetAmount){
            holder.exceededImage.visibility = View.GONE
            holder.exceededText.visibility = View.GONE
        }


    }

    override fun getItemCount(): Int {
        return budgetList.size
    }

    inner class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val category: TextView = itemView.findViewById(R.id.budgetCategoryList_text)
        val remaining: TextView = itemView.findViewById(R.id.budgetRemainingList_text)
        val remainingBar: LinearProgressIndicator = itemView.findViewById(R.id.budgetRemainingList_bar)
        val details: TextView = itemView.findViewById(R.id.budgetDetailsList_text)
        val exceededImage: ImageView = itemView.findViewById(R.id.budgetExceededList_img)
        val exceededText: TextView = itemView.findViewById(R.id.budgetExceededList_text)

        init{
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