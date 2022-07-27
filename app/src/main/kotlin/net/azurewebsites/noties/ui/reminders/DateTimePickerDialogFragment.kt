package net.azurewebsites.noties.ui.reminders

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.DialogFragmentDatetimePickerBinding
import net.azurewebsites.noties.ui.editor.EditorViewModel
import net.azurewebsites.noties.ui.helpers.printDebug
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class DateTimePickerDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener,
	TimePickerDialog.OnTimeSetListener {

	private var _binding: DialogFragmentDatetimePickerBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })
	private lateinit var helper: DateTimePickerHelper
	private lateinit var dateAdapter: ReminderDateAdapter
	private lateinit var timeAdapter: ReminderTimeAdapter
	private var selectedDate: LocalDate? = null
	private var selectedTime: LocalTime? = null

	override fun onAttach(context: Context) {
		super.onAttach(context)
		helper = DateTimePickerHelper(context)
		dateAdapter = ReminderDateAdapter(context, R.layout.spinner_date_item, helper.dates)
		timeAdapter = ReminderTimeAdapter(context, R.layout.spinner_time_item, helper.times)
		selectedDate = helper.dates[0].value
		selectedTime = helper.times[0].value
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		_binding = DialogFragmentDatetimePickerBinding.inflate(layoutInflater).apply {
			spinnerDate.adapter = dateAdapter
			spinnerTime.adapter = timeAdapter
		}
		onItemClick()
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.add_reminder)
			.setView(binding.root)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.ok_button) { _, _ -> scheduleNotification() }
			.create()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

	}

	override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

	}

	private fun scheduleNotification() {
		if (selectedDate != null && selectedTime != null) {
			val selectedDateTime = ZonedDateTime.of(selectedDate, selectedTime, ZoneId.systemDefault())
			printDebug(TAG, selectedDateTime)
			viewModel.updateNote(viewModel.entity.copy(reminderDate = selectedDateTime))
			val delay = selectedDateTime.toInstant().toEpochMilli()
			AlarmManagerHelper.setAlarmManager(requireContext(), delay)
		}
	}

	private fun onItemClick() {
		binding.spinnerDate.apply {
			(adapter as ReminderDateAdapter).setOnItemClickListener { _, _, position, _ ->
				hideDropDown()
				if (position == helper.dates.size - 1) {
					showDatePickerDialog()
				}
				else {
					binding.spinnerDate.setSelection(position)
					selectedDate = helper.dates[position].value
				}
			}
		}
		binding.spinnerTime.apply {
			(adapter as ReminderTimeAdapter).setOnItemClickListener { _, _, position, _ ->
				hideDropDown()
				if (position == helper.times.size - 1) {
					showTimePickerDialog()
				}
				else {
					binding.spinnerTime.setSelection(position)
					selectedTime = helper.times[position].value
				}
			}
		}
	}

	private fun showDatePickerDialog() {
		val reminderDate = viewModel.entity.reminderDate ?: ZonedDateTime.now()
		DatePickerDialog(
			requireContext(),
			this,
			reminderDate.year,
			reminderDate.monthValue - 1,
			reminderDate.dayOfMonth
		).show()
	}

	private fun showTimePickerDialog() {
		val reminderDate = viewModel.entity.reminderDate ?: ZonedDateTime.now()
		TimePickerDialog(
			requireContext(),
			this,
			reminderDate.hour,
			reminderDate.minute,
			DateFormat.is24HourFormat(requireContext())
		).show()
	}

	private companion object {
		private const val TAG = "DATE_TIME"
	}
}