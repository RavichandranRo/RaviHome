package com.example.ravihome.ui.travel

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ravihome.R
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
    private val previousStatusByPnr = mutableMapOf<String, String>()

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
                "travel_tickets",
                LocalHistoryStore.list(requireContext(), "travel_tickets")
            )
        }
        binding.cardStatus.setOnClickListener {
            viewModel.uiState.value.status?.let { showTicketDetails(it) }
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
                    listOf("PNR", "Train", "From", "To", "Status", "Coach", "Seat"),
                    listOf(
                        status.pnr,
                        status.trainName,
                        status.from,
                        status.to,
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
            val dialogView = layoutInflater.inflate(R.layout.dialog_refund, null)
            val refundMode = dialogView.findViewById<android.widget.EditText>(R.id.etRefundMode)
            val refundAmount = dialogView.findViewById<android.widget.EditText>(R.id.etRefundAmount)
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
                    binding.tvStatus.text = "${status.status} • ${status.berthType}"
                    binding.tvCoach.text =
                        "${status.coach} • Seat ${status.seat} • Fare ₹${status.fare}"
                    binding.tvChart.text = if (status.chartPrepared) {
                        "Chart prepared"
                    } else {
                        "Chart not prepared"
                    }
                    applyStatusColor(status.status)
                    handleWaitlistToConfirmed(status)
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

    private fun applyStatusColor(status: String) {
        val color = when {
            status.contains("CONFIRMED", true) -> R.color.status_green
            status.contains("RAC", true) -> R.color.status_orange
            status.contains("WAIT", true) || status.contains("WL", true) -> R.color.status_red
            else -> android.R.color.darker_gray
        }
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun handleWaitlistToConfirmed(status: TravelStatus) {
        val current = status.status.uppercase()
        val previous = previousStatusByPnr[status.pnr]
        if (previous != null && (previous.contains("WAIT") || previous.contains("RAC")) && current.contains(
                "CONFIRMED"
            )
        ) {
            animateConfirmation()
            vibrateAndBeep()
            PopupUtils.showAutoDismiss(
                requireContext(),
                "Great news",
                "PNR ${status.pnr} moved to CONFIRMED"
            )
        }
        previousStatusByPnr[status.pnr] = current
    }

    private fun animateConfirmation() {
        ObjectAnimator.ofFloat(binding.cardStatus, View.SCALE_X, 1f, 1.04f, 1f).apply {
            duration = 450L
            start()
        }
        ObjectAnimator.ofFloat(binding.cardStatus, View.SCALE_Y, 1f, 1.04f, 1f).apply {
            duration = 450L
            start()
        }
    }

    private fun vibrateAndBeep() {
        val context = requireContext()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(250)
        }
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90).startTone(
            ToneGenerator.TONE_PROP_ACK,
            220
        )
    }

    private fun showTicketDetails(status: TravelStatus) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ticket_details, null)
        val body = dialogView.findViewById<TextView>(R.id.tvTicketBody)
        val print = dialogView.findViewById<AppCompatImageButton>(R.id.btnPrint)
        body.text = """
PNR: ${status.pnr}
Train: ${status.trainName}
From: ${status.from}    To: ${status.to}
Timing: ${status.departureTime} → ${status.arrivalTime}
Coach: ${status.coach}    Seat: ${status.seat}
Fare: ${status.fare}
Class: ${status.berthType}
Ticket Status: ${status.status}
Chart: ${if (status.chartPrepared) "Prepared" else "Not Prepared"}
        """.trimIndent()

        val rows = listOf(
            listOf(
                "PNR",
                "Train",
                "From",
                "To",
                "Timing",
                "Coach",
                "Seat",
                "Fare",
                "Class",
                "Status"
            ),
            listOf(
                status.pnr,
                status.trainName,
                status.from,
                status.to,
                "${status.departureTime}-${status.arrivalTime}",
                status.coach,
                status.seat,
                status.fare,
                status.berthType,
                status.status
            )
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ticket")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()

        print.setOnClickListener {
            ExportUtils.exportPdf(requireContext(), "travel_ticket_${status.pnr}", rows)
            PopupUtils.showAutoDismiss(
                requireContext(),
                "Printed",
                "Ticket PDF generated in app storage."
            )
        }
    }
}