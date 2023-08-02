package io.github.xamdr.noties.ui.reminders

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogFragmentDatetimePickerBinding
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.getPositiveButton
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.views.MaterialSpinner
import timber.log.Timber
import java.time.*
import javax.inject.Inject

@AndroidEntryPoint
class DateTimePickerDialogFragment : DialogFragment(), MaterialCheckBox.OnCheckedStateChangedListener {

	private var _binding: DialogFragmentDatetimePickerBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<DateTimePickerViewModel>()
	private val reminderDate by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_REMINDER_DATE)
	}
	private lateinit var helper: DateTimePickerHelper
	private lateinit var dateAdapter: ReminderDateAdapter
	private lateinit var timeAdapter: ReminderTimeAdapter
	private lateinit var spinnerDate: MaterialSpinner<ReminderDate>
	private lateinit var spinnerTime: MaterialSpinner<ReminderTime>
	var listener: DateTimeListener? = null
	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onAttach(context: Context) {
		super.onAttach(context)
		helper = DateTimePickerHelper(context)
		dateAdapter = ReminderDateAdapter(context, R.layout.item_spinner_date, helper.dates)
		timeAdapter = ReminderTimeAdapter(context, R.layout.item_spinner_time, helper.times)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentDatetimePickerBinding.inflate(layoutInflater).apply {
			buildView(spinnerDate, spinnerTime)
			exactAlarmPref.apply {
				isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
				isChecked = preferenceStorage.isExactAlarmEnabled
				addOnCheckedStateChangedListener(this@DateTimePickerDialogFragment)
			}
		}.also {
			setAdapters()
			onSpinnerItemClick(spinnerDate, spinnerTime)
		}
		val builder = MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.add_reminder)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button, null)
		if (reminderDate != 0L) {
			builder.setNeutralButton(R.string.delete_button) { _, _ -> listener?.onReminderDateDeleted() }
		}
		return builder.create().apply {
			setOnShowListener {
				getButton(AlertDialog.BUTTON_POSITIVE).apply {
					setOnClickListener { onPositiveButtonClick() }
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		binding.exactAlarmPref.removeOnCheckedStateChangedListener(this)
		_binding = null
	}

	override fun onResume() {
		super.onResume()
		onDateTimeSet()
		viewModel.reminderState.observe(this) { state ->
			when (state) {
				ReminderDateState.ReminderDateInvalid -> getPositiveButton().isEnabled = false
				ReminderDateState.ReminderDateNotSet -> getPositiveButton().isEnabled = false
				ReminderDateState.ReminderDateValid -> getPositiveButton().isEnabled = true
			}
		}
	}

	override fun onCheckedStateChangedListener(checkBox: MaterialCheckBox, state: Int) {
		when (state) {
			MaterialCheckBox.STATE_CHECKED -> preferenceStorage.isExactAlarmEnabled = true
			MaterialCheckBox.STATE_UNCHECKED -> preferenceStorage.isExactAlarmEnabled = false
			MaterialCheckBox.STATE_INDETERMINATE -> {}
		}
	}

	private fun onPositiveButtonClick() {
		if (!preferenceStorage.isExactAlarmEnabled) {
			onDateTimeSet()
		}
		else {
			if (AlarmManagerHelper.canScheduleExactAlarms(requireContext())) {
				onDateTimeSet()
			}
			else {
				MaterialAlertDialogBuilder(requireContext())
					.setMessage(getString(R.string.exact_alarm_permission_rationale))
					.setNegativeButton(R.string.cancel_button, null)
					.setPositiveButton(R.string.go_to_settings_button) { _, _ -> requestScheduleExactAlarm() }
					.show()
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun buildView(vararg spinners: MaterialSpinner<*>) {
		spinnerDate = spinners[0] as MaterialSpinner<ReminderDate>
		spinnerTime = spinners[1] as MaterialSpinner<ReminderTime>
		if (reminderDate != 0L) {
			val selectedDate = DateTimeHelper.formatDate(reminderDate)
			val selectedTime = DateTimeHelper.formatTime(reminderDate)
			spinnerDate.setSelectedValue(selectedDate)
			spinnerTime.setSelectedValue(selectedTime)
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
				else {
					viewModel.onReminderDateSelected(spinnerDate.selectedItem, spinnerTime.selectedItem)
				}
			}
		})
		spinners[1].setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener {
			override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
				if (position == helper.times.size - 1) {
					showTimePickerDialog()
				}
				else {
					viewModel.onReminderDateSelected(spinnerDate.selectedItem, spinnerTime.selectedItem)
				}
			}
		})
	}

	private fun onDateTimeSet() {
		if (spinnerDate.selectedItem != null && spinnerTime.selectedItem != null) {
			val selectedDate = spinnerDate.selectedItem?.value ?: return
			val selectedTime = spinnerTime.selectedItem?.value ?: return
			val instant = LocalDateTime.of(selectedDate, selectedTime).atZone(ZoneId.systemDefault()).toInstant()
			Timber.d("Instant: $instant")
			listener?.onReminderDateSet(instant)
			dismiss()
		}
	}

	private fun showDatePickerDialog() {
		val selection = if (reminderDate == 0L) MaterialDatePicker.todayInUtcMilliseconds() else reminderDate
		val start = Instant.now().toEpochMilli()
		val datePicker = MaterialDatePicker.Builder
			.datePicker()
			.setTitleText(R.string.select_date_title)
			.setSelection(selection)
			.setCalendarConstraints(CalendarConstraints.Builder()
				.setStart(start)
				.build()
			)
			.build()
		datePicker.addOnPositiveButtonClickListener(object: MaterialPickerOnPositiveButtonClickListener<Long> {
			override fun onPositiveButtonClick(selection: Long) {
				datePicker.removeOnPositiveButtonClickListener(this)
				onDateSet(selection)
			}
		})
		datePicker.addOnNegativeButtonClickListener(object : View.OnClickListener {
			override fun onClick(v: View?) {
				datePicker.removeOnNegativeButtonClickListener(this)
				spinnerDate.selectedItem = null
				viewModel.onReminderDateSelected(spinnerDate.selectedItem, spinnerTime.selectedItem)
			}
		})
		datePicker.show(parentFragmentManager, DATE_PICKER_TAG)
	}

	private fun onDateSet(selection: Long) {
		val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
		val customReminderDate = ReminderDate.CustomDate(getString(R.string.select_date), selectedDate)
		helper.dates[helper.dates.size - 1] = customReminderDate
		spinnerDate.apply {
			selectedItem = customReminderDate
			setSelectedValue(customReminderDate.toString())
		}
		viewModel.onReminderDateSelected(spinnerDate.selectedItem, spinnerTime.selectedItem)
	}

	private fun showTimePickerDialog() {
		val isSystem24Hour = DateFormat.is24HourFormat(requireContext())
		val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
		val epochMilli = if (reminderDate == 0L) Instant.now().toEpochMilli() else reminderDate
		val localTime = LocalTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
		val timePicker = MaterialTimePicker.Builder()
			.setTitleText(R.string.select_time_title)
			.setTimeFormat(clockFormat)
			.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
			.setHour(localTime.hour)
			.setMinute(localTime.minute)
			.build()
		timePicker.addOnPositiveButtonClickListener(object: View.OnClickListener {
			override fun onClick(v: View?) {
				timePicker.removeOnPositiveButtonClickListener(this)
				onTimeSet(timePicker.hour, timePicker.minute)
			}
		})
		timePicker.addOnNegativeButtonClickListener(object : View.OnClickListener {
			override fun onClick(v: View?) {
				timePicker.removeOnNegativeButtonClickListener(this)
				spinnerTime.selectedItem = null
				viewModel.onReminderDateSelected(spinnerDate.selectedItem, spinnerTime.selectedItem)
			}
		})
		timePicker.show(parentFragmentManager, TIME_PICKER_TAG)
	}

	private fun onTimeSet(hour: Int, minute: Int) {
		val selectedTime = LocalTime.of(hour, minute)
		val customReminderTime = ReminderTime.CustomTime(getString(R.string.select_time), selectedTime)
		helper.times[helper.times.size - 1] = customReminderTime
		spinnerTime.apply {
			selectedItem = customReminderTime
			setSelectedValue(customReminderTime.toString())
		}
		viewModel.onReminderDateSelected(spinnerDate.selectedItem, spinnerTime.selectedItem)
	}

	private fun requestScheduleExactAlarm() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
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