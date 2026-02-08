package com.example.ravihome.ui.planned

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.ravihome.databinding.BottomSheetPlannedWorkBinding
import com.example.ravihome.ui.util.DateFormatUtils
import com.example.ravihome.ui.util.PopupUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.util.Calendar

class PlannedBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPlannedWorkBinding? = null
    private val binding: BottomSheetPlannedWorkBinding
        get() = _binding!!

    var onSave: ((String, LocalDate, String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPlannedWorkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etDate.setOnClickListener {
            showDatePicker { binding.etDate.setText(DateFormatUtils.formatInput(it)) }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val dateText = binding.etDate.text.toString().trim()
            val date = DateFormatUtils.parseInput(dateText)

            when {
                title.isBlank() -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing title",
                    "Please enter a title."
                )

                date == null -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing date",
                    "Please select a date."
                )

                else -> {
                    onSave?.invoke(title, date, desc)
                    dismiss()
                }
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.etTitle.addTextChangedListener {
            if (it?.isNotBlank() == true) {
                binding.inputTitle.error = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> onDateSelected(LocalDate.of(y, m + 1, d)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}