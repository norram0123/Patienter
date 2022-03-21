package com.android.norram.patienter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.android.norram.patienter.databinding.FragmentCountBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class CountFragment : Fragment() {
    private var helper: AchieveOpenHelper? = null
//    private lateinit var defaultValue: String
    private var defaultValue = "2030-11-11T00:00:00"
    private var hours: Int = 0
    private var message = ""

    private lateinit var sharedPref: SharedPreferences
    private lateinit var startTime: LocalDateTime
    private lateinit var goalTime: LocalDateTime
    private lateinit var now: LocalDateTime
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("vox", "count onCreate started")

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        startTime = LocalDateTime.parse(sharedPref.getString(getString(R.string.start_time), defaultValue))
        goalTime = LocalDateTime.parse(sharedPref.getString(getString(R.string.goal_time), defaultValue))

        now = LocalDateTime.now()
        hours = ChronoUnit.HOURS.between(startTime.toLocalTime(), goalTime.toLocalTime()).toInt()
        message = getString(R.string.cong)

        Log.i("vox", "startTime(after): ${startTime}")
        Log.i("vox", "goalTime(after): ${goalTime}")
        if(judge()) {
            val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentCountBinding>(inflater,
            R.layout.fragment_count, container, false)
        val goalTimeText = binding.goalTimeText
        val timeText = binding.timeText
        val dayText = binding.dayText
        val timeLinear = binding.timeLinear

        val handler = Handler(Looper.getMainLooper())
        timer = Timer()

        var differentSeconds: Long = 0
        var differentDays: Long = 0

        // timeLinear -> width = height
        val wlLayout = timeLinear.layoutParams
//        wlLayout.height = timeLinear.width
//        timeLinear.setLayoutParams(wlLayout)
//        timeLinear.layoutParams = wlLayout
        Log.i("equals", "h:${wlLayout.height}, w:${wlLayout.width}")

        val dtformat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        goalTimeText.text = ("Goal Time: " + dtformat.format(goalTime))

        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    now = LocalDateTime.now()
                    if(now.hour > goalTime.hour) {
                        differentSeconds = 3600*24 + ChronoUnit.SECONDS.between(now.toLocalTime(), goalTime.toLocalTime())
                        differentDays = ChronoUnit.DAYS.between(now.toLocalDate(), goalTime.toLocalDate()) - 1
                    } else {
                        differentSeconds = ChronoUnit.SECONDS.between(now.toLocalTime(), goalTime.toLocalTime())
                        differentDays = ChronoUnit.DAYS.between(now.toLocalDate(), goalTime.toLocalDate())
                    }
                    val h = differentSeconds / 3600
                    val m = differentSeconds % 3600 / 60
                    val s = differentSeconds % 60
                    timeText.text = ("%1$02d:%2$02d:%3$02d".format(h, m, s))
                    dayText.text = "${differentDays}" + getString(R.string.days)

                    if(judge()) {
                        timer.cancel()
                        val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
                        findNavController().navigate(action)
                    }
                }
            }
        }, 0, 60)

        binding.retireButton.setOnClickListener { view: View ->
            val dialogFragment = RetireDialogFragment(timer)
            activity?.let {
                dialogFragment.show(it.supportFragmentManager,  "retire_dialog")
            }
        }

        binding.achieveButton.setOnClickListener { view: View ->
            timer.cancel()
            view.findNavController()
                .navigate(R.id.action_countFragment_to_achieveFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {}

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(judge()) {
            val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
            findNavController().navigate(action)
        }
    }

    private fun judge(): Boolean {
        if(now >= goalTime) {
//            timer.cancel()

            if(helper == null) { helper = AchieveOpenHelper(requireContext()) }
            val db = helper?.writableDatabase ?: return false
            try {
                val period = "${startTime.year}/${startTime.monthValue}/${startTime.dayOfMonth}" + " ～ " +
                        "${goalTime.year}/${goalTime.monthValue}/${goalTime.dayOfMonth}"
                val title = sharedPref.getString(getString(R.string.goal_title), "")
                db.execSQL("insert into ACHIEVE_TABLE(sof, period,  title) VALUES('${getString(R.string.success)}', '$period', '$title')")
            } finally {
                db.close()
            }

            with(sharedPref.edit()) {
                putBoolean(getString(com.android.norram.patienter.R.string.set_flag), false)
                remove(getString(com.android.norram.patienter.R.string.start_time))
                remove(getString(com.android.norram.patienter.R.string.goal_time))
                remove(getString(com.android.norram.patienter.R.string.goal_title))
                apply()
            }
            return true
        }
        return false
    }
}