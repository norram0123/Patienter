package com.android.norram.patienter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
                        val now = LocalDateTime.now()

                        var diffDays = compareLocalDate(startTime.toLocalDate(), now.toLocalDate())
                        val diffSeconds: Long
                        if(startTime.toLocalTime() < now.toLocalTime()) {
                            diffSeconds = compareLocalTime(startTime.toLocalTime(), now.toLocalTime())
                        } else {
                            diffDays--
                            diffSeconds = compareLocalTime(startTime.toLocalTime(), now.toLocalTime().plusHours(24L))
                        }
                        val half = 1800
                        val hours = (diffDays*24 + ((diffSeconds+half) / 3600)).toInt()
                        val message = getString(R.string.give_up)

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
                                putBoolean(getString(R.string.set_flag), false)
                                remove(getString(R.string.start_time))
                                remove(getString(R.string.goal_time))
                                remove(getString(R.string.goal_title))
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

    private fun compareLocalDate(dateSmall: LocalDate, dateBig: LocalDate): Long {
        var diffDays: Long = 0
        var d1 = dateSmall
        val d2 = dateBig
        while(d1 != d2) {
            d1 = d1.plusDays(1L)
            diffDays++
        }
        return diffDays
    }

    private fun compareLocalTime(timeSmall: LocalTime, timeBig: LocalTime): Long {
        val s1 = timeSmall.hour * 3600 + timeSmall.minute * 60 + timeSmall.second
        val s2 = timeBig.hour * 3600 + timeBig.minute * 60 + timeBig.second
        return (s2 -s1).toLong()
    }
}