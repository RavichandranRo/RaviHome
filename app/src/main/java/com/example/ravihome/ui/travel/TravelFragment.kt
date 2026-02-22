package com.example.ravihome.ui.travel

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ravihome.databinding.FragmentTravelBinding
import com.example.ravihome.ui.export.ExportUtils
import com.example.ravihome.ui.util.LocalHistoryStore
import com.example.ravihome.ui.util.PopupUtils
import com.example.ravihome.ui.util.ViewAllDialogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class TravelFragment : Fragment() {

    private lateinit var binding: FragmentTravelBinding
    private val viewModel: TravelViewModel by viewModels()

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
        binding = FragmentTravelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var reminderDate: LocalDate? = null
        fun refreshRecent() {
            val items = LocalHistoryStore.list(requireContext(), "travel_tickets")
            binding.tvRecent.text =
                if (items.isEmpty()) "No recent tickets" else items.take(3).joinToString("\n")
        }

        refreshRecent()
        binding.btnViewAll.setOnClickListener {
            ViewAllDialogUtils.show(
                requireContext(),
                "All saved tickets",
                LocalHistoryStore.list(requireContext(), "travel_tickets")
            )
        }

        binding.btnPrintTicket.setOnClickListener {
            val status = viewModel.uiState.value.status
            if (status == null) {
                PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Missing ticket",
                    "Fetch a ticket before printing."
                )
            } else {
                val rows = listOf(
                    listOf("PNR", "Train/Bus", "From", "To", "Date", "Status", "Coach", "Seat"),
                    listOf(
                        status.pnr,
                        status.trainName,
                        status.from,
                        status.to,
                        status.date,
                        status.status,
                        status.coach,
                        status.seat
                    )
                )
                ExportUtils.exportPdf(requireContext(), "travel_ticket_${status.pnr}", rows)
                PopupUtils.showAutoDismiss(
                    requireContext(),
                    "Printed",
                    "Ticket PDF generated in app storage."
                )
            }
        }
        binding.btnFetch.setOnClickListener {
            val pnr = binding.etPnr.text?.toString()?.trim().orEmpty()
            viewModel.fetchPnrStatus(pnr)
        }
        binding.toggleTrips.check(binding.btnUpcoming.id)
        binding.toggleTrips.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val message = if (checkedId == binding.btnUpcoming.id) {
                "Showing upcoming trips."
            } else {
                "Showing completed trips."
            }
            PopupUtils.showAutoDismiss(requireContext(), "Trips filter", message)
        }

        binding.btnTripReminder.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    reminderDate = LocalDate.of(y, m + 1, d)
                    binding.tvTripReminder.text = "Reminder: ${reminderDate}"
                    PopupUtils.showAutoDismiss(
                        requireContext(),
                        "Reminder set",
                        "Upcoming trip reminder saved."
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.cbCancelled.setOnCheckedChangeListener { _, checked ->
            binding.btnRefunded.visibility = if (checked) View.VISIBLE else View.GONE
        }

        binding.btnRefunded.setOnClickListener {
            val dialogView = layoutInflater.inflate(
                com.example.ravihome.R.layout.dialog_refund,
                null
            )
            val refundMode = dialogView.findViewById<android.widget.EditText>(
                com.example.ravihome.R.id.etRefundMode
            )
            val refundAmount = dialogView.findViewById<android.widget.EditText>(
                com.example.ravihome.R.id.etRefundAmount
            )
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Refund details")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val mode = refundMode.text?.toString()?.trim().orEmpty()
                    val amount = refundAmount.text?.toString()?.trim().orEmpty()
                    PopupUtils.showAutoDismiss(
                        requireContext(),
                        "Refund saved",
                        "Mode: $mode • Amount: $amount"
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progress.isVisible = state.isLoading
                binding.cardStatus.isVisible = state.status != null

                state.status?.let { status ->
                    LocalHistoryStore.append(
                        requireContext(),
                        "travel_tickets",
                        "${status.trainName} • ${status.pnr} • ${status.from}→${status.to} • ${status.seat}"
                    )
                    refreshRecent()
                    binding.tvTrain.text = "${status.trainName} (${status.pnr})"
                    binding.tvRoute.text = "${status.from} ➜ ${status.to}"
                    binding.tvDate.text = "Journey: ${status.date}"
                    binding.tvStatus.text = "Status: ${status.status}"
                    binding.tvCoach.text = "Coach: ${status.coach}  Seat: ${status.seat}"
                    binding.tvChart.text = if (status.chartPrepared) {
                        "Chart prepared"
                    } else {
                        "Chart not prepared"
                    }
                }

                binding.tvStats.text = if (state.stats.totalChecks == 0) {
                    "No tickets checked yet"
                } else {
                    "Checked: ${state.stats.totalChecks} • Confirmed: ${state.stats.confirmed} • Waitlist: ${state.stats.waitlist}"
                }

                state.message?.let { message ->
                    PopupUtils.showAutoDismiss(
                        requireContext(),
                        "Ticket update",
                        message
                    )
                }
            }
        }
    }
}