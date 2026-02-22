package com.example.ravihome.ui.deposits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ravihome.databinding.FragmentDepositFixedBinding
import com.example.ravihome.ui.export.ExportDialog
import com.example.ravihome.ui.export.ExportFormat
import com.example.ravihome.ui.export.ExportUtils
import com.example.ravihome.ui.util.PopupUtils
import com.example.ravihome.ui.util.ViewAllDialogUtils
import com.example.ravihome.ui.util.VoiceInputHelper
import kotlinx.coroutines.launch

class FixedDepositFragment : Fragment() {

    private lateinit var binding: FragmentDepositFixedBinding
    private val viewModel: FixedDepositViewModel by viewModels()
    private val voiceInputHelper by lazy { VoiceInputHelper(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDepositFixedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DepositAdapter(
            maturityCalculator = {
                DepositCalculator.fixedMaturity(it.amount, it.durationMonths, it.rate)
            },
            onEdit = { entry ->
                DepositEditDialog.show(
                    requireContext(),
                    entry,
                    onSave = viewModel::update
                )
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        fun recalcMaturity() {
            val amount = binding.etAmount.text?.toString()?.toDoubleOrNull()
            val duration = binding.etDuration.text?.toString()?.toIntOrNull()
            val rate = binding.etRate.text?.toString()?.toDoubleOrNull()
            if (amount == null || duration == null || rate == null) {
                binding.tvMaturity.text = "Maturity amount: --"
                return
            }
            val maturity = DepositCalculator.fixedMaturity(amount, duration, rate)
            binding.tvMaturity.text = "Maturity amount: ₹%.2f".format(maturity)
        }

        binding.etAmount.addTextChangedListener { recalcMaturity() }
        binding.etDuration.addTextChangedListener { recalcMaturity() }
        binding.etRate.addTextChangedListener { recalcMaturity() }
        voiceInputHelper.attachTo(binding.etAmount, "Speak amount")
        voiceInputHelper.attachTo(binding.etDuration, "Speak duration in months")
        voiceInputHelper.attachTo(binding.etRate, "Speak rate of interest")
        voiceInputHelper.attachTo(binding.etBank, "Speak bank name")

        binding.btnSave.setOnClickListener {
            val amount = binding.etAmount.text?.toString()?.toDoubleOrNull()
            val duration = binding.etDuration.text?.toString()?.toIntOrNull()
            val rate = binding.etRate.text?.toString()?.toDoubleOrNull()
            val bank = binding.etBank.text?.toString()?.trim().orEmpty()

            when {
                amount == null || amount <= 0 -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing amount",
                    "Enter a valid deposit amount."
                )

                duration == null || duration <= 0 -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing duration",
                    "Enter duration in months."
                )

                rate == null || rate <= 0 -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing rate",
                    "Enter a valid interest rate."
                )

                bank.isBlank() -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing bank",
                    "Enter the bank name."
                )

                else -> {
                    viewModel.add(
                        DepositEntry(
                            id = System.currentTimeMillis(),
                            amount = amount,
                            durationMonths = duration,
                            rate = rate,
                            bank = bank,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    PopupUtils.showAutoDismiss(
                        requireContext(),
                        "Saved",
                        "Fixed deposit added."
                    )
                    binding.etAmount.text?.clear()
                    binding.etDuration.text?.clear()
                    binding.etRate.text?.clear()
                    binding.etBank.text?.clear()
                    binding.tvMaturity.text = "Maturity amount: --"
                }
            }
        }

        binding.btnExport.setOnClickListener {
            ExportDialog.show(requireContext()) { _, format ->
                val rows = buildList {
                    add(listOf("Bank", "Amount", "Duration (months)", "Rate (%)", "Maturity"))
                    addAll(adapter.currentList.map {
                        listOf(
                            it.bank,
                            it.amount.toString(),
                            it.durationMonths.toString(),
                            it.rate.toString(),
                            "%.2f".format(
                                DepositCalculator.fixedMaturity(
                                    it.amount,
                                    it.durationMonths,
                                    it.rate
                                )
                            )
                        )
                    })
                }

                when (format) {
                    ExportFormat.CSV -> ExportUtils.exportCsv(
                        requireContext(),
                        "fixed_deposits",
                        rows
                    )

                    ExportFormat.EXCEL -> ExportUtils.exportExcel(
                        requireContext(),
                        "fixed_deposits",
                        rows
                    )

                    ExportFormat.PDF -> ExportUtils.exportPdf(
                        requireContext(),
                        "fixed_deposits",
                        rows
                    )

                    ExportFormat.HTML -> ExportUtils.exportHtml(
                        requireContext(),
                        "fixed_deposits",
                        rows
                    )
                }
            }
        }

        binding.btnViewAll.setOnClickListener {
            ViewAllDialogUtils.show(
                requireContext(),
                "All fixed deposits",
                adapter.currentList.map { "${it.bank} • ₹%.2f".format(it.amount) }
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { list ->
                adapter.submitList(list)
                binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.tvRecent.text = if (list.isEmpty()) {
                    "No recent deposits"
                } else {
                    list.take(3).joinToString("\n") {
                        "${it.bank} • ₹%.2f".format(it.amount)
                    }
                }
            }
        }
    }
}