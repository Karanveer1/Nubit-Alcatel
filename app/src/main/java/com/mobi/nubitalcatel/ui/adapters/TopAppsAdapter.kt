package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.TopApps_Pojo
import com.mobi.nubitalcatel.databinding.TopAppsItemBinding

class TopAppsAdapter(
    private val context: Context,
    private val items: List<TopApps_Pojo>
) : RecyclerView.Adapter<TopAppsAdapter.TopAppsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopAppsViewHolder {
        val binding = TopAppsItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TopAppsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopAppsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TopAppsViewHolder(
        private val binding: TopAppsItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopApps_Pojo) {
            // Load App Banner
            Glide.with(context)
                .load(item.banner_image)
                .thumbnail(0.5f)
                .placeholder(R.drawable.placeholder_appss)
                .error(R.drawable.placeholder_appss)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .dontAnimate()
                .into(binding.topappsIcon)

            // App Name
            binding.txtTopappsName.text = item.title

            // Install Button (if required)
//            binding.btnInstallTopapps.text = item.package_name
            binding.btnInstallTopapps.setOnClickListener {
//                MyUtility.handleItemClick(
//                    context,
//                    item.package_name,
//                    item.redirect_link,
//                    item.banner_thumb_image,
//                    "Apps",
//                    item.open_with,
//                    item.title
//                )
            }

            // Dynamically set card width to 1/5th of screen (if needed)
//            val screenWidth: Int = MyUtility.getScreenWidth(binding.root.context)
//            binding.topappsCardview.layoutParams.width = screenWidth / 5
        }
    }
}
