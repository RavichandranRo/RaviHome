package com.example.ravihome.ui.planned

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.databinding.FragmentPlannedWorksBinding
import com.example.ravihome.ui.adapter.PlannedWorksAdapter
import com.example.ravihome.ui.export.ExportDialog
import com.example.ravihome.ui.export.ExportFormat
import com.example.ravihome.ui.export.ExportUtils
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class PlannedWorksFragment : Fragment() {

    private lateinit var binding: FragmentPlannedWorksBinding
    private val viewModel: PlannedWorksViewModel by viewModels()

    private lateinit var adapter: PlannedWorksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlannedWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter = PlannedWorksAdapter(
            onComplete = { viewModel.markCompleted(it) },
            onDelete = { confirmDelete(it) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Date picker (add)
        binding.etDate.setOnClickListener {
            showDatePicker { binding.etDate.setText(it) }
        }

        // Date filter
        binding.tvFilterDate.setOnClickListener {
            showDatePicker {
                binding.tvFilterDate.text = it
                viewModel.setDate(it)
            }
        }

        // Save
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val date = binding.etDate.text.toString().trim()

            when {
                title.isBlank() ->
                    Snackbar.make(binding.root, "Title is required", Snackbar.LENGTH_SHORT).show()

                date.isBlank() ->
                    Snackbar.make(binding.root, "Date is required", Snackbar.LENGTH_SHORT).show()

                else -> {
                    viewModel.addPlannedWork(title, date, desc)

                    Snackbar.make(binding.root, "Planned work saved", Snackbar.LENGTH_SHORT).show()

                    binding.etTitle.text?.clear()
                    binding.etDescription.text?.clear()
                    binding.etDate.text?.clear()
                }
            }
        }

        // Export (filtered list respected ✔️)
        binding.btnExport.setOnClickListener {
            ExportDialog.show(requireContext()) { _, format ->
                val rows = adapter.currentList.map {
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

        // Observe planned list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredWorks.collect { list ->
                adapter.submitList(list)

                binding.emptyState.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Swipe-to-delete with UNDO (correct)
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val work = adapter.currentList[viewHolder.adapterPosition]

                viewModel.deleteWork(work.id)

                Snackbar.make(binding.root, "Work deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.restoreWork(work)
                    }
                    .show()
            }

        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun confirmDelete(work: WorkEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete work?")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteWork(work.id)

                Snackbar.make(binding.root, "Work deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.addPlannedWork("work","work","work")
                    }
                    .show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                adapter.notifyDataSetChanged()
            }
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