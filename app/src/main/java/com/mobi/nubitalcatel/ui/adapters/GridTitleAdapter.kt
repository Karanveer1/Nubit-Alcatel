package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.Services_pojo
import com.mobi.nubitalcatel.databinding.GridTitleItemBinding

class GridTitleAdapter(
    private val context: Context,
    private var serviceList: List<Services_pojo> = emptyList()
) : RecyclerView.Adapter<GridTitleAdapter.ServiceViewHolder>() {

    fun updateList(updatedList: List<Services_pojo>) {
        serviceList = updatedList.toList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = GridTitleItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(serviceList[position])
    }

    override fun getItemCount(): Int = serviceList.size

    inner class ServiceViewHolder(private val binding: GridTitleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Services_pojo) {
            binding.serviceGridText.text = item.title

            Glide.with(context)
                .load(item.banner_image)
                .placeholder(R.drawable.placeholder_appss)
                .error(R.drawable.placeholder_appss)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .dontAnimate()
                .into(binding.serviceGridImage)

//            binding.root.setOnClickListener {
//                EventsLogger.logEvent(Constants.CATEGORY)
//                val params = mapOf(
//                    Constants.TITLE to item.title,
//                    Constants.DESCRIPTION to item.redirect_link
//                )
//                EventsLogger.logEvent(Constants.EVENT_SERVICES, params)
//
//                MyUtility.handleItemClick(
//                    context,
//                    item.package_name,
//                    item.redirect_link,
//                    item.banner_image,
//                    "Services",
//                    item.open_with,
//                    item.title
//                )
//            }
        }
    }
}
