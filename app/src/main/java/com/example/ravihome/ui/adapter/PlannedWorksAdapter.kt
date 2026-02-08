package com.example.ravihome.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.databinding.RowPlannedWorkBinding
import com.example.ravihome.ui.util.DateFormatUtils

class PlannedWorksAdapter(
    private val onCompleteRequested: (WorkEntity, () -> Unit) -> Unit,
    private val onDelete: (WorkEntity) -> Unit
) : ListAdapter<WorkEntity, PlannedWorksAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowPlannedWorkBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: RowPlannedWorkBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: WorkEntity) {
            b.tvTitle.text = item.title
            b.tvDate.text = DateFormatUtils.formatDisplay(item.date)
            b.tvDesc.text = item.description
            if (item.amount != null) {
                b.tvAmount.text = "₹%.2f".format(item.amount)
                b.tvAmount.visibility = android.view.View.VISIBLE
            } else {
                b.tvAmount.visibility = android.view.View.GONE
            }
            b.cbComplete.setOnCheckedChangeListener(null)
            b.cbComplete.isChecked = false
            b.cbComplete.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    onCompleteRequested(item) { b.cbComplete.isChecked = false }
                }
            }
            b.root.setOnLongClickListener {
                onDelete(item)
                true
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<WorkEntity>() {
            override fun areItemsTheSame(o: WorkEntity, n: WorkEntity) = o.id == n.id
            override fun areContentsTheSame(o: WorkEntity, n: WorkEntity) = o == n
        }
    }
}