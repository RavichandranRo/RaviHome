package com.example.ravihome.ui.travel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ravihome.databinding.FragmentTravelBinding
import com.example.ravihome.ui.util.PopupUtils
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        binding.btnFetch.setOnClickListener {
            val pnr = binding.etPnr.text?.toString()?.trim().orEmpty()
            viewModel.fetchPnrStatus(pnr)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progress.isVisible = state.isLoading
                binding.cardStatus.isVisible = state.status != null

                state.status?.let { status ->
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