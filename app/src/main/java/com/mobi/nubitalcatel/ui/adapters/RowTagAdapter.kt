package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.databinding.RowTagItemBinding

class RowTagAdapter(
    private val context: Context,
    private var dataList: List<TopNewsPojo>
) : RecyclerView.Adapter<RowTagAdapter.MyViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RowTagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]

        holder.binding.txtTopNewTitle.isSelected = true
        holder.binding.txtTopNewTitle.text = item.title

        Glide.with(holder.binding.imgTopNews.context)
            .load(item.feedProvider)
            .placeholder(R.drawable.placeholder_appss)
            .error(R.drawable.placeholder_appss)
            .thumbnail(0.25f)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .dontAnimate()
            .centerCrop()
            .into(holder.binding.imgTopNews)

//        holder.binding.root.setOnClickListener {
//            EventsLogger.logEvent(Constants.CATEGORY)
//            val params = hashMapOf(
//                Constants.TITLE to (item.title ?: ""),
//                Constants.DESCRIPTION to (item.description ?: "")
//            )
//            EventsLogger.logEvent(Constants.EVENT_NEWS_BANNER, params)
//
//            MyUtility.handleItemClick(
//                context,
//                "",
//                item.redirect_link ?: "",
//                "",
//                "Top News",
//                "",
//                "ABP News"
//            )
//        }
    }

    override fun getItemCount() = dataList.size

    override fun getItemId(position: Int) = position.toLong()

    class MyViewHolder(val binding: RowTagItemBinding) : RecyclerView.ViewHolder(binding.root)
}
