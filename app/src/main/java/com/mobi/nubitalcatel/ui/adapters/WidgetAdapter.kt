package com.mobi.nubitalcatel.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobi.nubitalcatel.core.models.NewsVideoPojo
import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.databinding.LayoutNewsVideoBinding

class WidgetAdapter(
    private val items: List<WidgetOrder>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_VIDEO = 1
        private const val TYPE_SERVICES = 2
        private const val TYPE_ADMOB = 3
        private const val TYPE_BANNER = 4
        private const val TYPE_NEWS = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].order_type) {
            "news_video" -> TYPE_VIDEO
            "services" -> TYPE_SERVICES
            "admob_one", "admob_two", "admob_three",
            "admob_four", "admob_five", "admob_six", "admob_seven" -> TYPE_ADMOB
            "banner_news_new", "entertainment" -> TYPE_BANNER
            "news1", "news2", "news3" -> TYPE_NEWS
            else -> TYPE_SERVICES // fallback
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_VIDEO -> VideoListViewHolder(
                LayoutNewsVideoBinding.inflate(inflater, parent, false)
            )
            else -> VideoListViewHolder(
                LayoutNewsVideoBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val widget = items[position]
        when (holder) {
            is VideoListViewHolder -> {
                // Assuming your WidgetOrder has a list of videos inside it
//                widget.videos?.let { holder.bind(it) }
            }
            // TODO: hook other ViewHolders (services, admob, banner, news…)
        }
    }

    override fun getItemCount() = items.size

    // --- ViewHolders ---
    class VideoListViewHolder(private val binding: LayoutNewsVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(videos: List<NewsVideoPojo>) {
            val videoAdapter = VideoItemAdapter(videos,binding.recyclerVierwsDy)
            binding.recyclerVierwsDy.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = videoAdapter
            }
        }
    }
}

//class WidgetAdapter(
//    private val items: List<WidgetOrder>
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    companion object {
//        private const val TYPE_VIDEO = 1
//        private const val TYPE_SERVICES = 2
//        private const val TYPE_ADMOB = 3
//        private const val TYPE_BANNER = 4
//        private const val TYPE_NEWS = 5
//        // ... extend more as needed
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return when (items[position].order_type) {
//            "news_video" -> TYPE_VIDEO
//            "services" -> TYPE_SERVICES
//            "admob_one", "admob_two", "admob_three",
//            "admob_four", "admob_five", "admob_six", "admob_seven" -> TYPE_ADMOB
//            "banner_news_new", "entertainment" -> TYPE_BANNER
//            "news1", "news2", "news3" -> TYPE_NEWS
//            else -> TYPE_SERVICES // default fallback
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        return when (viewType) {
//            TYPE_VIDEO -> VideoListViewHolder(
//                VideoItemBinding.inflate(inflater, parent, false)
//            )
//            else -> VideoListViewHolder(
//                VideoItemBinding.inflate(inflater, parent, false)
//            )
////            TYPE_SERVICES -> ServicesViewHolder(
////                ItemServicesBinding.inflate(inflater, parent, false)
////            )
////            TYPE_ADMOB -> AdmobViewHolder(
////                ItemAdmobBinding.inflate(inflater, parent, false)
////            )
////            TYPE_BANNER -> BannerViewHolder(
////                ItemBannerBinding.inflate(inflater, parent, false)
////            )
////            TYPE_NEWS -> NewsViewHolder(
////                ItemNewsBinding.inflate(inflater, parent, false)
////            )
////            else -> ServicesViewHolder(
////                ItemServicesBinding.inflate(inflater, parent, false)
////            )
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val widget = items[position]
//        when (holder) {
//            is VideoListViewHolder -> holder.bind(widget)
////            is ServicesViewHolder -> holder.bind(widget)
////            is AdmobViewHolder -> holder.bind(widget)
////            is BannerViewHolder -> holder.bind(widget)
////            is NewsViewHolder -> holder.bind(widget)
//        }
//    }
//
//    // --- ViewHolders ---
//    class VideoListViewHolder(private val binding: VideoItemBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(videos: List<NewsVideoPojo>) {
//            val videoAdapter = VideoItemAdapter(videos)
//            binding.recyclerViewVideos.apply {
//                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//                adapter = videoAdapter
//            }
//        }
//    }
//
//
//    override fun getItemCount() = items.size
//}
