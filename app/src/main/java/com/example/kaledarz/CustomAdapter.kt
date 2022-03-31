package com.example.kaledarz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(

    var activity: Activity,
    var context: Context,
    var _id: ArrayList<String>,
    var _date: ArrayList<String>,
    var _time: ArrayList<String>,
    var _interval: ArrayList<String>,
    var _content: ArrayList<String>

) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    var position = 0

    class MyViewHolder(
        var itemView: View,
        var time: TextView = itemView.findViewById(R.id.time_1),
//        var interval: TextView = itemView.findViewById(R.id.interval_1),
        var content: TextView = itemView.findViewById(R.id.content_1),
        var mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)
    ) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(this.context)
        inflater.inflate(R.layout.my_row, parent, false)
        val view = inflater.inflate(R.layout.my_row, parent, false)
        return MyViewHolder(view)
    }

    private fun trimDescription(description: String): String {
        if (!description.contains("\n")  && description.length <= 16) {
            return  description
        }
        var trimmedDescription = description.split("\n")[0]
        if (trimmedDescription.length > 16) {
            trimmedDescription = trimmedDescription.substring(0, 16)
        }
        return trimmedDescription
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        this.position = position
        holder.time.text = _time[position]
        holder.content.text = trimDescription(_content[position])
        holder.mainLayout.setOnClickListener {
            val i = Intent(context, ShowElemActivity::class.java)
            i.putExtra("id", _id[position])
            activity.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return _id.size
    }
}
