package com.example.kaledarz.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R

class CustomAdapter(

    var activity: Activity,
    var context: Context,
    var noteList: ArrayList<Note>
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {
    var position = 0

    private lateinit var myPref: SharedPreferences

    class MyViewHolder(
        var itemView: View,
        var time_start: TextView = itemView.findViewById(R.id.time_start),
        var date_start: TextView = itemView.findViewById(R.id.date_start),
        var time_end: TextView = itemView.findViewById(R.id.time_end),
        var date_end: TextView = itemView.findViewById(R.id.date_end),
        var content: TextView = itemView.findViewById(R.id.content_1),
        var mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout),
        var imageDone: ImageView = itemView.findViewById(R.id.imageDone),
        var imageMute: ImageView = itemView.findViewById(R.id.imageMute)
    ) : RecyclerView.ViewHolder(itemView)

    companion object {
        private const val EVENT_TITLE_LENGTH = 24
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(this.context)
        inflater.inflate(R.layout.my_row, parent, false)
        val view = inflater.inflate(R.layout.my_row, parent, false)
        return MyViewHolder(view)
    }

    private fun trimDescription(description: String): String {
        if (!description.contains("\n") && description.length <= EVENT_TITLE_LENGTH) {
            return description
        }
        var trimmedDescription = description.split("\n")[0]
        if (trimmedDescription.length > EVENT_TITLE_LENGTH) {
            trimmedDescription = trimmedDescription.substring(0, EVENT_TITLE_LENGTH)
        }
        return "$trimmedDescription..."
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        myPref = context.getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)

        holder.imageMute.isVisible = myPref.getString(Constants.ALARM_ON_OFF, "false") != "true"
        this.position = position

        holder.time_start.text = noteList[position].start_time
        holder.date_start.text = noteList[position].start_date
        holder.time_end.text = noteList[position].end_time
        holder.date_end.text = noteList[position].end_date
        holder.content.text = noteList[position].content?.let { trimDescription(it) }
        getRightStatusImage(holder, noteList[position])
        holder.mainLayout.setOnClickListener {
            val intent = Intent(context, ShowElemActivity::class.java)
            intent.putExtra("type", "EDIT")
            intent.putExtra("id", noteList[position].id)
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    private fun getRightStatusImage(holder: MyViewHolder, note: Note) {
        when (note.status) {
            Status.DONE -> {
                holder.imageDone.setImageResource(R.drawable.image_round_done)
            }
            Status.UNDONE -> {
                holder.imageDone.setImageResource(R.drawable.image_round_undone)
            }
            Status.PAST -> {
                holder.imageDone.setImageResource(R.drawable.image_round_late)
            }
            Status.FUTURE -> {
                holder.imageDone.setImageResource(R.drawable.image_round_future)
            }
            Status.ALL -> {
            }
        }
    }
}
