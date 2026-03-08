package com.example.ravihome.ui.deposits

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

class FixedDepositFragment : Fragment() {
    private data class ExportRequest(val format: ExportFormat, val rows: List<List<String>>)

    private lateinit var binding: FragmentDepositFixedBinding
    private val viewModel: FixedDepositViewModel by viewModels()
    private val voiceInputHelper by lazy { VoiceInputHelper(this) }
    private var pendingExport: ExportRequest? = null
    private val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var startDateMillis = System.currentTimeMillis()
    private var maturityDateMillis = System.currentTimeMillis()

    private val createExportFile =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            val request = pendingExport ?: return@registerForActivityResult
            if (uri == null) return@registerForActivityResult
            ExportUtils.exportToUri(requireContext(), uri, request.format, request.rows)
            PopupUtils.showAutoDismiss(
                requireContext(),
                "Export complete",
                "Saved to selected path"
            )
            pendingExport = null
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDepositFixedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etStartDate.setText(formatter.format(Date(startDateMillis)))
        binding.etMaturityDate.setText(formatter.format(Date(maturityDateMillis)))

        val adapter = DepositAdapter(
            maturityCalculator = {
                val durationDays = max(
                    1,
                    ((it.maturityDateMillis - it.startDateMillis) / (24 * 60 * 60 * 1000)).toInt()
                )
                DepositCalculator.fixedMaturity(it.amount, durationDays, it.rate)
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

        var recalcMaturity: () -> Unit = {}

        binding.etStartDate.setOnClickListener {
            showDatePicker(startDateMillis) {
                startDateMillis = it
                binding.etStartDate.setText(formatter.format(Date(it)))
                recalcMaturity()
            }
        }
        binding.etMaturityDate.setOnClickListener {
            showDatePicker(maturityDateMillis) {
                maturityDateMillis = it
                binding.etMaturityDate.setText(formatter.format(Date(it)))
                recalcMaturity()
            }
        }

        fun recalcMaturityInner() {
            val amount = binding.etAmount.text?.toString()?.toDoubleOrNull()
            val rate = binding.etRate.text?.toString()?.toDoubleOrNull()
            if (amount == null || rate == null || maturityDateMillis <= startDateMillis) {
                binding.tvMaturity.text = "Maturity amount: --"
                return
            }
            val durationDays =
                max(1, ((maturityDateMillis - startDateMillis) / (24 * 60 * 60 * 1000)).toInt())
            val maturity = DepositCalculator.fixedMaturity(amount, durationDays, rate)
            binding.tvMaturity.text = "Maturity amount: ₹%.2f".format(maturity)
        }

        fun resetDates() {
            startDateMillis = System.currentTimeMillis()
            maturityDateMillis = startDateMillis
            binding.etStartDate.setText(formatter.format(Date(startDateMillis)))
            binding.etMaturityDate.setText(formatter.format(Date(maturityDateMillis)))
        }
        recalcMaturity = ::recalcMaturityInner

        binding.etAmount.addTextChangedListener { recalcMaturityInner() }
        binding.etRate.addTextChangedListener { recalcMaturityInner() }
        voiceInputHelper.attachTo(binding.etDepositNumber, "Speak deposit number")
        voiceInputHelper.attachTo(binding.etAmount, "Speak amount")
        voiceInputHelper.attachTo(binding.etRate, "Speak rate of interest")
        voiceInputHelper.attachTo(binding.etBank, "Speak bank name")

        binding.btnSave.setOnClickListener {
            val number = binding.etDepositNumber.text?.toString()?.trim().orEmpty()
            val amount = binding.etAmount.text?.toString()?.toDoubleOrNull()
            val rate = binding.etRate.text?.toString()?.toDoubleOrNull()
            val bank = binding.etBank.text?.toString()?.trim().orEmpty()

            when {
                number.isBlank() -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing number",
                    "Enter a deposit number."
                )

                amount == null || amount <= 0 -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing amount",
                    "Enter a valid deposit amount."
                )

                maturityDateMillis <= startDateMillis -> PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Invalid dates",
                    "Maturity date should be after start date."
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
                            depositNumber = number,
                            amount = amount,
                            startDateMillis = startDateMillis,
                            maturityDateMillis = maturityDateMillis,
                            rate = rate,
                            bank = bank,
                            isPremature = binding.switchPremature.isChecked,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    PopupUtils.showAutoDismiss(requireContext(), "Saved", "Fixed deposit added.")
                    binding.etDepositNumber.text?.clear()
                    binding.etAmount.text?.clear()
                    binding.etRate.text?.clear()
                    binding.etBank.text?.clear()
                    binding.switchPremature.isChecked = false
                    binding.tvMaturity.text = "Maturity amount: --"
                    resetDates()
                }
            }
        }

        binding.btnExport.setOnClickListener {
            ExportDialog.show(requireContext()) { _, format ->
                val rows = buildList {
                    add(
                        listOf(
                            "Deposit Number",
                            "Bank",
                            "Amount",
                            "Start Date",
                            "Maturity Date",
                            "Premature",
                            "Rate (%)",
                            "Maturity"
                        )
                    )
                    addAll(adapter.currentList.map {
                        val durationDays = max(
                            1,
                            ((it.maturityDateMillis - it.startDateMillis) / (24 * 60 * 60 * 1000)).toInt()
                        )
                        listOf(
                            it.depositNumber,
                            it.bank,
                            it.amount.toString(),
                            formatter.format(Date(it.startDateMillis)),
                            formatter.format(Date(it.maturityDateMillis)),
                            it.isPremature.toString(),
                            it.rate.toString(),
                            "%.2f".format(
                                DepositCalculator.fixedMaturity(
                                    it.amount,
                                    durationDays,
                                    it.rate
                                )
                            )
                        )
                    })
                }
                pendingExport = ExportRequest(format, rows)
                createExportFile.launch("fixed_deposits.${ExportUtils.extensionFor(format)}")
            }
        }

        binding.btnViewAll.setOnClickListener {
            ViewAllDialogUtils.show(
                requireContext(),
                "All fixed deposits",
                adapter.currentList.map { "${it.depositNumber} • ${it.bank} • ₹%.2f".format(it.amount) })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { list ->
                adapter.submitList(list)
                binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.tvRecent.text = if (list.isEmpty()) "No recent deposits" else list.take(3)
                    .joinToString("\n") { "${it.depositNumber} • ${it.bank} • ₹%.2f".format(it.amount) }
            }
        }
    }


    private fun showDatePicker(initialMillis: Long, onDateSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val selected = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selected.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}