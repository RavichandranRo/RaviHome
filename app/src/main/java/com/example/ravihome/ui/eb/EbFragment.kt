package com.example.ravihome.ui.eb

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ravihome.databinding.FragmentEbBinding
import com.example.ravihome.ui.util.PopupUtils
import com.example.ravihome.ui.util.ViewAllDialogUtils
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class EbFragment : Fragment() {

    private lateinit var binding: FragmentEbBinding
    private val viewModel: EbViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var latestAmount: Float? = null
        var reminderDate: LocalDate? = null

        fun recalc() {
            val prev = binding.etPrevious.text.toString().toFloatOrNull()
            val curr = binding.etCurrent.text.toString().toFloatOrNull()
            binding.tilCurrent.error = null
            latestAmount = null
            binding.btnRecordPayment.isEnabled = false

            if (prev == null || curr == null) {
                binding.tvUnits.text = "Units: --"
                binding.tvAmount.text = "Amount: --"
                return
            }

            val units = curr - prev
            if (units < 0) {
                binding.tilCurrent.error = "Current reading must be greater than previous"
                binding.tvUnits.text = "Units: --"
                binding.tvAmount.text = "Amount: --"
                return
            }

            val amount = viewModel.calculate(units)
            latestAmount = amount
            binding.btnRecordPayment.isEnabled = amount > 0f
            binding.tvUnits.text = "Units: %.1f".format(units)
            binding.tvAmount.text = "Amount: ₹%.2f".format(amount)
        }


        binding.etPrevious.addTextChangedListener { recalc() }
        binding.etCurrent.addTextChangedListener { recalc() }

        binding.btnRecordPayment.setOnClickListener {
            val amount = latestAmount ?: return@setOnClickListener
            viewModel.recordPayment(amount)
            PopupUtils.showAutoDismiss(
                requireContext(),
                "EB payment saved",
                "Payment recorded successfully."
            )
        }

        binding.switchBillGenerated.setOnCheckedChangeListener { _, checked ->
            val message = if (checked) {
                "Bill marked as generated."
            } else {
                "Bill status cleared."
            }
            PopupUtils.showAutoDismiss(requireContext(), "EB bill status", message)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ebHistory.collect { entries ->
                binding.tvRecent.text = if (entries.isEmpty()) {
                    "No recent EB entries"
                } else {
                    entries.take(3)
                        .joinToString("\n") { "${it.title} • ₹%.2f".format(it.amount ?: 0.0) }
                }
            }
        }

        binding.btnViewAll.setOnClickListener {
            ViewAllDialogUtils.show(
                requireContext(),
                "All saved EB items",
                viewModel.ebHistory.value.map { "${it.title} • ₹%.2f".format(it.amount ?: 0.0) }
            )
        }
        binding.btnSetReminder.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    reminderDate = LocalDate.of(y, m + 1, d)
                    binding.tvReminder.text = "Reminder: ${reminderDate}"
                    PopupUtils.showAutoDismiss(
                        requireContext(),
                        "Reminder set",
                        "EB payment reminder saved."
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}
