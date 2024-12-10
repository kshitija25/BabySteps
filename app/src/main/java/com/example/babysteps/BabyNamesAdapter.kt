package com.example.babysteps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BabyNamesAdapter(private val namesList: List<String>) :
    RecyclerView.Adapter<BabyNamesAdapter.BabyNameViewHolder>() {

    class BabyNameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BabyNameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_baby_name, parent, false)
        return BabyNameViewHolder(view)
    }

    override fun onBindViewHolder(holder: BabyNameViewHolder, position: Int) {
        holder.nameTextView.text = namesList[position]
    }

    override fun getItemCount(): Int {
        return namesList.size
    }
}
