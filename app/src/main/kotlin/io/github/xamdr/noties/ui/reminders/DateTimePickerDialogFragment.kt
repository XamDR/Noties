package io.github.xamdr.noties.ui.reminders

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.DialogFragmentDatetimePickerBinding
import io.github.xamdr.noties.ui.editor.EditorViewModel
import timber.log.Timber
import java.time.*

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
		selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
		helper.dates[helper.dates.size - 1] = ReminderDate.CustomDate(getString(R.string.select_date), value = selectedDate)
		dateAdapter.notifyDataSetChanged()
		binding.spinnerDate.setSelection(helper.dates.size - 1)
	}

	override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
		selectedTime = LocalTime.of(hourOfDay, minute)
		helper.times[helper.times.size - 1] = ReminderTime.CustomTime(getString(R.string.select_time), value = selectedTime)
		timeAdapter.notifyDataSetChanged()
		binding.spinnerTime.setSelection(helper.times.size - 1)
	}

	private fun scheduleNotification() {
		if (selectedDate != null && selectedTime != null) {
			val selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)
			Timber.d("SelectedDateTime: %s", selectedDateTime)
//			viewModel.note = viewModel.note.copy(reminderDate = selectedDateTime)
//			val delay = selectedDateTime.toInstant(ZoneOffset.of(ZoneId.systemDefault().id)).toEpochMilli()
//			AlarmManagerHelper.setAlarmManager(requireContext(), delay, viewModel.note)
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
//		val reminderDate = viewModel.note.reminderDate ?: LocalDateTime.now()
//		DatePickerDialog(
//			requireContext(),
//			this,
//			reminderDate.year,
//			reminderDate.monthValue - 1,
//			reminderDate.dayOfMonth
//		).show()
	}

	private fun showTimePickerDialog() {
//		val reminderDate = viewModel.note.reminderDate ?: LocalDateTime.now()
//		TimePickerDialog(
//			requireContext(),
//			this,
//			reminderDate.hour,
//			reminderDate.minute,
//			DateFormat.is24HourFormat(requireContext())
//		).show()
	}

	private companion object {
		private const val TAG = "DATE_TIME"
	}
}