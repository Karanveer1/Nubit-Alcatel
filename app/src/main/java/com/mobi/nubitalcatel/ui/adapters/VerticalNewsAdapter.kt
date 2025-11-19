package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.databinding.VerticalNewsItemBinding

class VerticalNewsAdapter(
    private val cardTitle: String,
    private val context: Context,
    private var items: List<TopNewsPojo>
) : RecyclerView.Adapter<VerticalNewsAdapter.MyViewHolder>() {

    fun updateList(updatedList: List<TopNewsPojo>) {
        items = updatedList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = VerticalNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position]

        // Load image efficiently
        Glide.with(holder.binding.root)
            .load(item.image)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.placeholder_appss)
            .error(R.drawable.placeholder_appss)
            .centerCrop()
            .into(holder.binding.externalGamesIcon)

        holder.binding.txtExternalGamesName.text = item.title

//        holder.binding.root.setOnClickListener {
//            MyUtility.handleItemClick(
//                context,
//                item.packageName,
//                item.redirectLink,
//                item.image,
//                "Html Games",
//                item.openWith,
//                cardTitle
//            )
//        }
    }

    override fun getItemCount(): Int = items.size

    class MyViewHolder(val binding: VerticalNewsItemBinding) : RecyclerView.ViewHolder(binding.root)
}
