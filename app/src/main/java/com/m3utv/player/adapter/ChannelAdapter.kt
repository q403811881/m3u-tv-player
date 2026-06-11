package com.m3utv.player.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.m3utv.player.databinding.ItemChannelBinding
import com.m3utv.player.model.Channel

class ChannelAdapter(
    private val channels: List<Channel>,
    private val onItemClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel, isSelected: Boolean) {
            binding.tvChannelName.text = channel.name
            binding.tvChannelGroup.text = channel.groupTitle ?: ""
            binding.tvChannelGroup.isVisible = channel.groupTitle != null
            binding.root.isSelected = isSelected

            binding.root.setOnClickListener {
                val previous = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onItemClick(channel)
            }

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.root.isSelected = hasFocus
                if (hasFocus) {
                    selectedPosition = bindingAdapterPosition
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(channels[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = channels.size
}
