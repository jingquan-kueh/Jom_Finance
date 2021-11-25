package com.example.jom_finance.databinding

import com.example.jom_finance.models.Category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jom_finance.R
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack

class CategoryListAdapter(private val categoryList : ArrayList<Category>, private val listener : OnItemClickListener) : RecyclerView.Adapter<CategoryListAdapter.ListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category,parent,false)
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val category : Category = categoryList[position]
        holder.name.text = category.categoryName.toString()

        //Icon
        val loader = IconPackLoader(holder.icon.context)
        val iconPack = createDefaultIconPack(loader)
        val drawable = iconPack.getIconDrawable(category.categoryIcon!!, IconDrawableLoader(holder.icon.context))
        holder.icon.setImageDrawable(drawable)

        //Icon color
        holder.icon.setColorFilter(category.categoryColor!!)


    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class ListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name : TextView = itemView.findViewById(R.id.categoryNameList_text)
        val icon : ImageView = itemView.findViewById(R.id.categoryIconList_img)

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