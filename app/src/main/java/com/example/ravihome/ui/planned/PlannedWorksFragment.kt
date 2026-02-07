package com.example.ravihome.ui.planned

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.R
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.databinding.FragmentPlannedWorksBinding
import com.example.ravihome.ui.adapter.PlannedWorksAdapter
import com.example.ravihome.ui.export.ExportDialog
import com.example.ravihome.ui.export.ExportFormat
import com.example.ravihome.ui.export.ExportUtils
import com.example.ravihome.ui.util.DateFormatUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@AndroidEntryPoint
class PlannedWorksFragment : Fragment() {

    private lateinit var binding: FragmentPlannedWorksBinding
    private val viewModel: PlannedWorksViewModel by viewModels()

    private lateinit var adapter: PlannedWorksAdapter

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
            showDatePicker { binding.etDate.setText(DateFormatUtils.formatInput(it)) }
        }

        // Date filter
        binding.tvFilterDate.setOnClickListener {
            showDatePicker { date ->
                binding.tvFilterDate.text = DateFormatUtils.formatDisplay(date)
                viewModel.setDate(date)
            }
        }
        binding.btnClearFilter.setOnClickListener {
            binding.tvFilterDate.text = getString(R.string.all_dates)
            viewModel.setDate(null)
        }

        binding.etSearch.addTextChangedListener { text ->
            viewModel.setKeyword(text?.toString())
        }

        // Save
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val desc = binding.etDescription.text.toString().trim()
            val dateText = binding.etDate.text.toString().trim()
            val date = DateFormatUtils.parseInput(dateText)

            when {
                title.isBlank() ->
                    Snackbar.make(binding.root, "Title is required", Snackbar.LENGTH_SHORT).show()

                date == null ->
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
                    listOf(it.title, DateFormatUtils.formatDisplay(it.date), it.description)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentPlannedWorks.collect { list ->
                binding.tvRecent.text = if (list.isEmpty()) {
                    "No recent planned works"
                } else {
                    list.joinToString("\n") {
                        "${it.title} • ${DateFormatUtils.formatDisplay(it.date)}"
                    }
                }
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
                        viewModel.restoreWork(work)
                    }
                    .show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> onDateSelected(LocalDate.of(y, m + 1, d)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}