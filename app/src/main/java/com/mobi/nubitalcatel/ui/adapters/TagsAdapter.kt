package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.TagsPojo
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.databinding.TagsItemBinding

class NewsTagsAdapter(
    private val context: Context,
    private val tagsList: List<TagsPojo>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<NewsTagsAdapter.NewsTagsViewHolder>() {

    private var selectedPosition = 0

    interface OnItemClickListener {
        fun onItemClick(id: String, catName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsTagsViewHolder {
        val binding = TagsItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsTagsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsTagsViewHolder, position: Int) {
        holder.bind(tagsList[position], position)
    }

    override fun getItemCount(): Int = tagsList.size

    inner class NewsTagsViewHolder(
        private val binding: TagsItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TagsPojo, position: Int) {
            val isSelected = selectedPosition == position

            binding.btnNewsTags.text = item.categoryName
            binding.btnNewsTags.setBackgroundResource(
                if (isSelected) R.drawable.activated_tags else R.drawable.default_tags
            )
            binding.btnNewsTags.setTextColor(
                ContextCompat.getColor(context, if (isSelected) R.color.white else R.color.black)
            )

            binding.btnNewsTags.setOnClickListener {
                if (selectedPosition != position) {
                    val oldPos = selectedPosition
                    selectedPosition = position

//                    listener.onItemClick(item.!!, item.categoryName!!)

                    // Logging events
//                    EventsLogger.logEvent(Constants.CATEGORY)
//                    val params = hashMapOf(
//                        Constants.TITLE to item.categoryName,
//                        Constants.DESCRIPTION to item.id
//                    )
//                    EventsLogger.logEvent(Constants.EVENT_NEWS_CATE, params)

                    listener.onItemClick(item.id.toString(), item.categoryName ?: "")

                    notifyItemChanged(oldPos)
                    notifyItemChanged(position)
                }
            }
        }
    }
}
