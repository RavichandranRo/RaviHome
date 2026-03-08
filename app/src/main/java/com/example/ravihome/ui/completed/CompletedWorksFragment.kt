package com.example.ravihome.ui.completed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.databinding.FragmentCompletedWorksBinding
import com.example.ravihome.ui.adapter.CompletedWorksAdapter
import com.example.ravihome.ui.export.ExportDialog
import com.example.ravihome.ui.export.ExportFormat
import com.example.ravihome.ui.export.ExportUtils
import com.example.ravihome.ui.util.DateFormatUtils
import com.example.ravihome.ui.util.ViewAllDialogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompletedWorksFragment : Fragment() {
    private data class ExportRequest(val format: ExportFormat, val rows: List<List<String>>)

    private var pendingExport: ExportRequest? = null
    private val createExportFile =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            val request = pendingExport ?: return@registerForActivityResult
            if (uri != null) ExportUtils.exportToUri(
                requireContext(),
                uri,
                request.format,
                request.rows
            )
            pendingExport = null
        }
    private lateinit var binding: FragmentCompletedWorksBinding
    private val viewModel: CompletedWorksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompletedWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = CompletedWorksAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.completedWorks.collect {
                adapter.submitList(it)
                binding.emptyState.visibility =
                    if (it.isEmpty()) View.VISIBLE else View.GONE
                binding.tvRecent.text = if (it.isEmpty()) {
                    "No recent completed works"
                } else {
                    it.take(3).joinToString("\n") { work ->
                        "${work.title} • ${DateFormatUtils.formatDisplay(work.date)}"
                    }
                }
            }
        }

        binding.btnViewAll.setOnClickListener {
            ViewAllDialogUtils.show(
                requireContext(),
                "All completed works",
                adapter.currentList.map { "${it.title} • ${DateFormatUtils.formatDisplay(it.date)}" },
                onRowDelete = { index ->
                    adapter.currentList.getOrNull(index)?.let { viewModel.delete(it.id) }
                }
            )
        }
        binding.btnExport.setOnClickListener {
            ExportDialog.show(requireContext()) { _, format ->
                val rows = buildList {
                    add(listOf("Title", "Date"))
                    addAll(adapter.currentList.map {
                        listOf(
                            it.title,
                            DateFormatUtils.formatDisplay(it.date)
                        )
                    })
                }
                pendingExport = ExportRequest(format, rows)
                createExportFile.launch("completed_works.${ExportUtils.extensionFor(format)}")
            }
        }

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val work = adapter.currentList[vh.adapterPosition]
                viewModel.delete(work.id)
            }
        }

        ItemTouchHelper(swipeHandler)
            .attachToRecyclerView(binding.recyclerView)
    }
}