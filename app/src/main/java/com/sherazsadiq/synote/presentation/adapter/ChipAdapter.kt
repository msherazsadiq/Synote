package com.sherazsadiq.synote.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.synote.databinding.ItemChipBinding

class ChipAdapter(
    private val items: MutableList<String>,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<ChipAdapter.ChipViewHolder>() {

    inner class ChipViewHolder(private val binding: ItemChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.chip.text = text
            binding.chip.setOnCloseIconClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position < items.size) {
                    val item = items[position]
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    onRemove(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
