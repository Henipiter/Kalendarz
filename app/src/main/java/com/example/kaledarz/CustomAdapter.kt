package com.example.kaledarz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(

    var activity: Activity,
    var context: Context,
    var _id:ArrayList<String>,
    var _date:ArrayList<String>,
    var _time:ArrayList<String>,
    var _interval:ArrayList<String>,
    var _content:ArrayList<String>

) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    var position = 0
    class MyViewHolder(
        var itemView: View,
        var time: TextView = itemView.findViewById(R.id.time_1),
        var interval: TextView = itemView.findViewById(R.id.interval_1),
        var content: TextView = itemView.findViewById(R.id.content_1),
        var mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)


    ) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(this.context)
        inflater.inflate(R.layout.my_row, parent, false)
        val view = inflater.inflate(R.layout.my_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


        this.position = position
var x = _date.get(position).toString()
        holder.time.setText(_time.get(position).toString())
        holder.interval.setText(_interval.get(position).toString())
        holder.content.setText(_content.get(position).toString())
        holder.mainLayout.setOnClickListener(View.OnClickListener() {

            var i = Intent(context, ShowElemActivity::class.java)
            i.putExtra("id", _id.get(position).toString())

            activity.startActivity(i)

        })
    }

    override fun getItemCount(): Int {
        return _id.size
    }




}