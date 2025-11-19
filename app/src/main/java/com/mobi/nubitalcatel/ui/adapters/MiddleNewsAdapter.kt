package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.MiddleNewsPojo
import java.text.SimpleDateFormat
import java.util.Locale
import com.mobi.nubitalcatel.databinding.LayoutNewsVideoBinding
import com.mobi.nubitalcatel.databinding.MiddleNewsAdapterItemsBinding
import java.util.TimeZone

class MiddleNewsAdapter(
    private val context: Context,
    private val items: List<MiddleNewsPojo>
) : RecyclerView.Adapter<MiddleNewsAdapter.MiddleNewsViewHolder>() {

    companion object {
        private const val AD_POSITION = 2 // Example: Show ad at position 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiddleNewsViewHolder {
        val binding = MiddleNewsAdapterItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MiddleNewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MiddleNewsViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class MiddleNewsViewHolder(
        private val binding: MiddleNewsAdapterItemsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MiddleNewsPojo, position: Int) {
//            // Handle Ads
//            if (position == AD_POSITION) {
//                binding.frameAds.visibility = View.VISIBLE
//                binding.middleNewsLayout.visibility = View.GONE
////                AdManager.showMediumRectangleAd(
////                    context,
////                    binding.frameAds,
////                    item.adUnitId
////                )
//                return
//            } else {
//                binding.frameAds.visibility = View.GONE
//                binding.middleNewsLayout.visibility = View.VISIBLE
//            }
//
//            // Image with Glide
//            Glide.with(binding.root.context)
//                .load(item.image)
//                .thumbnail(0.25f)
//                .placeholder(R.drawable.placeholder_apps)
//                .centerCrop()
//                .into(binding.imgMiddleNews)
//
//            // Texts
//            binding.txtMiddleNewsTitle.text = item.title
//            binding.txtMiddleNewsDes.text = item.description
//
//            // Date formatting
//            try {
//                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
//                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
//                val date = inputFormat.parse(item.postedDate)
//                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
//                binding.txtMiddleNewsDate.text = outputFormat.format(date!!)
//            } catch (e: Exception) {
//                binding.txtMiddleNewsDate.text = "${item.postedDate} ${item.postedTime}"
//            }
//
//            // Card + Image sizing
////            binding.layoutCardveiw.layoutParams.width = MyUtility.NEWS_CONTAINER_WIDTH
////            binding.imgMiddleNews.layoutParams = RelativeLayout.LayoutParams(
////                ViewGroup.LayoutParams.MATCH_PARENT,
////                (MyUtility.getScreenWidth(context) / 2.3).toInt()
////            )
////            binding.imgMiddleNews.scaleType = ImageView.ScaleType.FIT_XY
        }
    }
}
