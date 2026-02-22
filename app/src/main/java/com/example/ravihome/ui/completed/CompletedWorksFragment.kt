package com.example.ravihome.ui.completed

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
import com.example.ravihome.databinding.FragmentCompletedWorksBinding
import com.example.ravihome.ui.adapter.CompletedWorksAdapter
import com.example.ravihome.ui.util.DateFormatUtils
import com.example.ravihome.ui.util.ViewAllDialogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CompletedWorksFragment : Fragment() {

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
                adapter.currentList.map { "${it.title} • ${DateFormatUtils.formatDisplay(it.date)}" }
            )
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