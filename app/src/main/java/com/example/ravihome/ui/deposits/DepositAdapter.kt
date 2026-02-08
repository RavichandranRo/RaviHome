package com.example.ravihome.ui.deposits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.databinding.RowDepositEntryBinding

class DepositAdapter(
    private val maturityCalculator: (DepositEntry) -> Double,
    private val onEdit: (DepositEntry) -> Unit
) : ListAdapter<DepositEntry, DepositAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowDepositEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: RowDepositEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DepositEntry) {
            binding.tvTitle.text = "${item.bank} • ${item.durationMonths} months"
            binding.tvDetails.text = "Amount: ₹%.2f • Rate: %.2f%%".format(item.amount, item.rate)
            val maturity = maturityCalculator(item)
            binding.tvMaturity.text = "Maturity: ₹%.2f".format(maturity)
            binding.btnEdit.setOnClickListener { onEdit(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DepositEntry>() {
            override fun areItemsTheSame(oldItem: DepositEntry, newItem: DepositEntry) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DepositEntry, newItem: DepositEntry) =
                oldItem == newItem
        }
    }
}