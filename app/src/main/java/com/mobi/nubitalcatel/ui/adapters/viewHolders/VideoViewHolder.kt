package com.mobi.nubitalcatel.ui.adapters.viewHolders

import androidx.recyclerview.widget.RecyclerView
import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.databinding.VideoItemBinding

class VideoViewHolder(
    private val binding: VideoItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(widget: WidgetOrder) {
//        binding.textview.text = widget.name
    }
}
