package com.norram.patienter

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class DatePickerDialogFragment constructor(_prevCalendar: Calendar, _isChecked: Boolean, _timePickerDialogFragment: TimePickerDialogFragment) : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val prevCalendar: Calendar
    private val isChecked: Boolean
    private val timePickerDialogFragment: TimePickerDialogFragment
    init {
        prevCalendar = _prevCalendar
        isChecked = _isChecked
        timePickerDialogFragment = _timePickerDialogFragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val year = prevCalendar.get(Calendar.YEAR)
        val month = prevCalendar.get(Calendar.MONTH)
        val day = prevCalendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireActivity(), this, year, month, day)
            .also {
                val calendar = Calendar.getInstance()
                it.datePicker.minDate = calendar.timeInMillis
                calendar.set(calendar.get(Calendar.YEAR)+10, 11, 31)
                it.datePicker.maxDate = calendar.timeInMillis
                it.setTitle("")
            }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {

        val chooseDate = getString(R.string.choose_date_format).format(year, month+1, day)

        val chooseDateBundle = Bundle()
        chooseDateBundle.putString(getString(R.string.choose_date), chooseDate)

        setFragmentResult(getString(R.string.request_key), chooseDateBundle)

        if(isChecked) {
            val supportFragmentManager = requireActivity().supportFragmentManager
            timePickerDialogFragment.show(supportFragmentManager, "choose_dialog")
        }
    }
}
