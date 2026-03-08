package com.example.ravihome.ui.deposits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ravihome.databinding.RowDepositEntryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DepositAdapter(
    private val maturityCalculator: (DepositEntry) -> Double,
    private val onEdit: (DepositEntry) -> Unit,
    private val onDelete: (DepositEntry) -> Unit = {}
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

    inner class VH(private val binding: RowDepositEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(item: DepositEntry) {
            val maturityLabel =
                if (item.isPremature) "Premature" else formatter.format(Date(item.maturityDateMillis))
            binding.tvTitle.text = "${item.bank} • ${item.depositNumber}"
            binding.tvDetails.text = "Amount: ₹%.2f • Rate: %.2f%% • Maturity: %s".format(
                item.amount,
                item.rate,
                maturityLabel
            )
            val maturity = maturityCalculator(item)
            binding.tvMaturity.text = "Maturity amount: ₹%.2f".format(maturity)
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.root.setOnLongClickListener {
                onDelete(item)
                true
            }
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