package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.databinding.NewsItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NewsAdapter(
    private val context: Context,
    private val items: List<TopNewsPojo>,
    private val topCateName: String
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    companion object {
        private const val AD_POSITION = 2 // Example: Show ad at position 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class NewsViewHolder(
        private val binding: NewsItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopNewsPojo, position: Int) {

            // Handle Ads
            if (item.isAds) {
                binding.frameAds.visibility = View.VISIBLE
                binding.rlNews.visibility = View.GONE
                binding.dividerLine.visibility = View.GONE
//                AdManager.showBannerAdNoMargin(context, binding.frameAds, item.adUnitId)
                return
            } else {
                binding.frameAds.visibility = View.GONE
                binding.rlNews.visibility = View.VISIBLE
            }

            // Load News Image
            Glide.with(context)
                .load(item.image)
                .placeholder(R.drawable.placeholder_appss)
                .error(R.drawable.placeholder_appss)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgTopNews)

            // Divider Visibility
            if (position == itemCount - 1) {
                binding.dividerLine.visibility = View.GONE
            }

            // Title & Description Styling
            if (item.title.isNullOrEmpty()) {
                binding.txtTopNewTitle.visibility = View.GONE
                binding.txtTopNewDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                binding.txtTopNewDes.setTextColor(context.getColor(R.color.black))
            } else {
                binding.txtTopNewTitle.visibility = View.VISIBLE
                binding.txtTopNewTitle.text = item.title
                binding.txtTopNewTitle.setTextColor(context.getColor(R.color.black))
                binding.txtTopNewDes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                binding.txtTopNewDes.setTextColor(context.getColor(R.color.default_text_color))
            }

            // Format Date
            try {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy Z", Locale.getDefault())
                val date = inputFormat.parse(item.postedDate)
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                binding.txtTopNewDate.text = date?.let { outputFormat.format(it) } ?: "Invalid Date"
            } catch (e: Exception) {
                binding.txtTopNewDate.text = item.postedDate
            }

            // Feed provider icon
            Glide.with(context).load(item.feedProvider).into(binding.ivNewsBy)
            binding.txtTopNewDes.text = item.description

            // Handle Clicks
            binding.layoutTopNewsItems.setOnClickListener {
//                if (MyUtility.isConnectedToInternet()) {
//                    EventsLogger.logEvent(Constants.CATEGORY)
//                    val params = hashMapOf(
//                        Constants.TITLE to (item.title ?: ""),
//                        Constants.DESCRIPTION to (item.description ?: "")
//                    )
//                    EventsLogger.logEvent(Constants.EVENT_TRENDING_NEWS, params)
//
//                    MyUtility.handleItemClick(
//                        context,
//                        "",
//                        item.redirectLink,
//                        item.redirectLink,
//                        topCateName,
//                        "",
//                        topCateName
//                    )
//                } else {
//                    MyUtility.NoInternet_Msg(context)
//                }
            }
        }
    }
}
