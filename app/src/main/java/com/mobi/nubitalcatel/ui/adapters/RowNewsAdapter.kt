package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.databinding.ItemRowNewsBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RowNewsAdapter(
    private val context: Context,
    private val newsList: List<TopNewsPojo>,
    private val topCateName: String
) : RecyclerView.Adapter<RowNewsAdapter.RowNewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowNewsViewHolder {
        val binding = ItemRowNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RowNewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowNewsViewHolder, position: Int) {
        val item = newsList[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = newsList.size

    inner class RowNewsViewHolder(private val binding: ItemRowNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopNewsPojo, position: Int) {
            Log.d("NewsAdapter", "Binding item: ${item.title}")

            // Load main image
            Glide.with(context)
                .load(item.image)
                .placeholder(R.drawable.placeholder_appss)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.placeholder_appss)
                .into(binding.imgTopNews)

            // Divider visibility for last item
            binding.dividerLine.visibility =
                if (position == itemCount - 1) View.GONE else View.VISIBLE

            // Handle Ads vs News
            if (item.isAds) {
                binding.frameAds.visibility = View.VISIBLE
                binding.rlNews.visibility = View.GONE
                binding.dividerLine.visibility = View.GONE
//                Log.d("NewsAdapter", "Loading Ad with unitId: ${item.adUnitId}")
//                AdManager.showBannerAdNoMargin(context, binding.frameAds, item.adUnitId)
            } else {
                binding.frameAds.visibility = View.GONE
                binding.rlNews.visibility = View.VISIBLE
            }

            // Title handling
            if (item.title.isNullOrEmpty()) {
                binding.txtTopNewTitle.visibility = View.GONE
                binding.txtTopNewDes.textSize = 16f
                binding.txtTopNewDes.setTextColor(context.getColor(R.color.black))
            } else {
                binding.txtTopNewTitle.visibility = View.VISIBLE
                binding.txtTopNewTitle.text = item.title
                binding.txtTopNewTitle.setTextColor(context.getColor(R.color.black))
                binding.txtTopNewDes.textSize = 13f
                binding.txtTopNewDes.setTextColor(context.getColor(R.color.default_text_color))
            }

            // Date formatting
            try {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy Z", Locale.getDefault())
                val date = inputFormat.parse(item.postedDate)
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                binding.txtTopNewDate.text = date?.let { outputFormat.format(it) } ?: "Invalid Date"
            } catch (e: ParseException) {
                binding.txtTopNewDate.text = ""
            }

            // News provider image
            Glide.with(context)
                .load(item.feedProvider)
                .into(binding.ivNewsBy)

            // Description
            binding.txtTopNewDes.text = item.description

            // Click listener
            binding.layoutTopNewsItems.setOnClickListener {
//                if (MyUtility.isConnectedToInternet()) {
//                    EventsLogger.logEvent(Constants.CATEGORY)
//                    val params = mapOf(
//                        Constants.TITLE to (item.title ?: ""),
//                        Constants.DESCRIPTION to (item.description ?: "")
//                    )
//                    EventsLogger.logEvent(Constants.EVENT_TRENDING_NEWS, params)
//
//                    MyUtility.handleItemClick(
//                        context = context,
//                        title = item.title ?: "",
//                        link = item.redirectLink,
//                        fallbackLink = item.redirectLink,
//                        category = topCateName,
//                        extra = "",
//                        topCateName = topCateName
//                    )
//                } else {
//                    MyUtility.NoInternet_Msg(context)
//                }
            }
        }
    }
}
