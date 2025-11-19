package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.External_game_pojo
import com.mobi.nubitalcatel.databinding.RowBannerItemBinding

class RowBannerItemAdapter(
    private val context: Context,
    private val games: ArrayList<External_game_pojo> = arrayListOf(),
    private val onGameClick: ((External_game_pojo) -> Unit)? = null
) : RecyclerView.Adapter<RowBannerItemAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RowBannerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val game = games[position]

        Log.d("RowBannerItemAdapter", "Banner: ${game.banner_image}")

        Glide.with(holder.binding.root.context)
            .load(game.banner_image)
            .thumbnail(0.25f)
            .placeholder(R.drawable.placeholder_appss)
            .error(R.drawable.placeholder_you_tube)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(holder.binding.imgTopNews)

        holder.binding.root.setOnClickListener {
            onGameClick?.invoke(game)
        }
    }

    override fun getItemCount(): Int = games.size

    class MyViewHolder(val binding: RowBannerItemBinding) : RecyclerView.ViewHolder(binding.root)
}
