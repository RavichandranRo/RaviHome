package com.example.ravihome.ui.planned

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.core.widget.*
import com.example.ravihome.databinding.FragmentPlannedWorksBinding
import com.example.ravihome.ui.adapter.PlannedWorksAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.ui.export.ExportDialog
import com.example.ravihome.ui.export.ExportFormat
import com.example.ravihome.ui.export.ExportUtils

@AndroidEntryPoint
class PlannedWorksFragment : Fragment() {

    private lateinit var binding: FragmentPlannedWorksBinding
    private val viewModel: PlannedWorksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlannedWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlannedWorksAdapter(
            onComplete = { work ->
                viewModel.markCompleted(work)
            },
            onDelete = { work ->
                confirmDelete(work)
            }
        )

        binding.recyclerView.adapter = adapter

//        binding.etDate.setOnClickListener {
//            showDatePicker()
//        }
        binding.etSearch.addTextChangedListener {
            viewModel.setKeyword(it.toString().takeIf { txt -> txt.isNotBlank() })
        }

        binding.tvFilterDate.setOnClickListener {
            showDatePicker { date ->
                binding.tvFilterDate.text = date
                viewModel.setDate(date)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredWorks.collect { list ->
                adapter.submitList(list)
            }
        }


        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()

            if (title.isNotEmpty() && date.isNotEmpty()) {
                viewModel.addPlannedWork(title, date)
                binding.etTitle.text?.clear()
                binding.etDate.text?.clear()
            }
        }
        binding.btnExport.setOnClickListener {
            ExportDialog.show(requireContext()) { _, format ->
                val rows = viewModel.plannedWorks.value.map {
                    listOf(it.title, it.date, it.description)
                }

                when (format) {
                    ExportFormat.CSV -> ExportUtils.exportCsv(requireContext(), "planned", rows)
                    ExportFormat.EXCEL -> ExportUtils.exportExcel(requireContext(), "planned", rows)
                    ExportFormat.PDF -> ExportUtils.exportPdf(requireContext(), "planned", rows)
                    ExportFormat.HTML -> ExportUtils.exportHtml(requireContext(), "planned", rows)
                }
            }
        }

    }

    private fun confirmDelete(work: WorkEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete?")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteWork(work.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> onDateSelected("$d/${m + 1}/$y") },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

}