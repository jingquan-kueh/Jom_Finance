package com.example.jom_finance.databinding

import android.graphics.Color.BLUE
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.example.jom_finance.models.Budget
import com.google.android.material.progressindicator.LinearProgressIndicator


class BudgetListAdapter (private val budgetList : ArrayList<Budget>) : RecyclerView.Adapter<BudgetListAdapter.ListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetListAdapter.ListViewHolder{
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_budget,parent,false)
        return ListViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val budget : Budget = budgetList[position]

        holder.category.text = budget.budgetCategory.toString()
        holder.category.compoundDrawables[0].setTint(BLUE)

        val budgetAmount =  budget.budgetAmount!!
        val budgetSpent = budget.budgetSpent!!


        val budgetRemaining = budgetAmount - budgetSpent
        holder.remaining.text = "Remaining RM $budgetRemaining"

        val percentage = (budgetSpent / budgetAmount) * 100
        holder.remainingBar.progress = percentage.toInt()

        holder.remainingBar.setIndicatorColor(BLUE)


        holder.details.text = "RM$budgetSpent of RM$budgetAmount"



        if(!budget.budgetAlert!!){
            holder.exceededImage.visibility = View.GONE
            holder.exceededText.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return budgetList.size
    }

    class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.budgetCategoryList_text)
        val remaining: TextView = itemView.findViewById(R.id.budgetRemainingList_text)
        val remainingBar: LinearProgressIndicator = itemView.findViewById(R.id.budgetRemainingList_bar)
        val details: TextView = itemView.findViewById(R.id.budgetDetailsList_text)

        val exceededImage: ImageView = itemView.findViewById(R.id.budgetExceededList_img)
        val exceededText: TextView = itemView.findViewById(R.id.budgetExceededList_text)
    }

}