package com.android.norram.patienter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class RetireDialogFragment constructor(_timer: Timer) : DialogFragment() {
    private val timer: Timer
    init {
        timer = _timer
    }

    private var helper: AchieveOpenHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.retire_dialog)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        if(helper == null) {
                            helper = AchieveOpenHelper(requireContext())
                        }
                        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                        val defaultValue = resources.getString(R.string.default_goal_time)
                        val startTime = LocalDateTime.parse(
                            sharedPref?.getString(getString(R.string.start_time), defaultValue))
                        val goalTime = LocalDateTime.parse(
                            sharedPref?.getString(getString(R.string.goal_time), defaultValue))

                        val hours = ChronoUnit.HOURS.between(startTime.toLocalTime(), LocalDateTime.now()).toInt()
                        val message = getString(R.string.giveup)

                        val period = "${startTime.year}/${startTime.monthValue}/${startTime.dayOfMonth}" + " ～ " +
                                "${goalTime.year}/${goalTime.monthValue}/${goalTime.dayOfMonth}"
                        val title = sharedPref?.getString(getString(R.string.goal_title), "")

                        val db = helper!!.writableDatabase
                        try {
                            db.execSQL("insert into ACHIEVE_TABLE(sof, period,  title) VALUES('${getString(R.string.failure)}', '$period', '$title')")
                        } finally {
                            db.close()
                        }

                        sharedPref?.let {
                            with(sharedPref.edit()) {
                                putBoolean(getString(com.android.norram.patienter.R.string.set_flag), false)
                                remove(getString(com.android.norram.patienter.R.string.start_time))
                                remove(getString(com.android.norram.patienter.R.string.goal_time))
                                remove(getString(com.android.norram.patienter.R.string.goal_title))
                                apply()
                            }
                        }

                        timer.cancel()
                        val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
                        findNavController().navigate(action)
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}