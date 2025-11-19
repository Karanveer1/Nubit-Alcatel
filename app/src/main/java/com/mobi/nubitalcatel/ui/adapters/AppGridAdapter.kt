package com.mobi.nubitalcatel.ui.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.databinding.ItemAppGridBinding

class AppGridAdapter(
    private val context: Context,
    private val sortedApps: List<Map.Entry<String, Long>>,
    private val packageManager: PackageManager
) : RecyclerView.Adapter<AppGridAdapter.AppGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppGridViewHolder {
        val binding = ItemAppGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppGridViewHolder, position: Int) {
        holder.bind(sortedApps[position])
    }

    override fun getItemCount(): Int = sortedApps.size

    inner class AppGridViewHolder(
        private val binding: ItemAppGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appEntry: Map.Entry<String, Long>) {
            val packageName = appEntry.key
            val appName = getAppName(packageName)
            val appIcon = getAppIcon(packageName)

            binding.serviceGridText.text = appName

            if (appIcon != null) {
                binding.serviceGridImage.setImageDrawable(appIcon)
            } else {
                binding.serviceGridImage.setImageResource(R.drawable.placeholder_apps)
            }

            binding.root.setOnClickListener {
                openApp(packageName)
            }
        }
    }

    private fun openApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            context.startActivity(it)
        }
    }

    private fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            packageName
        }
    }
}
