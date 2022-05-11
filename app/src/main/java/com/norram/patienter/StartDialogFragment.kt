package com.norram.patienter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDateTime

class StartDialogFragment constructor(_title: String, _selectedTime: LocalDateTime) : DialogFragment(){
    private val title: String = _title
    private val selectedTime: LocalDateTime = _selectedTime

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.start_dialog)
                .setPositiveButton(R.string.ok
                ) { _, _ ->
                    if (isFutureTime(selectedTime)) {
                        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                        sharedPref?.let {
                            with(sharedPref.edit()) {
                                putBoolean(getString(R.string.set_flag), true)
                                putString(
                                    getString(R.string.start_time),
                                    LocalDateTime.now().toString().substring(0, 20)
                                )
                                putString(
                                    getString(R.string.goal_time),
                                    selectedTime.toString()
                                )
                                putString(getString(R.string.goal_title), title)
                                apply()
                            }
                            findNavController().navigate(R.id.action_setFragment_to_countFragment)
                        }
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun isFutureTime(selectedTime: LocalDateTime): Boolean {
        if(selectedTime <= LocalDateTime.now()) {
            Toast.makeText(context, R.string.alert2, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}