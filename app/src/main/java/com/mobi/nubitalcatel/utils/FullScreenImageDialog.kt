package com.mobi.nubitalcatel.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.ui.activity.WebviewActivity
import java.io.Serializable

class FullScreenImageDialog : DialogFragment() {
    private var mediaList: MutableList<MediaItem>? = ArrayList()
    private var currentPosition = 0
    private var indicatorLayout: LinearLayout? = null
    private var singleRedirectUrl: String? = null

    // Autoscroll variables
    private var autoscrollHandler: Handler? = null
    private var autoscrollRunnable: Runnable? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var isAutoscrollEnabled = true
    private var isUserInteracting = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)
        val closeIcon = view.findViewById<ImageView>(R.id.close_button)
        closeIcon.setOnClickListener { v: View? -> dismiss() }
        recyclerView = view.findViewById<RecyclerView>(R.id.mediaRecyclerView)
        indicatorLayout = view.findViewById<LinearLayout>(R.id.indicatorLayout)
        if (arguments != null) {
            if (requireArguments().containsKey(ARG_MEDIA_LIST)) {
                mediaList = requireArguments().getSerializable(ARG_MEDIA_LIST) as MutableList<MediaItem>?
            } else if (requireArguments().containsKey(ARG_IMAGE_URL)) {
                val imageUrl = requireArguments().getString(ARG_IMAGE_URL)
                val redirect_link = requireArguments().getString(ARG_REDIRECT_URL)
                singleRedirectUrl = requireArguments().getString(ARG_REDIRECT_URL)
                mediaList = ArrayList()
                mediaList!!.add(MediaItem(imageUrl, MediaItem.Type.IMAGE, redirect_link))
            }
        }
        val adapter: MediaAdapter = MediaAdapter(mediaList!!)
        recyclerView!!.setAdapter(adapter)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView!!.setLayoutManager(layoutManager)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        setupIndicators(mediaList!!.size)
        setCurrentIndicator(0)
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val pos = layoutManager!!.findFirstVisibleItemPosition()
                if (pos != currentPosition) {
                    currentPosition = pos
                    setCurrentIndicator(pos)
                    handleVideoPlayback(pos)
                }
            }
        })

        // Setup autoscroll functionality
        setupAutoscroll()

        // Add touch listener to pause autoscroll on user interaction
        recyclerView!!.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    pauseAutoscroll()
                    isUserInteracting = true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Resume autoscroll after a delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!isUserInteracting) {
                            resumeAutoscroll()
                        }
                    }, USER_INTERACTION_PAUSE_DURATION)
                    isUserInteracting = false
                }
            }
            false // Don't consume the touch event
        })
        return view
    }

    // Sample static data for testing


    override fun onStart() {
        super.onStart()
        //        if (getDialog() != null && getDialog().getWindow() != null) {
//            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        }
        if (dialog != null && dialog!!.window != null) {
            val window = dialog!!.window
            if (window != null) {
                // Convert 8dp to pixels
                val marginInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8f,
                    resources.displayMetrics
                ).toInt()

                // Set dialog size with margins
                val params = window.attributes
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                params.horizontalMargin = marginInPx.toFloat()
                params.verticalMargin = marginInPx.toFloat()
                window.attributes = params

                // Optional: Transparent background
                window.setBackgroundDrawableResource(R.color.transparent)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pauseAutoscroll()
    }

    override fun onResume() {
        super.onResume()
        if (!isUserInteracting) {
            resumeAutoscroll()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoscroll()
    }

    private fun setupIndicators(count: Int) {
        indicatorLayout!!.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(context)
            dot.setImageResource(R.drawable.indicator_dot_unselected)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            indicatorLayout!!.addView(dot)
        }
    }

    private fun setCurrentIndicator(position: Int) {
        for (i in 0 until indicatorLayout!!.childCount) {
            val dot = indicatorLayout!!.getChildAt(i) as ImageView
            dot.setImageResource(if (i == position) R.drawable.indicator_dot_selected else R.drawable.indicator_dot_unselected)
        }
    }

    private fun handleVideoPlayback(currentPos: Int) {
        if (recyclerView == null || recyclerView!!.adapter == null) return
        val adapter = recyclerView!!.adapter as MediaAdapter?
        for (i in mediaList!!.indices) {
            val holder = recyclerView!!.findViewHolderForAdapterPosition(i)
            if (holder is MediaAdapter.VideoViewHolder) {
                val videoView = holder.videoView
                if (i == currentPos) {
                    // Start video for current position
                    if (!videoView.isPlaying) {
                        videoView.start()
                    }
                } else {
                    // Pause video for other positions
                    if (videoView.isPlaying) {
                        videoView.pause()
                    }
                }
            }
        }
    }

    // Autoscroll methods
    private fun setupAutoscroll() {
        if (mediaList!!.size <= 1) {
            return  // Don't autoscroll if there's only one item
        }
        autoscrollHandler = Handler(Looper.getMainLooper())
        autoscrollRunnable = object : Runnable {
            override fun run() {
                if (isAutoscrollEnabled && !isUserInteracting && mediaList!!.size > 1) {
                    val nextPosition = (currentPosition + 1) % mediaList!!.size
                    recyclerView!!.smoothScrollToPosition(nextPosition)
                    currentPosition = nextPosition
                    setCurrentIndicator(currentPosition)
                }
                autoscrollHandler!!.postDelayed(this, AUTOSCROLL_DELAY)
            }
        }

        // Start autoscroll
        autoscrollHandler!!.postDelayed(autoscrollRunnable as Runnable, AUTOSCROLL_DELAY)
    }

    private fun pauseAutoscroll() {
        isAutoscrollEnabled = false
    }

    private fun resumeAutoscroll() {
        isAutoscrollEnabled = true
    }

    private fun stopAutoscroll() {
        if (autoscrollHandler != null && autoscrollRunnable != null) {
            autoscrollHandler!!.removeCallbacks(autoscrollRunnable!!)
        }
    }

    // Public methods to control autoscroll
    fun setAutoscrollEnabled(enabled: Boolean) {
        isAutoscrollEnabled = enabled
        if (enabled && !isUserInteracting) {
            resumeAutoscroll()
        } else {
            pauseAutoscroll()
        }
    }

    fun setAutoscrollDelay(delayMillis: Long) {
        // Stop current autoscroll and restart with new delay
        stopAutoscroll()
        if (delayMillis > 0) {
            autoscrollHandler!!.postDelayed(autoscrollRunnable!!, delayMillis)
        }
    }

    class MediaItem(val url: String?, val type: Type, val redirect_link: String?) :
        Serializable {
        enum class Type {
            IMAGE,
            VIDEO
        }
    }

    private inner class MediaAdapter internal constructor(private val items: List<MediaItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return if (items[position].type == MediaItem.Type.IMAGE) TYPE_IMAGE else TYPE_VIDEO
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val context = parent.context
            val cardMargin = (16 * context.resources.displayMetrics.density).toInt() // 16dp
            val cardRadius = (16 * context.resources.displayMetrics.density).toInt() // 16dp
            val cardElevation = (4 * context.resources.displayMetrics.density).toInt() // 4dp
            val cardView = CardView(context)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.setMargins(cardMargin, cardMargin, cardMargin, cardMargin)
            cardView.layoutParams = params
            cardView.radius = cardRadius.toFloat()
            cardView.cardElevation = cardElevation.toFloat()
            cardView.useCompatPadding = true
            return if (viewType == TYPE_IMAGE) {
                val imageView = ImageView(context)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.FIT_XY
                cardView.addView(imageView)
                ImageViewHolder(cardView)
            } else {
                val videoView = VideoView(context)
                videoView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                cardView.addView(videoView)
                VideoViewHolder(cardView)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (holder is ImageViewHolder) {
                Glide.with(holder.itemView.context).load(item.url).into(holder.imageView)
                // Handle click for single-image mode
//                if (singleRedirectUrl != null) {
                holder.imageView.setOnClickListener {
//                    MyUtility.handleItemClick(
//                        activity, "", item.redirect_link, "",
//                        "Middle News", "1", "Ad"
//                    )
                    val intent = Intent(context, WebviewActivity::class.java)
                    intent.putExtra("url", item.redirect_link)
                    context!!.startActivity(intent)
                }
                //                }
            } else if (holder is VideoViewHolder) {
                val videoHolder = holder
                videoHolder.videoView.setVideoURI(Uri.parse(item.url))

                // Only start video if it's the current position
                if (position == currentPosition) {
                    videoHolder.videoView.start()
                }
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }

        internal inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView

            init {
                // itemView is CardView, child 0 is ImageView
                imageView = (itemView as CardView).getChildAt(0) as ImageView
            }
        }

        internal inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var videoView: VideoView

            init {
                // itemView is CardView, child 0 is VideoView
                videoView = (itemView as CardView).getChildAt(0) as VideoView
            }
        }
    }

    companion object {
        private const val ARG_MEDIA_LIST = "media_list"
        private const val ARG_IMAGE_URL = "image_url"
        private const val ARG_REDIRECT_URL = "redirectUrl"
        private const val AUTOSCROLL_DELAY: Long = 2000 // 3 seconds
        private const val TYPE_IMAGE = 0
        private const val TYPE_VIDEO = 1
        private const val USER_INTERACTION_PAUSE_DURATION: Long =
            3000 // 5 seconds pause after user interaction

        fun newInstance(imageUrl: String?, redirectUrl: String?): FullScreenImageDialog {
            val dialog = FullScreenImageDialog()
            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            args.putString(ARG_REDIRECT_URL, redirectUrl)
            dialog.setArguments(args)
            return dialog
        }

        fun newInstance(mediaList: ArrayList<MediaItem?>?): FullScreenImageDialog {
            val dialog = FullScreenImageDialog()
            val args = Bundle()
            args.putSerializable(ARG_MEDIA_LIST, mediaList)
            dialog.setArguments(args)
            return dialog
        }

        fun showDialog(activity: FragmentActivity, imageUrl: String?, redirectUrl: String?) {
            val dialog = newInstance(imageUrl, redirectUrl)
            dialog.show(activity.supportFragmentManager, "FullScreenImageDialog")
        }

        fun showDialog(activity: FragmentActivity, mediaList: ArrayList<MediaItem?>) {
            val dialog = newInstance(mediaList)
            dialog.show(activity.supportFragmentManager, "FullScreenImageDialog")
        }

        val sampleMediaList: ArrayList<MediaItem>
            // Sample static data for testing
            get() =// Sample video 1
            //        list.add(new MediaItem("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", MediaItem.Type.VIDEO));
            // Sample video 2
            //        list.add(new MediaItem("https://www.w3schools.com/html/mov_bbb.mp4", MediaItem.Type.VIDEO));
            // Another image
            //        list.add(new MediaItem("https://vistory.s3.ap-south-1.amazonaws.com/assets/40a13cda42e692240439c01b33cd001f.jpg", MediaItem.Type.IMAGE));
            //        list.add(new MediaItem("https://farm4.staticflickr.com/3752/9684880330_9b4698f7cb_z_d.jpg", MediaItem.Type.IMAGE));
                //        list.add(new MediaItem("https://vistory.s3.ap-south-1.amazonaws.com/assets/40a13cda42e692240439c01b33cd001f.jpg", MediaItem.Type.IMAGE));
                ArrayList()
    }
}