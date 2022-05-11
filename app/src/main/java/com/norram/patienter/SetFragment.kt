package com.norram.patienter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.norram.patienter.databinding.FragmentSetBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class SetFragment : Fragment() {
    private lateinit var binding: FragmentSetBinding
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

    private var flagPreventFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        (activity as AppCompatActivity).let {
            it.supportActionBar?.title = "Patienter"
        }

        sharedPref?.let {
            if(sharedPref.getBoolean(getString(R.string.set_flag), false))
                findNavController().navigate(R.id.action_setFragment_to_countFragment)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_set, container, false)
        flagPreventFirst = true

        var flagPreventTwice = false // to prevent changeDayList function from being run twice
        val prevCalendar = Calendar.getInstance()

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

        // for hiding keyboard
        binding.setConstraint.setOnTouchListener { v, event ->
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN->{
                    v.requestFocus()
                }
            }
            true
        }
        titleEdit.setOnFocusChangeListener { view, b ->
            if(!b) {
                val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        setSpinnerOptions(yearSpinner)
        setSpinnerOptions(monthSpinner)
        setSpinnerOptions(daySpinner)
        setSpinnerOptions(hourSpinner)
        setSpinnerOptions(minuteSpinner)
        setSpinnerOptions(secondSpinner)

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if(flagPreventTwice) {
                    flagPreventTwice = false
                    return
                }
                if(flagPreventFirst || pos == 0) {
                    flagPreventFirst = false
                    return
                }
                setSpinnerOptions(daySpinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val detailSwitch = binding.detailSwitch

        binding.chooseButton.setOnClickListener {
            val supportFragmentManager = requireActivity().supportFragmentManager
            val timePickerDialogFragment = TimePickerDialogFragment()
            supportFragmentManager.setFragmentResultListener(
                getString(R.string.request_key2),
                viewLifecycleOwner
            ) { resultKey2, bundle2 ->
                if (resultKey2 == getString(R.string.request_key2)) {
                    val chooseTime = bundle2.getString(getString(R.string.choose_time))
                    val time = LocalTime.parse(chooseTime)
                    val hour = time.hour
                    val minute = time.minute

                    hourSpinner.setSelection(hour)
                    minuteSpinner.setSelection(minute)
                }
            }

            val datePickerDialogFragment = DatePickerDialogFragment(prevCalendar, detailSwitch.isChecked, timePickerDialogFragment)
            supportFragmentManager.setFragmentResultListener(
                getString(R.string.request_key),
                viewLifecycleOwner
            ) { resultKey, bundle ->
                if(resultKey == getString(R.string.request_key)) {
                    val chooseDate = bundle.getString(getString(R.string.choose_date))
                    val date = LocalDate.parse(chooseDate, DateTimeFormatter.ISO_DATE)
                    val year = date.year
                    val month = date.monthValue
                    val day = date.dayOfMonth
                    prevCalendar.set(year, month-1, day)
                    yearSpinner.setSelection(year - LocalDate.now().year)
                    monthSpinner.setSelection(month)
                    setSpinnerOptions(daySpinner)
                    daySpinner.setSelection(day)

                    flagPreventTwice = true
                }
            }

            datePickerDialogFragment.show(supportFragmentManager, "choose_dialog")
        }

        detailSwitch.setOnCheckedChangeListener {_, isChecked ->
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

        binding.startButton.setOnClickListener {
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

                try {
                    // check whether the date exists
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    format.isLenient = false
                    format.parse(getString(R.string.choose_date_format).format(year, month, day))

                    val selectedTime = LocalDateTime.of(year, month, day, hour, minute, second)
                    if(selectedTime.isAfter(LocalDateTime.now())) {
                        val dialogFragment = StartDialogFragment(title, selectedTime)
                        activity?.let { dialogFragment.show(it.supportFragmentManager,  "help_dialog") }
                    } else {
                        Toast.makeText(context, R.string.alert2, Toast.LENGTH_LONG).show()
                    }
                } catch(e: ParseException) {
                    Toast.makeText(context, "存在しない日付です", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.achieveButton.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_setFragment_to_achieveFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finish()
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

    private fun setSpinnerOptions(spinner: Spinner) {
        val mList = ArrayList<String>()
        if(spinner == binding.monthSpinner || spinner == daySpinner) mList.add("")

        val date = if(flagPreventFirst) {
            LocalDate.now()
        } else {
            LocalDate.of(Integer.valueOf(yearSpinner.selectedItem.toString()), Integer.valueOf(monthSpinner.selectedItem.toString()), 1)
        }
        val (a, b) = when(spinner) {
            binding.monthSpinner -> Pair(1, 12)
            binding.daySpinner -> Pair(1, date.lengthOfMonth())
            binding.hourSpinner -> Pair(0, 23)
            binding.minuteSpinner, binding.secondSpinner -> Pair(0, 59)
            else -> Pair(date.year + 0, date.year + 10)
        }
        for(i in a..b) {
            mList += i.toString()
        }
        val mAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            mList
        )
        mAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = mAdapter
    }
}