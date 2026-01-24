package com.example.ravihome.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.ravihome.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collect { s ->
                setupPieChart(s)
                setupBarChart(s)
            }
        }
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
}

