package com.android.norram.patienter

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.android.norram.patienter.databinding.FragmentSetBinding
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class SetFragment : Fragment() {
    private lateinit var titleEdit: EditText
    private lateinit var yearSpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var daySpinner: Spinner
    private lateinit var hourSpinner: Spinner
    private lateinit var minuteSpinner: Spinner
    private lateinit var secondSpinner: Spinner
    private lateinit var hourText: TextView
    private lateinit var minuteText: TextView
    private lateinit var secondText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            val flag = sharedPref.getBoolean(getString(R.string.set_flag), false)
            if(flag) findNavController().navigate(R.id.action_setFragment_to_countFragment)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSetBinding>(inflater,
            R.layout.fragment_set, container, false)

        var flagMonthSpinner = true
        val now = LocalDateTime.now()

        titleEdit = binding.titleEdit
        yearSpinner = binding.yearSpinner
        monthSpinner = binding.monthSpinner
        daySpinner = binding.daySpinner
        hourSpinner = binding.hourSpinner
        minuteSpinner = binding.minuteSpinner
        secondSpinner = binding.secondSpinner
        hourText = binding.hourText
        minuteText = binding.minuteText
        secondText = binding.secondText

        val yearList = ArrayList<String>()
        val monthList = ArrayList<String>()
        val dayList = ArrayList<String>()
        val hourList = ArrayList<String>()
        val minuteList = ArrayList<String>()
        val secondList = ArrayList<String>()

        monthList.add("")
        dayList.add("")

        for(i in 0..10) {
            yearList.add((now.year + i).toString())
        }
        for(i in 1..12) {
            monthList.add(i.toString())
        }
        val calendar = Calendar.getInstance()
        calendar.set(now.year, now.monthValue - 1, 1)
        var dayMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for(i in 1..dayMax) {
            dayList.add(i.toString())
        }
        for(i in 0..23) {
            hourList.add(i.toString())
        }
        for(i in 0..59) {
            minuteList.add(i.toString())
        }
        for(i in 0..59) {
            secondList.add(i.toString())
        }

        val yearAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            yearList
        )
        val monthAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            monthList
        )
        var dayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dayList
        )
        val hourAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            hourList
        )
        val minuteAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            minuteList
        )
        val secondAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            secondList
        )

        yearAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        hourAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        secondAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        yearSpinner.adapter = yearAdapter
        monthSpinner.adapter = monthAdapter
        daySpinner.adapter = dayAdapter
        hourSpinner.adapter = hourAdapter
        minuteSpinner.adapter = minuteAdapter
        secondSpinner.adapter = secondAdapter

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if(flagMonthSpinner || pos == 0) {
                    flagMonthSpinner = false
                    return
                }
                if(pos-1 == 1) {
                    var days = 28
                    if(Integer.valueOf(yearSpinner.selectedItem.toString()) % 4 == 0) {
                        if(Integer.valueOf(yearSpinner.selectedItem.toString()) % 100 == 0) {
                            if(Integer.valueOf(yearSpinner.selectedItem.toString()) % 400 == 0) {
                                days = 29
                            }
                        } else {
                            days = 29
                        }
                    }
                    dayList.clear()
                    dayList.add("")
                    for(i in 1..days) {
                        dayList.add(i.toString())
                    }
                } else {
                    calendar.set(Calendar.MONTH, pos-1)
                    dayList.clear()
                    dayList.add("")
                    for(i in 1..(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))) {
                        dayList.add(i.toString())
                    }
                }
                dayAdapter = ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    dayList
                )
                dayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
                daySpinner.adapter = dayAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        binding.detailSwitch.setOnCheckedChangeListener {_, isChecked ->
            if(isChecked) {
                hourSpinner.visibility = View.VISIBLE
                minuteSpinner.visibility = View.VISIBLE
                secondSpinner.visibility = View.VISIBLE
                hourText.visibility = View.VISIBLE
                minuteText.visibility = View.VISIBLE
                secondText.visibility = View.VISIBLE
            } else {
                hourSpinner.setSelection(0)
                secondSpinner.setSelection(0)
                minuteSpinner.setSelection(0)
                hourSpinner.visibility = View.INVISIBLE
                minuteSpinner.visibility = View.INVISIBLE
                secondSpinner.visibility = View.INVISIBLE
                hourText.visibility = View.INVISIBLE
                minuteText.visibility = View.INVISIBLE
                secondText.visibility = View.INVISIBLE
            }
        }

        binding.startButton.setOnClickListener { view: View ->
            if(monthSpinner.selectedItem.toString() == ""
                || daySpinner.selectedItem.toString() == "") {
                Toast.makeText(context, R.string.alert1, Toast.LENGTH_LONG).show()
            } else {
                val year = Integer.valueOf(yearSpinner.selectedItem.toString())
                val month = Integer.valueOf(monthSpinner.selectedItem.toString())
                val day = Integer.valueOf(daySpinner.selectedItem.toString())
                val hour = Integer.valueOf(hourSpinner.selectedItem.toString())
                val minute = Integer.valueOf(minuteSpinner.selectedItem.toString())
                val second = Integer.valueOf(secondSpinner.selectedItem.toString())
                val title = titleEdit.text.toString()

                val selectedTime = LocalDateTime.of(year, month, day, hour, minute, second)
                Log.i("vox", "SelectedTime: ${selectedTime}")

//                val selectedTime = "%1$02d-%2$02d-%3$02dT%4$02d:%5$02d:%6$02d".format(year, month, day, hour, minute, second)
                if(isFutureTime(selectedTime)) {
                    val dialogFragment = StartDialogFragment(title, selectedTime)
                    activity?.let {
                        dialogFragment.show(it.supportFragmentManager,  "help_dialog")
                    }
                }
            }
        }

        binding.achieveButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_setFragment_to_achieveFragment)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.helpMenu -> {
                val dialogFragment = HelpDialogFragment()
                activity?.let { dialogFragment.show(it.supportFragmentManager,  "help_dialog") }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isFutureTime(selectedTime: LocalDateTime): Boolean {
        if(selectedTime <= LocalDateTime.now()) {
            Toast.makeText(context, R.string.alert2, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}