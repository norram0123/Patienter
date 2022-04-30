package com.norram.patienter

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.text.DateFormat
import java.util.*

class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(requireActivity(), this, hour, minute, true)
            .also {
                it.setTitle("")
            }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val chooseTime = getString(R.string.choose_time_format).format(hourOfDay, minute)

        val chooseTimeBundle = Bundle()
        chooseTimeBundle.putString(getString(R.string.choose_time), chooseTime)

        setFragmentResult(getString(R.string.request_key2), chooseTimeBundle)
    }
}