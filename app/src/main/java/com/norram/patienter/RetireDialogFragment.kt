package com.norram.patienter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import java.time.LocalDateTime
import java.util.*

class RetireDialogFragment constructor(_timer: Timer) : DialogFragment() {
    private val timer: Timer = _timer
    private var helper: AchieveOpenHelper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.retire_dialog)
                .setPositiveButton(R.string.ok
                ) { _, _ ->
                    if (helper == null) { helper = AchieveOpenHelper(requireContext()) }
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                    val defaultValue = resources.getString(R.string.default_goal_time)
                    val startTime = LocalDateTime.parse(sharedPref?.getString(getString(R.string.start_time), defaultValue))
                    val goalTime = LocalDateTime.parse(sharedPref?.getString(getString(R.string.goal_time), defaultValue))
                    val dateManager = DateManager()
                    val hours = dateManager.getDiffHours(startTime, LocalDateTime.now())
                    val message = getString(R.string.give_up)
                    val period = "${startTime.year}/${startTime.monthValue}/${startTime.dayOfMonth}" + " ～ " +
                                "${goalTime.year}/${goalTime.monthValue}/${goalTime.dayOfMonth}"
                    val title = sharedPref?.getString(getString(R.string.goal_title), "")

                    // val db = helper!!.writable...try{}
                    helper!!.writableDatabase.use { db ->
                        db.execSQL(
                            "insert into ACHIEVE_TABLE(sof, period,  title) VALUES('${
                                getString(R.string.failure)
                            }', '$period', '$title')"
                        )
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
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}