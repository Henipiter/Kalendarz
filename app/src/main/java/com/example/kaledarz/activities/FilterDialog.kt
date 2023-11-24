package com.example.kaledarz.activities

import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.kaledarz.DTO.DateFilter
import com.example.kaledarz.databinding.DialogFilterBinding
import com.example.kaledarz.helpers.PickerHelper
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterDialog(
    var dateFilter: DateFilter,
    var onStartClick: (DateFilter) -> Unit,


    ) : DialogFragment() {
    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var pickerHelper: PickerHelper


    private var lowerStartCalendar = Calendar.getInstance()
    private var upperStartCalendar = Calendar.getInstance()
    private var lowerEndCalendar = Calendar.getInstance()
    private var upperEndCalendar = Calendar.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWidthPercent(95)
        binding.lowerStartDateInput.setText(dateFilter.lowerStartDate)
        binding.upperStartDateInput.setText(dateFilter.upperStartDate)
        binding.lowerEndDateInput.setText(dateFilter.lowerEndDate)
        binding.upperEndDateInput.setText(dateFilter.upperEndDate)

        pickerHelper = PickerHelper(requireContext())

        binding.lowerStartDateLayout.setStartIconOnClickListener {
            binding.lowerStartDateInput.setText("")
        }
        binding.upperStartDateLayout.setStartIconOnClickListener {
            binding.upperStartDateInput.setText("")
        }
        binding.lowerEndDateLayout.setStartIconOnClickListener {
            binding.lowerEndDateInput.setText("")
        }
        binding.upperEndDateLayout.setStartIconOnClickListener {
            binding.upperEndDateInput.setText("")
        }
        binding.lowerStartDateLayout.setEndIconOnClickListener {
            showLowerStartDatePicker()
        }
        binding.upperStartDateLayout.setEndIconOnClickListener {
            showUpperStartDatePicker()
        }
        binding.lowerEndDateLayout.setEndIconOnClickListener {
            showLowerEndDatePicker()
        }
        binding.upperEndDateLayout.setEndIconOnClickListener {
            showUpperEndDatePicker()
        }
        binding.confirmButton.setOnClickListener {
            val newDateFilter = DateFilter(
                lowerStartDate = binding.lowerStartDateInput.text.toString(),
                upperStartDate = binding.upperStartDateInput.text.toString(),
                lowerEndDate = binding.lowerEndDateInput.text.toString(),
                upperEndDate = binding.upperEndDateInput.text.toString(),
                content = binding.contentInput.text.toString(),
            )
            onStartClick.invoke(newDateFilter)
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

    private fun showLowerStartDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
            .setSelection(lowerStartCalendar.timeInMillis).build()
        datePicker.show(childFragmentManager, "Test")
        datePicker.addOnPositiveButtonClickListener {
            lowerStartCalendar.timeInMillis = it
            val date =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(lowerStartCalendar.time)
            binding.lowerStartDateInput.setText(date)
        }
    }

    private fun showUpperStartDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
            .setSelection(upperStartCalendar.timeInMillis).build()
        datePicker.show(childFragmentManager, "Test")
        datePicker.addOnPositiveButtonClickListener {
            upperStartCalendar.timeInMillis = it
            val date =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(upperStartCalendar.time)
            binding.upperStartDateInput.setText(date)
        }
    }

    private fun showLowerEndDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
            .setSelection(lowerEndCalendar.timeInMillis).build()
        datePicker.show(childFragmentManager, "Test")
        datePicker.addOnPositiveButtonClickListener {
            lowerEndCalendar.timeInMillis = it
            val date =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(lowerEndCalendar.time)
            binding.lowerEndDateInput.setText(date)
        }
    }

    private fun showUpperEndDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date")
            .setSelection(upperEndCalendar.timeInMillis).build()
        datePicker.show(childFragmentManager, "Test")
        datePicker.addOnPositiveButtonClickListener {
            upperEndCalendar.timeInMillis = it
            val date =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(upperEndCalendar.time)
            binding.upperEndDateInput.setText(date)
        }
    }

    fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    /**
     * Call this method (in onActivityCreated or later)
     * to make the dialog near-full screen.
     */
    fun DialogFragment.setFullScreen() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}