package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.MiddleNewsPojoOld
import com.mobi.nubitalcatel.databinding.ImgNewsAdapterItemBinding
import com.mobi.nubitalcatel.ui.activity.WebviewActivity

class ImgNewsAdapter(
    private val context: Context,
    private val items: List<MiddleNewsPojoOld>
) : RecyclerView.Adapter<ImgNewsAdapter.ImgNewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgNewsViewHolder {
        val binding = ImgNewsAdapterItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImgNewsViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ImgNewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int =
        if (items.isNotEmpty()) 1 else 0 // keep same logic as your original Java version

    inner class ImgNewsViewHolder(
        private val binding: ImgNewsAdapterItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.N)
        fun bind(item: MiddleNewsPojoOld) {
            binding.txtTopNewTitle.text = item.title
            binding.txtTopNewDes.text = item.description

            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder_appss)
                .error(R.drawable.placeholder_appss)
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .centerCrop()
                .into(binding.imgTopNews)

            binding.root.setOnClickListener {
                // Log event
//                EventsLogger.logEvent(Constants.CATEGORY)

//                val params = hashMapOf(
//                    Constants.TITLE to item.title,
//                    Constants.DESCRIPTION to item.description
//                )
//                EventsLogger.logEvent(Constants.EVENT_IMG_NEWS_SPORTS, params)

                val intent = Intent(context, WebviewActivity::class.java)
                intent.putExtra("url", item.redirectLink)
                intent.putExtra("title", item.feedProviderName)
                context.startActivity(intent)
                // Handle click
//                MyUtility.handleItemClick(
//                    context,
//                    "",
//                    item.redirect_link,
//                    item.image,
//                    "News",
//                    item.open_with,
//                    item.feedProviderName
//                )
            }
        }
    }
}
