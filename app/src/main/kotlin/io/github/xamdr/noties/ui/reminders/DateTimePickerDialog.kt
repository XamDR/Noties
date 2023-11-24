package io.github.xamdr.noties.ui.reminders

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.components.DropDown
import io.github.xamdr.noties.ui.components.TimePickerDialog
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
	reminderDate: Long?,
	onReminderDateSet: (Instant) -> Unit,
	onCancelReminder: () -> Unit,
	onDismiss: () -> Unit,
	viewModel: DateTimePickerViewModel = hiltViewModel()
) {
	val helper = DateTimePickerHelper()
	var isConfirmButtonEnabled by rememberSaveable { mutableStateOf(value = false) }
	val reminderDateState by viewModel.reminderState.collectAsStateWithLifecycle(initialValue = ReminderDateState.ReminderDateNotSet)
	var selectedDate by rememberSaveable { mutableStateOf(value = reminderDate?.toLocalDate()) }
	var selectedTime by rememberSaveable { mutableStateOf(value = reminderDate?.toLocalTime()) }
	var openDateDialog by rememberSaveable { mutableStateOf(value = false) }
	var openTimeDialog by rememberSaveable { mutableStateOf(value = false) }

	isConfirmButtonEnabled = when (reminderDateState) {
		ReminderDateState.ReminderDateInvalid -> false
		ReminderDateState.ReminderDateNotSet -> false
		ReminderDateState.ReminderDateValid -> true
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		dismissButton = {
			Row(
				modifier = if (reminderDate != null) Modifier.fillMaxWidth(fraction = 0.75f) else Modifier,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				if (reminderDate != null) {
					TextButton(onClick = onCancelReminder) {
						Text(text = stringResource(id = R.string.delete_button))
					}
				}
				TextButton(onClick = onDismiss) {
					Text(text = stringResource(id = R.string.cancel_button))
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = { onReminderDateSet(viewModel.onDateTimeSet(selectedDate, selectedTime)) },
				enabled = isConfirmButtonEnabled
			) {
				Text(text = stringResource(id = R.string.ok_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.reminder)) },
		text = {
			Column {
				DropDown(
					hint = stringResource(id = R.string.date_hint),
					value = selectedDate.asString(),
					items = helper.dates,
					entries = stringArrayResource(id = R.array.reminder_date_options),
					onItemClick = { date ->
						if (date == null) {
							openDateDialog = true
						}
						else {
							selectedDate = date
							viewModel.onReminderDateSelected(selectedDate, selectedTime)
						}
					},
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
				)
				DropDown(
					hint = stringResource(id = R.string.time_hint),
					value = selectedTime.asString(),
					items = helper.times,
					entries = stringArrayResource(id = R.array.reminder_time_options),
					onItemClick = { time ->
						if (time == null) {
							openTimeDialog = true
						}
						else {
							selectedTime = time
							viewModel.onReminderDateSelected(selectedDate, selectedTime)
						}
					},
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
				)
			}
			if (openDateDialog) {
				val selection = if (reminderDate == 0L) null else reminderDate
				val start = LocalDate.now().year
				val datePickerState = rememberDatePickerState(
					initialSelectedDateMillis = selection,
					yearRange = IntRange(start, DatePickerDefaults.YearRange.last)
				)
				val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != selection } }
				DatePickerDialog(
					onDismissRequest = { openDateDialog = false },
					dismissButton = {
						TextButton(onClick = { openDateDialog = false }) {
							Text(text = stringResource(id = R.string.cancel_button))
						}
					},
					confirmButton = {
						TextButton(
							onClick = {
								openDateDialog = false
								selectedDate = datePickerState.selectedDateMillis?.let { viewModel.onDateSet(it) }
								viewModel.onReminderDateSelected(selectedDate, selectedTime)
							},
							enabled = confirmEnabled
						) {
							Text(text = stringResource(id = R.string.ok_button))
						}
					}
				) {
					DatePicker(state = datePickerState)
				}
			}
			if (openTimeDialog) {
				val context = LocalContext.current
				val isSystem24Hour = DateFormat.is24HourFormat(context)
				val epochMilli = reminderDate ?: Instant.now().toEpochMilli()
				val localTime = LocalTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault())
				val timePickerState = rememberTimePickerState(
					initialHour = localTime.hour,
					initialMinute = localTime.minute,
					is24Hour = isSystem24Hour
				)
				TimePickerDialog(
					onDismissRequest = { openTimeDialog = false },
					dismissButton = {
						TextButton(onClick = { openTimeDialog = false }) {
							Text(text = stringResource(id = R.string.cancel_button))
						}
					},
					confirmButton = {
						TextButton(
							onClick = {
								openTimeDialog = false
								selectedTime = viewModel.onTimeSet(timePickerState.hour, timePickerState.minute)
								viewModel.onReminderDateSelected(selectedDate, selectedTime)
							}
						) {
							Text(text = stringResource(id = R.string.ok_button))
						}
					}
				) {
					TimePicker(state = timePickerState)
				}
			}
		}
	)
}

@Composable
private fun DateTimePickerDialog() {
	AlertDialog(
		onDismissRequest = {},
		dismissButton = {
			Row(
				modifier = Modifier.fillMaxWidth(fraction = 0.75f),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				TextButton(onClick = {}) {
					Text(text = stringResource(id = R.string.delete_button))
				}
				TextButton(onClick = {}) {
					Text(text = stringResource(id = R.string.cancel_button))
				}
			}
		},
		confirmButton = {
			TextButton(onClick = {}) {
				Text(text = stringResource(id = R.string.ok_button))
			}
		},
		title = { Text(text = stringResource(id = R.string.reminder)) },
		text = {
			Column {
				DropDown(
					hint = stringResource(id = R.string.date_hint),
					value = "",
					items = emptyList<Unit>(),
					entries = emptyArray(),
					onItemClick = {},
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
				)
				DropDown(
					hint = stringResource(id = R.string.time_hint),
					value = "",
					items = emptyList<Unit>(),
					entries = emptyArray(),
					onItemClick = {},
					modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
				)
			}
		}
	)
}

@DevicePreviews
@Composable
private fun DateTimePickerDialogPreview() = NotiesTheme { DateTimePickerDialog() }