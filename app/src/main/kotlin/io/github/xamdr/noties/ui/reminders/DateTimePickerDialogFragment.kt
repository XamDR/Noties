package io.github.xamdr.noties.ui.reminders

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogFragmentDatetimePickerBinding
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.views.MaterialSpinner
import timber.log.Timber
import java.time.*

class DateTimePickerDialogFragment : DialogFragment() {

	private var _binding: DialogFragmentDatetimePickerBinding? = null
	private val binding get() = _binding!!
	private val reminderDate by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_REMINDER_DATE, 0L)
	}
	private lateinit var helper: DateTimePickerHelper
	private lateinit var dateAdapter: ReminderDateAdapter
	private lateinit var timeAdapter: ReminderTimeAdapter
	private lateinit var spinnerDate: MaterialSpinner<ReminderDate>
	private lateinit var spinnerTime: MaterialSpinner<ReminderTime>
	private var listener: DateTimeListener? = null

	override fun onAttach(context: Context) {
		super.onAttach(context)
		helper = DateTimePickerHelper(context)
		dateAdapter = ReminderDateAdapter(context, R.layout.item_spinner_date, helper.dates)
		timeAdapter = ReminderTimeAdapter(context, R.layout.item_spinner_time, helper.times)
	}

	@Suppress("UNCHECKED_CAST")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentDatetimePickerBinding.inflate(layoutInflater).apply {
			this@DateTimePickerDialogFragment.spinnerDate = spinnerDate as MaterialSpinner<ReminderDate>
			this@DateTimePickerDialogFragment.spinnerTime = spinnerTime as MaterialSpinner<ReminderTime>
		}.also {
			setAdapters()
			onSpinnerItemClick(spinnerDate, spinnerTime)
		}
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.add_reminder)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ -> onDateTimeSet() }
			.create()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun setDateTimeListener(listener: DateTimeListener) {
		this.listener = listener
	}

	private fun onDateTimeSet() {
		if (spinnerDate.selectedItem != null && spinnerTime.selectedItem != null) {
			val selectedDate = spinnerDate.selectedItem?.value ?: return
			val selectedTime = spinnerTime.selectedItem?.value ?: return
			val instant = LocalDateTime.of(selectedDate, selectedTime).atZone(ZoneId.systemDefault()).toInstant()
			Timber.d("Instant: $instant")
			listener?.onDateTimeSet(instant)
		}
	}

	private fun setAdapters() {
		spinnerDate.setAdapter(dateAdapter)
		spinnerTime.setAdapter(timeAdapter)
	}

	private fun onSpinnerItemClick(vararg spinners: MaterialSpinner<*>) {
		spinners[0].setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener {
			override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
				if (position == helper.dates.size - 1) {
					showDatePickerDialog()
				}
			}
		})
		spinners[1].setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener {
			override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
				if (position == helper.times.size - 1) {
					showTimePickerDialog()
				}
			}
		})
	}

	private fun showDatePickerDialog() {
		val datePicker = MaterialDatePicker.Builder
			.datePicker()
			.setTitleText(R.string.select_date_title)
			.setSelection(reminderDate)
			.build()
		datePicker.addOnPositiveButtonClickListener(object: MaterialPickerOnPositiveButtonClickListener<Long> {
			override fun onPositiveButtonClick(selection: Long) {
				datePicker.removeOnPositiveButtonClickListener(this)
				onDateSet(selection)
			}
		})
		datePicker.show(parentFragmentManager, DATE_PICKER_TAG)
	}

	private fun onDateSet(selection: Long) {
		val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
		val customReminderDate = ReminderDate.CustomDate(getString(R.string.select_date), selectedDate)
		helper.dates[helper.dates.size - 1] = customReminderDate
		binding.spinnerDate.apply {
			selectedItem = customReminderDate
			setSelectedValue(customReminderDate.toString())
		}
	}

	private fun showTimePickerDialog() {
		val isSystem24Hour = DateFormat.is24HourFormat(requireContext())
		val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
		val timePicker = MaterialTimePicker.Builder()
			.setTitleText(R.string.select_time_title)
			.setTimeFormat(clockFormat)
			.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
//			.setHour(reminderDate.hour)
//			.setMinute(reminderDate.minute)
			.build()
		timePicker.addOnPositiveButtonClickListener(object: View.OnClickListener {
			override fun onClick(v: View?) {
				timePicker.removeOnPositiveButtonClickListener(this)
				onTimeSet(timePicker.hour, timePicker.minute)
			}
		})
		timePicker.show(parentFragmentManager, TIME_PICKER_TAG)
	}

	private fun onTimeSet(hour: Int, minute: Int) {
		val selectedTime = LocalTime.of(hour, minute)
		val customReminderTime = ReminderTime.CustomTime(getString(R.string.select_time), selectedTime)
		helper.times[helper.times.size - 1] = customReminderTime
		binding.spinnerTime.apply {
			selectedItem = customReminderTime
			setSelectedValue(customReminderTime.toString())
		}
	}

	companion object {
		private const val DATE_PICKER_TAG = "DATE_PICKER_TAG"
		private const val TIME_PICKER_TAG = "TIME_PICKER_TAG"
		fun newInstance(reminderDate: Long) = DateTimePickerDialogFragment().apply {
			arguments = bundleOf(Constants.BUNDLE_REMINDER_DATE to reminderDate)
		}
	}
}