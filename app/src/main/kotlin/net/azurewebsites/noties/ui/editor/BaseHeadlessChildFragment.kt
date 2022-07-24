package net.azurewebsites.noties.ui.editor

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

open class BaseHeadlessChildFragment : Fragment() {
	protected val viewModel by viewModels<EditorViewModel>({ requireParentFragment() })
}