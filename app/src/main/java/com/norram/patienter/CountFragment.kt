package com.norram.patienter

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
import java.time.LocalDateTime
import java.util.*

class CountFragment : Fragment() {
    private val defaultValue = "2030-11-11T00:00:00"
    private var helper: AchieveOpenHelper? = null
    private var hours: Int = 0
    private var message = ""
    private var judgeFlag = true
    private var goalTitle: String? = ""
    private var dialogFragment: RetireDialogFragment? = null

    private lateinit var binding: FragmentCountBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var startTime: LocalDateTime
    private lateinit var goalTime: LocalDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        startTime = LocalDateTime.parse(sharedPref.getString(getString(R.string.start_time), defaultValue))
        goalTime = LocalDateTime.parse(sharedPref.getString(getString(R.string.goal_time), defaultValue))
        goalTitle = sharedPref.getString(getString(R.string.goal_title), "")

        // set goal title to actionbar
        (activity as AppCompatActivity).let { if(goalTitle != "") it.supportActionBar?.title = goalTitle }

        val dateManager = DateManager()
        hours = dateManager.getDiffHours(startTime, goalTime)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_count, container, false)
        val dayText = binding.dayText
        val timeText = binding.timeText
        val goalPeriodText = binding.goalPeriodText
        val handler = Handler(Looper.getMainLooper())
        val timer = Timer()
        val dateManager = DateManager()

        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    if(judge()) {
                        timer.cancel()
                        val action = CountFragmentDirections.actionCountFragmentToResetFragment(hours, message)
                        findNavController().navigate(action)
                    }

                    val ds = dateManager.getDiffDs(goalTime, LocalDateTime.now())
                    val h = ds.second / 3600
                    val m = ds.second % 3600 / 60
                    val s = ds.second % 60
                    timeText.text = ("%1$02d:%2$02d:%3$02d".format(h, m, s))
                    dayText.text = getString(R.string.days).format(ds.first)
                }
            }
        }, 0, 60)

        goalPeriodText.text = getString(R.string.goal_formatter)
            .format(goalTime.year, goalTime.monthValue, goalTime.dayOfMonth, goalTime.hour, goalTime.minute, goalTime.second)

        binding.retireButton.setOnClickListener {
            dialogFragment = RetireDialogFragment(timer)
            activity?.let { dialogFragment?.show(it.supportFragmentManager,  "retire_dialog") }
        }

        binding.achieveButton.setOnClickListener { view: View ->
            timer.cancel()
            view.findNavController().navigate(R.id.action_countFragment_to_achieveFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            timer.cancel()
            activity?.finish()
        }

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
        if(goalTime.isBefore(LocalDateTime.now())) {
            if(judgeFlag) {
                judgeFlag = false

                if(helper == null) { helper = AchieveOpenHelper(requireContext()) }
                helper!!.writableDatabase.use{ db ->
                    val period = "${startTime.year}/${startTime.monthValue}/${startTime.dayOfMonth}" + " ～ " +
                            "${goalTime.year}/${goalTime.monthValue}/${goalTime.dayOfMonth}"
                    val title = sharedPref.getString(getString(R.string.goal_title), "")
                    db.execSQL("insert into ACHIEVE_TABLE(sof, period,  title) VALUES('${getString(R.string.success)}', '$period', '$title')")
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
}