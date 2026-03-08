package com.example.ravihome.ui.deposits

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Switch
import com.example.ravihome.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DepositEditDialog {
    fun show(
        context: Context,
        entry: DepositEntry,
        onSave: (DepositEntry) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_deposit_edit, null)
        val numberInput = dialogView.findViewById<EditText>(R.id.etDepositNumber)
        val amountInput = dialogView.findViewById<EditText>(R.id.etAmount)
        val startDateInput = dialogView.findViewById<EditText>(R.id.etStartDate)
        val maturityDateInput = dialogView.findViewById<EditText>(R.id.etMaturityDate)
        val rateInput = dialogView.findViewById<EditText>(R.id.etRate)
        val bankInput = dialogView.findViewById<EditText>(R.id.etBank)
        val prematureSwitch = dialogView.findViewById<Switch>(R.id.switchPremature)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        var startDateMillis = entry.startDateMillis
        var maturityDateMillis = entry.maturityDateMillis

        numberInput.setText(entry.depositNumber)
        amountInput.setText(entry.amount.toString())
        startDateInput.setText(formatter.format(Date(entry.startDateMillis)))
        maturityDateInput.setText(formatter.format(Date(entry.maturityDateMillis)))
        rateInput.setText(entry.rate.toString())
        bankInput.setText(entry.bank)

        prematureSwitch.isChecked = entry.isPremature

        startDateInput.setOnClickListener {
            showDatePicker(context, startDateMillis) {
                startDateMillis = it
                startDateInput.setText(formatter.format(Date(it)))
            }
        }
        maturityDateInput.setOnClickListener {
            showDatePicker(context, maturityDateMillis) {
                maturityDateMillis = it
                maturityDateInput.setText(formatter.format(Date(it)))
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Edit deposit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updated = entry.copy(
                    depositNumber = numberInput.text?.toString()?.trim().orEmpty()
                        .ifBlank { entry.depositNumber },
                    amount = amountInput.text?.toString()?.toDoubleOrNull() ?: entry.amount,
                    startDateMillis = startDateMillis,
                    maturityDateMillis = maturityDateMillis,
                    rate = rateInput.text?.toString()?.toDoubleOrNull() ?: entry.rate,
                    bank = bankInput.text?.toString()?.trim().orEmpty().ifBlank { entry.bank },
                    isPremature = prematureSwitch.isChecked
                )
                onSave(updated)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(
        context: Context,
        initialMillis: Long,
        onDateSelected: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val selected = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selected.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}