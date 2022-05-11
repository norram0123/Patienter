package com.norram.patienter

import android.app.ActionBar
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.norram.patienter.databinding.FragmentCountBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class CountFragment : Fragment() {
    private var helper: AchieveOpenHelper? = null
    private var defaultValue = "2030-11-11T00:00:00"
    private var hours: Int = 0
    private var message = ""
    private var judgeFlag = true

    private lateinit var sharedPref: SharedPreferences
    private lateinit var startTime: LocalDateTime
    private lateinit var goalTime: LocalDateTime
    private lateinit var now: LocalDateTime
    private lateinit var timer: Timer

    private var goalTitle: String? = ""
    private var dialogFragment: RetireDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        startTime = LocalDateTime.parse(sharedPref.getString(getString(R.string.start_time), defaultValue))
        goalTime = LocalDateTime.parse(sharedPref.getString(getString(R.string.goal_time), defaultValue))
        goalTitle = sharedPref.getString(getString(R.string.goal_title), "")

//        (activity as AppCompatActivity?)!!.supportActionBar?.title = goalTitle
        (activity as AppCompatActivity).let {
            if(goalTitle != "") it.supportActionBar?.title = goalTitle
        }

        now = LocalDateTime.now()
        var diffDays = compareLocalDate(startTime.toLocalDate(), goalTime.toLocalDate())
        val diffSeconds: Long
        if(startTime.toLocalTime() < goalTime.toLocalTime()) {
            diffSeconds = compareLocalTime(startTime.toLocalTime(), goalTime.toLocalTime())
        } else {
            diffDays--
            diffSeconds = compareLocalTime(startTime.toLocalTime(), goalTime.toLocalTime()) + 24*3600
        }
        val half = 1800
        hours = (diffDays*24 + ((diffSeconds+half) / 3600)).toInt()
        message = getString(R.string.cong)

        if(judge()) {
            val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentCountBinding>(inflater,
            R.layout.fragment_count, container, false)
        val goalTimeText = binding.goalTimeText
        val timeText = binding.timeText
        val dayText = binding.dayText
//        val timeLinear = binding.timeLinear

        val handler = Handler(Looper.getMainLooper())
        timer = Timer()

        var differentSeconds: Long
        var differentDays: Long

        // timeLinear -> width = height
//        val wlLayout = timeLinear.layoutParams
//        wlLayout.height = timeLinear.width
//        timeLinear.setLayoutParams(wlLayout)
//        timeLinear.layoutParams = wlLayout

        goalTimeText.text = getString(R.string.goal_formatter).format(goalTime.year, goalTime.monthValue, goalTime.dayOfMonth, goalTime.hour, goalTime.minute, goalTime.second)

        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    if(judge()) {
                        timer.cancel()
                        val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
                        findNavController().navigate(action)
                    }

                    now = LocalDateTime.now()
                    if(now.toLocalTime() >= goalTime.toLocalTime()) {
                        differentSeconds = 3600*24 + compareLocalTime(now.toLocalTime(), goalTime.toLocalTime())
                        differentDays = compareLocalDate(now.toLocalDate(), goalTime.toLocalDate()) - 1
                    } else {
                        differentSeconds = compareLocalTime(now.toLocalTime(), goalTime.toLocalTime())
                        differentDays = compareLocalDate(now.toLocalDate(), goalTime.toLocalDate())
                    }
                    val h = differentSeconds / 3600
                    val m = differentSeconds % 3600 / 60
                    val s = differentSeconds % 60
                    timeText.text = ("%1$02d:%2$02d:%3$02d".format(h, m, s))
                    dayText.text = getString(R.string.days).format(differentDays)
                }
            }
        }, 0, 60)

        binding.retireButton.setOnClickListener { view: View ->
            dialogFragment = RetireDialogFragment(timer)
            activity?.let {
                dialogFragment?.show(it.supportFragmentManager,  "retire_dialog")
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

            if(judgeFlag) {
                judgeFlag = false

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
                    putBoolean(getString(R.string.set_flag), false)
                    remove(getString(R.string.start_time))
                    remove(getString(R.string.goal_time))
                    remove(getString(R.string.goal_title))
                    apply()
                }
            }
            dialogFragment?.dismiss()
            return true
        }
        return false
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