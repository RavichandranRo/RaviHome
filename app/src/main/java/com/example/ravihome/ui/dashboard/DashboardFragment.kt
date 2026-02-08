package com.example.ravihome.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.ravihome.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding: FragmentDashboardBinding
        get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
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
        _binding =
            FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvWelcome.text = greetingMessage()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.summary.collect { s ->
                    setupPieChart(s)
                    setupBarChart(s)
                }
            }
        }
        binding.btnGoEb.setOnClickListener {
            findNavController().navigate(com.example.ravihome.R.id.ebFragment)
        }

        binding.btnGoTravel.setOnClickListener {
            findNavController().navigate(com.example.ravihome.R.id.travelFragment)
        }

        binding.btnGoPayments.setOnClickListener {
            findNavController().navigate(com.example.ravihome.R.id.paymentFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupPieChart(s: DashboardSummary) {
        val entries = listOf(
            PieEntry(s.planned.toFloat(), "Planned"),
            PieEntry(s.completed.toFloat(), "Completed")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(Color.BLUE, Color.GREEN)

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    private fun setupBarChart(s: DashboardSummary) {
        val entries = listOf(
            BarEntry(0f, s.planned.toFloat()),
            BarEntry(1f, s.completed.toFloat())
        )

        val dataSet = BarDataSet(entries, "Works")
        dataSet.colors = listOf(Color.BLUE, Color.GREEN)

        binding.barChart.data = BarData(dataSet)
        binding.barChart.invalidate()
    }

    private fun greetingMessage(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}

