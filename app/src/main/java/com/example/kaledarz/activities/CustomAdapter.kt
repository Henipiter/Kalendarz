package com.example.kaledarz.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.kaledarz.DTO.Constants
import com.example.kaledarz.DTO.Note
import com.example.kaledarz.DTO.Status
import com.example.kaledarz.R
import com.example.kaledarz.databinding.MyRowBinding

class CustomAdapter(
    var context: Context,
    var noteList: ArrayList<Note>,
    var onClick: (String?) -> Unit
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {


    class MyViewHolder(val binding: MyRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = MyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
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
        val myPref = context.getSharedPreferences("run_alarms", AppCompatActivity.MODE_PRIVATE)
        holder.binding.imageMute.isVisible =
            myPref.getString(Constants.ALARM_ON_OFF, "true") != "true"

        holder.binding.timeStart.text = noteList[position].start_time
        holder.binding.dateStart.text = noteList[position].start_date
        holder.binding.timeEnd.text = noteList[position].end_time
        holder.binding.dateEnd.text = noteList[position].end_date
        holder.binding.content1.text = noteList[position].content?.let { trimDescription(it) }
        getRightStatusImage(holder, noteList[position])
        holder.binding.mainLayout.setOnClickListener {
            onClick.invoke(noteList[position].id)
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    private fun getRightStatusImage(holder: MyViewHolder, note: Note) {
        when (note.status) {
            Status.DONE -> {
                holder.binding.imageDone.setImageResource(R.drawable.image_round_done)
            }

            Status.UNDONE -> {
                holder.binding.imageDone.setImageResource(R.drawable.image_round_undone)
            }

            Status.PAST -> {
                holder.binding.imageDone.setImageResource(R.drawable.image_round_late)
            }

            Status.FUTURE -> {
                holder.binding.imageDone.setImageResource(R.drawable.image_round_future)
            }

            Status.ALL -> {
            }
        }
    }

    companion object {
        private const val EVENT_TITLE_LENGTH = 24
    }

}
