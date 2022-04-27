package net.azurewebsites.eznotes.ui.notes

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

// Based on these articles:
// https://www.journaldev.com/23164/android-recyclerview-swipe-to-delete-undo
// https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
class SwipeToDeleteCallback(private val adapter: NoteAdapter) :
	ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {

	private val background = ColorDrawable()
	private val backgroundColor = Color.parseColor("#f44336")
	private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

	override fun onMove(
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		target: RecyclerView.ViewHolder
	): Boolean = false

	override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
		if (viewHolder is NoteAdapter.NoteViewHolder) {
			val position = viewHolder.bindingAdapterPosition
			adapter.deleteNote(position)
		}
	}

	override fun onChildDraw(
		canvas: Canvas,
		recyclerView: RecyclerView,
		viewHolder: RecyclerView.ViewHolder,
		dX: Float,
		dY: Float,
		actionState: Int,
		isCurrentlyActive: Boolean
	) {
		val itemView = viewHolder.itemView
		val isCanceled = dX == 0f && !isCurrentlyActive

		if (isCanceled) {
			clearCanvas(canvas, itemView.right + dX, itemView.top.toFloat(),
				itemView.right.toFloat(), itemView.bottom.toFloat()
			)
			super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
			return
		}
		background.color = backgroundColor

		if (dX >= 0.0f) {
			// Draw the red delete background on the left
			background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
		}
		else {
			// Draw the red delete background on the right
			background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
		}
		background.draw(canvas)
		super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
	}

	private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
		c?.drawRect(left, top, right, bottom, clearPaint)
	}
}