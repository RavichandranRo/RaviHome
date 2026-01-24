package com.example.ravihome.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.databinding.RowCompletedWorkBinding

class CompletedWorksAdapter : ListAdapter<WorkEntity, CompletedWorksAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowCompletedWorkBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: RowCompletedWorkBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: WorkEntity) {
            b.tvTitle.text = item.title
            b.tvDate.text = item.date
            b.tvDesc.text = item.description
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<WorkEntity>() {
            override fun areItemsTheSame(o: WorkEntity, n: WorkEntity) = o.id == n.id
            override fun areContentsTheSame(o: WorkEntity, n: WorkEntity) = o == n
        }
    }
}