package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobi.nubitalcatel.core.models.OtherServicesPojo
import com.mobi.nubitalcatel.databinding.SingleRowTextItemBinding


class SingleRowTextAdapter(private val context: Context, items: List<OtherServicesPojo>) :
    RecyclerView.Adapter<SingleRowTextAdapter.MyViewHoldecr>() {
    private val items: List<OtherServicesPojo>

    init {
        this.items = items // avoid null issues
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHoldecr {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SingleRowTextItemBinding =
            SingleRowTextItemBinding.inflate(inflater, parent, false)
        return MyViewHoldecr(binding)
    }

    override fun onBindViewHolder( holder: MyViewHoldecr, position: Int) {
        val (title, icon, open_with, package_name, redirect) = items[position]
        holder.binding.txtOtherSer.setText(title)
        Glide.with(context).load(items[position].icon).into(holder.binding.imgOtherSer)
        holder.binding.layoutOS.setOnClickListener { v ->
//            EventsLogger.logEvent(Constants.CATEGORY)
//            val params: MutableMap<String, String?> =
//                HashMap()
//            params[Constants.TITLE] = title
//            params[Constants.DESCRIPTION] = redirect
//            EventsLogger.logEvent(Constants.EVENT_OTHER_SERVICES, params)
//            if (TextUtils.isEmpty(redirect)) {
//                MyUtility.saveTracksInDB(
//                    context,
//                    icon,
//                    title,
//                    "Other Services",
//                    "0",
//                    title,
//                    ""
//                )
//                val intent =
//                    Intent(context, YupTv_Activity::class.java)
//                intent.putExtra("title", title)
//                context.startActivity(intent)
//            } else {
//                MyUtility.handleItemClick(
//                    context,
//                    package_name,
//                    redirect,
//                    redirect,
//                    "Other Services",
//                    open_with,
//                    title
//                )
//            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class MyViewHoldecr(binding: SingleRowTextItemBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        val binding: SingleRowTextItemBinding

        init {
            this.binding = binding
        }
    }
}

