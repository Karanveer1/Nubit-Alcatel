import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.ContainerOrder
import com.mobi.nubitalcatel.core.models.NewsVideoPojo
import com.mobi.nubitalcatel.core.models.OtherServicesPojo
import com.mobi.nubitalcatel.core.models.Payload
import com.mobi.nubitalcatel.core.models.Services_pojo
import com.mobi.nubitalcatel.core.models.TagsPojo
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.core.models.ViewsOrder
import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.databinding.GameTwoItemBinding
import com.mobi.nubitalcatel.databinding.GridTitleItemBinding
import com.mobi.nubitalcatel.databinding.ImgNewsAdapterItemBinding
import com.mobi.nubitalcatel.databinding.LayoutBattClockUtilBinding
import com.mobi.nubitalcatel.databinding.LayoutHealthUtilBinding
import com.mobi.nubitalcatel.databinding.LayoutNewsVideoBinding
import com.mobi.nubitalcatel.databinding.LayoutViewNoTitleBinding
import com.mobi.nubitalcatel.databinding.LayoutViewUtilBinding
import com.mobi.nubitalcatel.databinding.LayoutViewWebviewBinding
import com.mobi.nubitalcatel.databinding.LayoutViewWithTabsBinding
import com.mobi.nubitalcatel.databinding.LayoutViewWithTitleBinding
import com.mobi.nubitalcatel.databinding.LayoutWeatherRamBinding
import com.mobi.nubitalcatel.databinding.NewsItemBinding
import com.mobi.nubitalcatel.databinding.RowTagItemBinding
import com.mobi.nubitalcatel.databinding.SingleRowTextItemBinding
import com.mobi.nubitalcatel.databinding.VerticalNewsItemBinding
import com.mobi.nubitalcatel.ui.activity.WebviewActivity
import com.mobi.nubitalcatel.ui.adapters.GridTitleAdapter
import com.mobi.nubitalcatel.ui.adapters.NewsTagsAdapter
import com.mobi.nubitalcatel.ui.adapters.SingleRowTextAdapter
import com.mobi.nubitalcatel.ui.adapters.VideoItemAdapter
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import kotlin.math.max
import kotlin.math.min

// RecyclerView Adapter for Containers
class WidgetAdapter(
    private val containers: HashMap<Int, ContainerOrder>,
    private val viewTypes: HashMap<Int, ViewsOrder>,
    private val widgets: List<WidgetOrder>,
    private var trendingNewsData: List<TopNewsPojo> = emptyList(),
    private val newsCategories: List<TagsPojo> = emptyList(),
    private val showbizNewsData: List<TopNewsPojo> = emptyList(),
    private val worldNewsData: List<TopNewsPojo> = emptyList(),
    private val sportsNewsData: List<TopNewsPojo> = emptyList(),
    private val parentListener: WithTitleTabsViewHolder.OnNewsCategoryClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnTagClickListener {
        fun onTagClick(tag: TopNewsPojo)
    }

    companion object {
        private const val TYPE_WITH_TITLE = 1
        private const val TYPE_NO_TITLE = 2
        private const val TYPE_VIDEO = 3
        private const val TYPE_OTHER_SERVICES = 4
        private const val TYPE_SERVICES = 5
        private const val TYPE_BANNER = 6
        private const val TYPE_UTIL = 7
        private const val TYPE_WEBVIEW = 8
        private const val TYPE_HEALTH = 9
        private const val TYPE_TABS_VIEW = 10
    }

    private val viewPool = RecyclerView.RecycledViewPool().apply {
        // Optimize view pool for better performance
        setMaxRecycledViews(0, 20) // TYPE_WITH_TITLE
        setMaxRecycledViews(1, 20) // TYPE_NO_TITLE
        setMaxRecycledViews(2, 20) // TYPE_VIDEO
        setMaxRecycledViews(3, 20) // TYPE_OTHER_SERVICES
        setMaxRecycledViews(4, 20) // TYPE_SERVICES
        setMaxRecycledViews(5, 20) // TYPE_BANNER
        setMaxRecycledViews(6, 20) // TYPE_UTIL
    }

    init {
        setHasStableIds(true)
        setHasStableIds(true)
    }

//    fun updateFreqApps(newFreqApps: List<Payload>) {
//        this.freqAppsData = newFreqApps
//        notifyDataSetChanged()
//    }

    override fun getItemId(position: Int): Long {
        val id = widgets.getOrNull(position)?.id ?: position
        return id.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val widget = widgets.getOrNull(position) ?: return TYPE_NO_TITLE
        val healthWidget = widgets.getOrNull(position)?.viewTypeId
        val container = containers[widget.containerType]
        val name = container?.containerName
            ?: return if (!widget.title.isNullOrEmpty()) TYPE_WITH_TITLE else TYPE_NO_TITLE
        val n = name.uppercase()

        // Removed logging for performance
        return when {
            healthWidget == 7 -> TYPE_HEALTH
            n.contains("CONTAINER_WEBVIEW") -> TYPE_WEBVIEW
            n.contains("TABS") -> TYPE_TABS_VIEW
            n.contains("WITH_TITLE") -> TYPE_WITH_TITLE
            n.contains("CONTAINER_CAROUSEL_HORIZONTAL_NO_TITLE") -> TYPE_VIDEO
            n.contains("NO_TITLE") -> TYPE_NO_TITLE
            n.contains("CONTAINER_WIDGETS_LAYOUT") -> TYPE_UTIL
            else -> TYPE_NO_TITLE
        }
    }

//
//    override fun getItemViewType(position: Int): Int {
//        val widget = widgets[position]
//        Log.e("WidgetAdapter", "Widget at position $position -> ${widgets[position]}")
//
//        return when {
//            widget.containerTypeDescription == "Video" -> TYPE_VIDEO
//            widget.containerTypeDescription == "SingleContainer" -> TYPE_SERVICES
//            widget.containerTypeDescription == "SingleContainer" -> TYPE_OTHER_SERVICES
//            widget.containerTypeDescription == "Strip" && widget.payload.isEmpty() -> TYPE_BANNER
//            widget.title != null -> TYPE_WITH_TITLE
//            else -> TYPE_NO_TITLE
//        }
//    }

    public fun updateTrendingNews(newData: List<TopNewsPojo>) {
        trendingNewsData = newData
        notifyDataSetChanged()
    }
    private val videoHolders = mutableListOf<VideoListViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_WITH_TITLE -> {
                val binding = LayoutViewWithTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                WithTitleViewHolder(binding, viewPool)
            }

            TYPE_TABS_VIEW -> {
                val binding = LayoutViewWithTabsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                WithTitleTabsViewHolder(binding, viewPool)
            }

            TYPE_UTIL -> {
                val binding = LayoutViewUtilBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                UtilViewHolder(binding, viewPool)
            }

            TYPE_HEALTH -> {
                val binding = LayoutHealthUtilBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HealthViewHolder(binding, parent.context)
            }

            TYPE_VIDEO -> {
                val binding = LayoutNewsVideoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                VideoListViewHolder(binding)
            }

            TYPE_WEBVIEW -> {
                val binding = LayoutViewWebviewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                WebviewViewHolder(binding, viewPool)
            }

            TYPE_OTHER_SERVICES -> {
                val binding = LayoutViewWithTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                OtherServicesViewHolder(binding, viewPool)
            }

            TYPE_SERVICES -> {
                val binding = LayoutViewWithTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ServicesViewHolder(binding, viewPool)
            }

            TYPE_BANNER -> {
                val binding = LayoutViewNoTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                BannerViewHolder(binding)
            }

            else -> {
                val binding = LayoutViewNoTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                NoTitleViewHolder(binding, viewPool)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < 0 || position >= widgets.size) {
            return
        }

        val widget = widgets[position]
        if (widget == null) {
            return
        }

        val container = containers[widget.containerType]
        val viewType = widget.viewTypeId?.let { viewTypes[it] }

//        if (widget.endpointUrl!!.isNotBlank()){
//
//        }

        when (holder) {
            is WithTitleViewHolder -> holder.bind(
                container,
                viewType,
                widget.payload,
                widget.title,
                trendingNewsData,
                newsCategories,
                showbizNewsData,
                worldNewsData,
                sportsNewsData
            )

            is WithTitleTabsViewHolder -> holder.bind(
                container,
                viewType,
                widget.payload,
                widget.title,
                trendingNewsData,
                newsCategories,
                object : WithTitleTabsViewHolder.OnNewsCategoryClickListener {
                    override fun onCategoryClick(id: String, catName: String) {
                        // Send this event to the fragment
                        parentListener.onCategoryClick(id, catName)
                    }
                }
            )

            is NoTitleViewHolder -> holder.bind(container, viewType, widget.payload)
            is UtilViewHolder -> holder.bind(container, viewType, widget.payload)
            is VideoListViewHolder -> {holder.bind(widget.payload)}
            is WebviewViewHolder -> holder.bind(container, viewType, widget.payload, widget.title!!)
            is HealthViewHolder -> holder.bind(container, viewType, widget.payload, widget.title!!)
//            is OtherServicesViewHolder -> holder.bind(widget)
//            is ServicesViewHolder -> holder.bind(widget)
//            is BannerViewHolder -> holder.bind(widget)
        }
    }


//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val widget = widgets[position]
//        val container = containers[widget.containerType]
//        val viewType = widget.id.let { viewTypes[it] }
//        when (holder) {
//            is WithTitleViewHolder -> holder.bind(container, viewType, widget.payload, widget.title)
//            is NoTitleViewHolder -> holder.bind(container, viewType, widget.payload)
//            is VideoListViewHolder -> holder.bind(widget.payload)
//            is OtherServicesViewHolder -> holder.bind(widget)
//            is ServicesViewHolder -> holder.bind(widget)
//            is BannerViewHolder -> holder.bind(widget)
//        }
//    }

    override fun getItemCount(): Int = widgets.size
}

class WithTitleViewHolder(
    private val binding: LayoutViewWithTitleBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        container: ContainerOrder?, viewType: ViewsOrder?, payload: List<Payload>, title: String?,
        trendingNewsData: List<TopNewsPojo>, newsCategories: List<TagsPojo>,
        showbizNewsData: List<TopNewsPojo>,
        worldNewsData: List<TopNewsPojo>,
        sportsNewsData: List<TopNewsPojo>,
//        freqAppsData: List<Payload>
    ) {
        binding.tvTitleView.text = title ?: container?.containerName ?: "Unknown Container"
        binding.btnNewsSeemore.setOnClickListener {
            val intent = Intent(binding.root.context, WebviewActivity::class.java)
            intent.putExtra("url", "https://www.ndtv.com/video/live/channel/ndtv24x7")
            intent.putExtra("title", "Nubit")
            binding.root.context.startActivity(intent)
        }
        binding.recyclerViewTitle.apply {
            layoutManager = LinearLayoutManager(
                binding.root.context,
                if (container?.containerName?.contains("VERTICAL") == true || viewType?.viewType == "hotnews") LinearLayoutManager.VERTICAL
                else LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                isItemPrefetchEnabled = true
                initialPrefetchItemCount = 4
            }

            val effectiveType = viewType?.viewType ?: inferTypeFromTitle(title)
            setRecycledViewPool(pool)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null // Disable animations for better performance
            Log.e("checkviewtpye", "title view>>" + effectiveType)
            when (effectiveType) {
                "hotnews", "sportnews", "news" -> {
                    binding.btnNewsSeemore.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnNewsSeemore.visibility = View.GONE
                }
            }

            adapter = ChildViewAdapter(
                payload,
                effectiveType,
                trendingNewsData,
                newsCategories,
                showbizNewsData,
                worldNewsData,
                sportsNewsData,
                sportsNewsData
            )
        }
    }

    private fun inferTypeFromTitle(title: String?): String? {
        val t = title?.lowercase() ?: return null
        return when {
            t.contains("health") -> "healthUtil"
            t.contains("ram") -> "ramUtil"
            t.contains("weather") -> "weatherUtil"
            else -> null
        }
    }
}


class WithTitleTabsViewHolder(
    private val binding: LayoutViewWithTabsBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        container: ContainerOrder?, viewType: ViewsOrder?, payload: List<Payload>, title: String?,
        trendingNewsData: List<TopNewsPojo>, newsCategories: List<TagsPojo>, listener: OnNewsCategoryClickListener
    ) {
        binding.tvTitleView.text = title ?: container?.containerName ?: "Unknown Container"
        binding.recyclerViewTitleTags.apply {
            layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                isItemPrefetchEnabled = true
            }

            val effectiveType = viewType?.viewType ?: inferTypeFromTitle(title)
            setRecycledViewPool(pool)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null // Disable animations for better performance
            Log.e("checkviewtpye", "title view>>" + effectiveType)

            adapter = NewsTagsAdapter(
                context,
                newsCategories,
                object : NewsTagsAdapter.OnItemClickListener {
                    override fun onItemClick(id: String, catName: String) {
                        // pass click up to parent (Fragment will call API)
                        listener.onCategoryClick(id, catName)
                    }
                }
//                object : NewsTagsAdapter.OnItemClickListener {
//                    override fun onItemClick(item: TopNewsPojo) {
//                        listener.onTagClick(item) // pass click up to parent
//                    }
//                }
            )

        }
        binding.recyclerViewTitleTabs.apply {
            layoutManager = LinearLayoutManager(
                binding.root.context,
                if (container?.containerName?.contains("VERTICAL") == true || viewType?.viewType == "hotnews") LinearLayoutManager.VERTICAL
                else LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                isItemPrefetchEnabled = true
                initialPrefetchItemCount = 4
            }

            val effectiveType = viewType?.viewType ?: inferTypeFromTitle(title)
            setRecycledViewPool(pool)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null // Disable animations for better performance
            Log.e("checkvicewtpye", "title view>>" + effectiveType)

            adapter = ChildViewAdapter(
                payload,
                effectiveType,
                trendingNewsData,
                newsCategories,
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList()
            )
        }
    }

    interface OnNewsCategoryClickListener {
        fun onCategoryClick(id: String, catName: String)
    }
    private fun inferTypeFromTitle(title: String?): String? {
        val t = title?.lowercase() ?: return null
        return when {
            t.contains("health") -> "healthUtil"
            t.contains("ram") -> "ramUtil"
            t.contains("weather") -> "weatherUtil"
            else -> null
        }
    }
}

class NoTitleViewHolder(
    private val binding: LayoutViewNoTitleBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(container: ContainerOrder?, viewType: ViewsOrder?, payload: List<Payload>) {
        binding.recyclerViewNoTitle.layoutManager = LinearLayoutManager(
            binding.root.context,
            if (container?.containerName?.contains("VERTICAL") == true || viewType?.viewType == "news") LinearLayoutManager.VERTICAL
            else LinearLayoutManager.HORIZONTAL,
            false
        )
        val effectiveType = viewType?.viewType
        binding.recyclerViewNoTitle.apply {
            setRecycledViewPool(pool)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
        Log.e("checkviewtpye", "no title view>>" + effectiveType)

        binding.recyclerViewNoTitle.adapter = ChildViewAdapter(payload, effectiveType)
    }
}

class UtilViewHolder(
    private val binding: LayoutViewUtilBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(container: ContainerOrder?, viewType: ViewsOrder?, payload: List<Payload>) {

        val effectiveType = viewType?.viewType
        Log.e("checkvicewtpye", "util view>>" + effectiveType)

        binding.recyclerViewNoTitle.apply {
            layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false
            ).apply {
                isItemPrefetchEnabled = true
                initialPrefetchItemCount = 4
            }

            val effectiveType = viewType?.viewType
            setRecycledViewPool(pool)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null // Disable animations for better performance
            Log.e("checkvicewctpye", "title view>>" + effectiveType)

            adapter = ChildViewAdapter(
                payload,
                effectiveType,
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList()
            )
        }
    }
}

class WebviewViewHolder(
    private val binding: LayoutViewWebviewBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        container: ContainerOrder?,
        viewType: ViewsOrder?,
        payload: List<Payload>,
        title: String
    ) {
        // Configure WebView settings
        binding.tvTitleView.setText(title)
        val s = binding.webviewMain.settings
        s.javaScriptEnabled = true // Enable JavaScript (required for many websites)
        s.domStorageEnabled = true // Enable DOM storage for web storage APIs
        s.useWideViewPort = true // Support viewport meta tags for responsive design
        s.loadWithOverviewMode = true // Fit content to WebView size
        s.builtInZoomControls = true // Optional: Enable zoom controls
        s.displayZoomControls = false // Optional: Hide zoom controls UI

        binding.webviewMain.scrollBarStyle =
            View.SCROLLBARS_INSIDE_OVERLAY // Ensure scrollbars don't overlap content
        binding.webviewMain.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http:") || url.startsWith("https:")) {
//                    view.loadUrl(url) // Load HTTP/HTTPS URLs in WebView
                    val parts = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val lastPart = parts[parts.size - 1]
                    println("Last part of the URL: " + correctUrl(lastPart))
                    val intent = Intent(binding.root.context, WebviewActivity::class.java)
                    intent.putExtra("url", url)
                    intent.putExtra("title", "Nubit")
                    binding.root.context.startActivity(intent)
                    return true
                }
                return false // Let system handle non-HTTP URLs (e.g., tel:, mailto:)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // Handle URL loading for Android Nougat (API 24) and above
                val url = request.url.toString()
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url)
                    return true
                }
                return false // Let system handle non-HTTP URLs
            }

            override fun onPageFinished(view: WebView, url: String) {
                // Optional: Called when page finishes loading
                Log.d("WebView", "Page loaded: $url")
            }
        }
        binding.webviewMain.setDownloadListener { url: String?, userAgent: String?, contentDisposition: String?, mimetype: String?, contentLength: Long ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(url))
            binding.webviewMain.context.startActivity(intent)
        }
        binding.webviewMain.loadUrl("https://www.vistory.mobi/minus-one")
//        binding.webviewMain.setWebViewClient(object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                Log.e("checkurlss", ">>$url")
//
//                return true // Returning true means the host application handles the URL
//            }
//        })
    }

    private fun correctUrl(url: String): String {
        return try {
            // Decode the URL to replace URL-encoded characters with their original representations
            URLDecoder.decode(url, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            url // Return original URL if decoding fails
        }
    }
}

class VideoListViewHolder(private val binding: LayoutNewsVideoBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(payload: List<Payload>) {
        // Convert Payload to NewsVideoPojo for compatibility
//        val videos = payload.map { NewsVideoPojo(it.title!!, it.endpointUrl!!, it.heroImageUrl!!) }
        val videos = payload.map {
            val heroUrl = it.heroImageUrl
            val isImage = heroUrl != null && listOf(".jpeg", ".jpg", ".png").any { ext ->
                heroUrl.contains(ext, ignoreCase = true)
            }

            if (isImage) {
                NewsVideoPojo(
                    title = it.title ?: "",
                    url = it.heroImageUrl ?: "",
                    action = "banner",
                    redirectLink = it.endpointUrl ?: ""
                )
            } else {
                NewsVideoPojo(
                    title = it.title ?: "",
                    url = it.heroImageUrl ?: "",
                    action = "video",
                    redirectLink = it.heroImageUrl ?: ""
                )
            }

        }
        val videoAdapter = VideoItemAdapter(videos, binding.recyclerVierwsDy)
        binding.recyclerVierwsDy.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = videoAdapter
        }

        binding.recyclerVierwsDy.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    videoAdapter.playVisibleVideo()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            private fun playVisibleVideo() {
                val layoutManager =
                    binding.recyclerVierwsDy.getLayoutManager() as LinearLayoutManager
                        ?: return
                for (i in layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()) {
                    val view = layoutManager.findViewByPosition(i)
                    if (view != null) {
                        val height = view.height
                        val visibleHeight =
                            min(binding.recyclerVierwsDy.getHeight(), view.bottom) - max(
                                0,
                                view.top
                            )
                        val visiblePercentage = visibleHeight.toFloat() / height
                        if (visiblePercentage > 0.8f) {
                            videoAdapter.playVideoAtPosition(i)
                            break
                        }
                    }
                }
            }
        })

        // Start at center position for endless scrolling
        binding.recyclerVierwsDy.post {
            val centerPosition = videoAdapter.getCenterPosition()
            binding.recyclerVierwsDy.scrollToPosition(centerPosition)
        }
    }

}

class OtherServicesViewHolder(
    private val binding: LayoutViewWithTitleBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(widget: WidgetOrder) {
        binding.tvTitleView.text = widget.title ?: "Services"
        // Use payload items instead of static list
        val services = widget.payload.map {
            OtherServicesPojo(it.title, it.heroImageUrl ?: "", "", it.heroImageUrl, it.thumbnailUrl)
        }
        val adapter = SingleRowTextAdapter(binding.root.context, services)
        binding.recyclerViewTitle.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            setRecycledViewPool(pool)
            this.adapter = adapter
        }
    }
}

class ServicesViewHolder(
    private val binding: LayoutViewWithTitleBinding,
    private val pool: RecyclerView.RecycledViewPool
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(widget: WidgetOrder) {
        binding.tvTitleView.text = widget.title ?: ""
        val services = widget.payload.map {
            Services_pojo(it.title, it.heroImageUrl ?: "", "", it.heroImageUrl, it.thumbnailUrl)
        }
        val servicesAdapter = GridTitleAdapter(binding.root.context, services)
        binding.recyclerViewTitle.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            setRecycledViewPool(pool)
            adapter = servicesAdapter
        }
    }
}


class BannerViewHolder(private val binding: LayoutViewNoTitleBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(widget: WidgetOrder) {
        // Placeholder for banner; use widget.heroImageUrl or thumbnailUrl if available
        if (widget.heroImageUrl != null) {
//            Glide.with(binding.root.context)
//                .load(widget.heroImageUrl)
//                .placeholder(R.drawable.ic_image_placeholder)
//                .into(binding.recyclerViewNoTitle) // Assumes recyclerViewNoTitle can display an image
        }
    }
}

// RecyclerView Adapter for Child Views - Performance Optimized
class ChildViewAdapter(
    private val payload: List<Payload>,
    private val viewType: String?,
    private var trendingNewsData: List<TopNewsPojo> = emptyList(),
    private val newsCategories: List<TagsPojo> = emptyList(),
    private val showbizData: List<TopNewsPojo> = emptyList(),
    private val worldData: List<TopNewsPojo> = emptyList(),
    private val sportsNewsData: List<TopNewsPojo> = emptyList(),
    private val sportsBannerNewsData: List<TopNewsPojo> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Pre-computed data mappings for performance
    private val newsDataMap by lazy {
        trendingNewsData.associateBy { it.title }
    }

    // Pre-computed placeholder objects to avoid creation during binding
    private val placeholderPayload by lazy { createPlaceholderPayload() }
    private val placeholderNewsData by lazy { createPlaceholderNewsData() }

    companion object {
        private const val TYPE_FREQUENT_APPS = 1
        private const val TYPE_NEWS = 2
        private const val TYPE_SPORT_NEWS = 3
        private const val TYPE_OTHER = 4
        private const val TYPE_SERVICES = 5
        private const val TYPE_HEALTH_UTIL = 6
        private const val TYPE_RAM_UTIL = 7
        private const val TYPE_WEATHER_UTIL = 8
        private const val TYPE_SHOWBIZ = 9
        private const val TYPE_WORLD_NEWS = 10
        private const val TYPE_STRIP = 11
        private const val TYPE_PENTA_VIEW = 12
        private const val TYPE_RAM_WIDGET = 13
        private const val TYPE_NEW_BANNER = 14
        private const val TYPE_TOP_APPS = 15
        private const val TYPE_CARASUAL = 16
        private const val TYPE_OVERFLOW_TEXT = 17
        private const val TYPE_NEW_BANNER_VERTICAL = 18
        private const val TYPE_SPORT_SCORE = 19

    }

    override fun getItemViewType(position: Int): Int {
        return when (viewType) {
            "service" -> TYPE_SERVICES
            "hotnews" -> TYPE_NEWS
            "topapps" -> TYPE_TOP_APPS
            "showbiz" -> TYPE_SHOWBIZ
            "news" -> TYPE_WORLD_NEWS
            "frequentapps" -> TYPE_FREQUENT_APPS
            "myhealth" -> TYPE_HEALTH_UTIL
            "sportnews" -> TYPE_SPORT_NEWS
            "score" -> TYPE_SPORT_SCORE
            "strip" -> TYPE_STRIP
            "ramWidget" -> TYPE_RAM_WIDGET
            "weatherWidget" -> TYPE_WEATHER_UTIL
//            "pentaView" -> TYPE_PENTA_VIEW
            "newBannerPost" -> if (position == 0) TYPE_NEW_BANNER else TYPE_NEW_BANNER_VERTICAL
            "overFlowStrip" -> TYPE_OVERFLOW_TEXT
            "carasual" -> TYPE_CARASUAL
            else -> TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.e("checkginalviews", ">>" + viewType)

        return when (viewType) {
            TYPE_SERVICES -> {
                val binding = GridTitleItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ServicesChildViewHolder(binding, parent.context)
            }

            TYPE_FREQUENT_APPS -> {
                val binding = GridTitleItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                FrequentAppsViewHolder(binding, parent.context)
            }

            TYPE_NEWS -> {
                val binding = NewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                NewsViewHolder(binding, parent.context)
            }

            TYPE_OVERFLOW_TEXT -> {
                val binding = RowTagItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                RowTagsNewsViewHolder(binding, parent.context)
            }

            TYPE_TOP_APPS -> {
                val binding = GridTitleItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                FrequentAppsViewHolder(binding, parent.context)
            }

            TYPE_SPORT_NEWS -> {
                val binding = NewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SportsNewsViewHolder(binding, parent.context)
            }

            TYPE_SPORT_SCORE -> {
                val binding = GameTwoItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ScoreNewsViewHolder(binding, parent.context)
            }

            TYPE_WORLD_NEWS -> {
                val binding = NewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                WorldNewsViewHolder(binding, parent.context)
            }

            TYPE_SHOWBIZ -> {
                val binding = VerticalNewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ShowbizViewHolder(binding, parent.context)
            }

            TYPE_HEALTH_UTIL -> {
                val binding = LayoutHealthUtilBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HealthViewHolder(binding, parent.context)
            }

            TYPE_RAM_WIDGET -> {
                val binding = LayoutWeatherRamBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                RamViewHolder(binding, parent.context)
            }

            TYPE_WEATHER_UTIL -> {
                val binding = LayoutBattClockUtilBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                BattViewHolder(binding, parent.context)
            }

            TYPE_NEW_BANNER -> {
                val binding = ImgNewsAdapterItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SportsBannerViewHolder(binding, parent.context)
            }

            TYPE_NEW_BANNER_VERTICAL -> {
                val binding = NewsItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SportsBannerVerticalViewHolder(binding, parent.context)
            }

            TYPE_STRIP -> {
                val binding = SingleRowTextItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                StripNewsViewHolder(binding, parent.context)
            }
//            TYPE_CARASUAL -> {
//                val binding = LayoutNewsVideoBinding.inflate(
//                    LayoutInflater.from(parent.context), parent, false
//                )
//                VideoListViewHolder(binding)
//            }
            else -> {
                val binding = SingleRowTextItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                StripNewsViewHolder(binding, parent.context)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Optimized binding with minimal object creation
        when (holder) {
            is FrequentAppsViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                holder.bind(item)
            }

            is ServicesChildViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                holder.bind(item)
            }

            is NewsViewHolder -> {
                val newsDataItem =
                    if (position < trendingNewsData.size) trendingNewsData[position] else placeholderNewsData
                val isLastItem = position == trendingNewsData.size - 1
                holder.bind(newsDataItem, isLastItem)
            }

            is RowTagsNewsViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                val newsDataItem =
                    //if (position < payload.size) payload[position] else placeholderPayload
                    if (position < trendingNewsData.size) trendingNewsData[position] else placeholderNewsData
                val isLastItem = position == trendingNewsData.size - 1
                holder.bind(item,isLastItem)
            }

            is SportsNewsViewHolder -> {
                val newsDataItem =
                    if (position < sportsNewsData.size) sportsNewsData[position] else placeholderNewsData
                val isLastItem = position == sportsNewsData.size - 1
                holder.bind(newsDataItem, isLastItem)
            }

            is ScoreNewsViewHolder -> {
                val isLastItem = position == payload.size - 1
                val item = if (position < payload.size) payload[position] else placeholderPayload

                holder.bind(item, isLastItem)
            }

            is ShowbizViewHolder -> {
                val newsDataItem =
                    if (position < showbizData.size) showbizData[position] else placeholderNewsData

                holder.bind(newsDataItem)
            }

            is SportsBannerViewHolder -> {
                val newsDataItem = sportsBannerNewsData.firstOrNull() ?: placeholderNewsData
                val isLastItem = position == sportsBannerNewsData.size - 1

                holder.bind(newsDataItem,isLastItem)
            }

            is SportsBannerVerticalViewHolder -> {
                val baseSize = sportsBannerNewsData.size
                val index = position - 1
                val newsDataItem =
                    if (index in 0 until baseSize) sportsBannerNewsData[index] else placeholderNewsData
                val isLastItem = position == sportsBannerNewsData.size - 1

                holder.bind(newsDataItem,isLastItem)
            }

            is WorldNewsViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                val newsDataItem =
                    if (position < worldData.size) worldData[position] else placeholderNewsData
                val isLastItem = position == worldData.size - 1

                holder.bind(newsDataItem,isLastItem)
            }

            is RamViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                holder.bind(item)
            }

            is BattViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                holder.bind(item)
            }

            is StripNewsViewHolder -> {
                val item = if (position < payload.size) payload[position] else placeholderPayload
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int {
        val utilType =
            viewType == "healthUtil" || viewType == "ramWidget" || viewType == "weatherWidget"
        val newsType =
            viewType == "hotnews" || viewType == "showbiz" || viewType == "sportnews" || viewType == "news"
        val bannerType = viewType == "newBannerPost"
        return when {
            utilType && payload.isEmpty() -> 1
            newsType -> maxOf(trendingNewsData.size)
            bannerType -> if (sportsBannerNewsData.isNotEmpty()) sportsBannerNewsData.size + 1 else 0
            else -> payload.size
        }
    }

    private fun createPlaceholderPayload(): Payload {
        return Payload(
            id = -1,
            orderSequence = 0,
            containerType = 0,
            containerTypeDescription = null,
            title = null,
            endpointUrl = null,
            heroImageUrl = null,
            thumbnailUrl = null,
            defaultFocus = false
        )
    }

    private fun createPlaceholderNewsData(): TopNewsPojo {
        return TopNewsPojo(
            title = null,
            description = null,
            imageUrl = null,
        )
    }
}

class FrequentAppsViewHolder(
    private val binding: GridTitleItemBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    private val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_image_placeholder)
        .centerCrop()

    fun bind(item: Payload) {
        val layoutParams = binding.root.layoutParams
        val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
        layoutParams.width = (screenWidth / 4.8).toInt() // Adjust divisor for spacing
        binding.root.layoutParams = layoutParams
        binding.serviceGridText.text = item.title
        val imageUrl = item.heroImageUrl ?: item.thumbnailUrl
        if (imageUrl != null) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(binding.serviceGridImage)
        } else {
            binding.serviceGridImage.setImageResource(R.drawable.ic_image_placeholder)
        }
        binding.llCardMain.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", item.endpointUrl)
            intent.putExtra("title", item.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class ServicesChildViewHolder(
    private val binding: GridTitleItemBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    private val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_image_placeholder)
        .centerCrop()

    fun bind(item: Payload) {
        // Set dynamic width — show 4 items per row with spacing
        val layoutParams = binding.root.layoutParams
        val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
        layoutParams.width = (screenWidth / 4.8).toInt() // Adjust divisor to tune spacing
        binding.root.layoutParams = layoutParams

        // Bind data
        binding.serviceGridText.text = item.title
        val imageUrl = item.heroImageUrl ?: item.thumbnailUrl
        if (imageUrl != null) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(binding.serviceGridImage)
        } else {
            binding.serviceGridImage.setImageResource(R.drawable.ic_image_placeholder)
        }

        binding.llCardMain.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", item.endpointUrl)
            intent.putExtra("title", item.feedProviderName)
            context.startActivity(intent)
        }
    }
}


class NewsViewHolder(
    private val binding: NewsItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        newsData: TopNewsPojo,
        isLastItem: Boolean
    ) {
        // Convert TopNewsPojo to Payload format and use it
//        val newsItem = newsData.find { it.title == item.title }?.toPayload() ?: item
        Log.e("checkdatanws", "news>>" + newsData.title + ">>" + newsData.feedProvider)

        binding.txtTopNewTitle.text = newsData.title
        binding.txtTopNewDes.text = newsData.description

        val imageUrl = newsData.imageUrl ?: ""
        val feedProvider = newsData.feedProvider ?: ""

        Glide.with(binding.root.context)
            .load(feedProvider)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.ivNewsBy)

        Glide.with(binding.root.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.imgTopNews)


        binding.dividerLine.isVisible = !isLastItem
        binding.rlNews.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class RowTagsNewsViewHolder(
    private val binding: RowTagItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(newsData: Payload,
             isLastItem: Boolean) {
        // Convert TopNewsPojo to Payload format and use it
//        val newsItem = newsData.find { it.title == item.title }?.toPayload() ?: item
        Log.e("checkdatacdnws", "news>>" + newsData.title + ">>" + newsData.feedProvider)

        binding.txtTopNewTitle.text = newsData.title
        binding.txtTopNewTitle.setSelected(true)
        val imageUrl = newsData.feedProvider ?: ""

        Glide.with(binding.root.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.imgTopNews)


        binding.layoutTopNewsItems.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class SportsNewsViewHolder(
    private val binding: NewsItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        newsData: TopNewsPojo,
        isLastItem: Boolean
    ) {
//        val newsItem = newsData.find { it.title == item.title }?.toPayload() ?: item
        Log.e("checkdatanws", "news>>" + newsData.title + ">>" + newsData.feedProvider)

        binding.txtTopNewTitle.text = newsData.title
        binding.txtTopNewDes.text = newsData.description

        val imageUrl = newsData.imageUrl ?: ""
        val feedProvider = newsData.feedProvider ?: ""

        Glide.with(binding.root.context)
            .load(feedProvider)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.ivNewsBy)

        Glide.with(binding.root.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.imgTopNews)

        binding.dividerLine.isVisible = !isLastItem
        binding.rlNews.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }

    }
}

class ScoreNewsViewHolder(
    private val binding: GameTwoItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        newsData: Payload,
        isLastItem: Boolean
    ) {
//        val newsItem = newsData.find { it.title == item.title }?.toPayload() ?: item
        Log.e("chceeckdcdatanws", "11>>" + newsData.title + ">>" + newsData.feedProvider)
        val rawTitle = newsData.title?.replace("&amp;", "&")?.trim() ?: ""
        val parts = rawTitle.split(" v ")
        Log.e("chceeckdcdatanws", "22>>" + parts)

        if (parts.size == 2) {
            val team1Raw = parts[0].trim()
            val team2Raw = parts[1].trim()

            // Extract name (first word) and score (rest)
            val team1Split = team1Raw.split(" ").filter { it.isNotBlank() }
            val team2Split = team2Raw.split(" ").filter { it.isNotBlank() }

            val team1Name = team1Split.firstOrNull() ?: ""
            val team1Score = team1Split.drop(1).joinToString(" ")

            val team2Name = team2Split.firstOrNull() ?: ""
            val team2Score = team2Split.drop(1).joinToString(" ")

            // Set title and subtitle
            binding.txtTopTitle.text = "$team1Name vs $team2Name match"
            binding.txtTopSubTitle.text = "From ${newsData.feedProviderName ?: "Live update"}"

            // Set team names and scores
            binding.team1Name.text = team1Name
            binding.team1Score.text = team1Score
            binding.team2Name.text = team2Name
            binding.team2Score.text = team2Score
        } else {
            // Fallback if format doesn’t have “v”
            binding.txtTopTitle.text = rawTitle
            binding.txtTopSubTitle.text = "From ${newsData.feedProviderName ?: "Live update"}"

            binding.team1Name.text = ""
            binding.team1Score.text = ""
            binding.team2Name.text = ""
            binding.team2Score.text = ""
        }


//        val imageUrl = newsData.imageUrl ?: ""
//        val feedProvider = newsData.feedProvider ?: ""
//
//        Glide.with(binding.root.context)
//            .load(feedProvider)
//            .placeholder(R.drawable.ic_image_placeholder)
//            .into(binding.ivNewsBy)
//
//        binding.dividerLine.isVisible = !isLastItem
//        binding.rlNews.setOnClickListener {
//            val intent = Intent(context, WebviewActivity::class.java)
//            intent.putExtra("url", newsData.redirectLink)
//            context.startActivity(intent)
//        }

    }
}

class ShowbizViewHolder(
    private val binding: VerticalNewsItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_image_placeholder)
        .centerCrop()

    fun bind(newsData: TopNewsPojo) {
        Log.e("checkdatanws", "showbiz>>" + newsData.title)
        binding.txtExternalGamesName.text = newsData.title
        val imageUrl = newsData.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(binding.externalGamesIcon)
        } else {
            binding.externalGamesIcon.setImageResource(R.drawable.ic_image_placeholder)
        }
        binding.externalGamesCardview.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class SportsBannerViewHolder(
    private val binding: ImgNewsAdapterItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_placeholder)
        .error(R.drawable.ic_image_placeholder)
        .centerCrop()

    fun bind(newsData: TopNewsPojo,
             isLastItem: Boolean) {
        Log.e("checkdfratanws", "showbiz>>" + newsData.title)
        binding.txtTopNewTitle.text = newsData.title
        val imageUrl = newsData.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(binding.imgTopNews)
        } else {
            binding.imgTopNews.setImageResource(R.drawable.ic_image_placeholder)
        }
        binding.layoutTopNewsItems.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class SportsBannerVerticalViewHolder(
    private val binding: NewsItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(newsData: TopNewsPojo,
             isLastItem: Boolean) {
        Log.e("checkdfratanws", "banner-vertical>>" + isLastItem)
        binding.txtTopNewTitle.text = newsData.title
        binding.txtTopNewDes.text = newsData.description

        val imageUrl = newsData.imageUrl ?: ""
        val feedProvider = newsData.feedProvider ?: ""

        Glide.with(binding.root.context)
            .load(feedProvider)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.ivNewsBy)

        if (imageUrl.isNotEmpty()) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.imgTopNews)
        } else {
            binding.imgTopNews.setImageResource(R.drawable.ic_image_placeholder)
        }

        binding.dividerLine.isVisible = !isLastItem
        binding.rlNews.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class WorldNewsViewHolder(
    private val binding: NewsItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(newsData: TopNewsPojo,
             isLastItem: Boolean) {
        // Convert TopNewsPojo to Payload format and use it
//        val newsItem = newsData.find { it.title == item.title }?.toPayload() ?: item
        Log.e("checkspdatanws", "world>>" + newsData.title)

        binding.txtTopNewTitle.text = newsData.title
        binding.txtTopNewDes.text = newsData.description

        val imageUrl = newsData.imageUrl
        val feedProvider = newsData.feedProvider ?: ""

        Glide.with(binding.root.context)
            .load(feedProvider)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.ivNewsBy)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.imgTopNews)
        } else {
            binding.imgTopNews.setImageResource(R.drawable.ic_image_placeholder)
        }
        binding.dividerLine.isVisible = !isLastItem

        binding.rlNews.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.redirectLink)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class StripNewsViewHolder(
    private val binding: SingleRowTextItemBinding,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(newsData: Payload) {
        binding.txtOtherSer.text = newsData.title
        binding.txtOtherSer.setOnClickListener {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra("url", newsData.endpointUrl)
            intent.putExtra("title", newsData.feedProviderName)
            context.startActivity(intent)
        }
    }
}

class HealthViewHolder(
    private val binding: LayoutHealthUtilBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        container: ContainerOrder?,
        viewType: ViewsOrder?,
        payload: List<Payload>,
        cardTitle: String
    ) {

        binding.btnNutriaideTitle.text = cardTitle
        binding.cdNutraide.setOnClickListener {
            Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
}

class RamViewHolder(
    private val binding: LayoutWeatherRamBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Payload) {
        binding.layoutRam.ivCleanRam.setOnClickListener {
            try {
                cleanCache(context)
                simulateRamCleaning(binding.layoutRam.tvTotalRAM)
                startImageAnimation(binding.layoutRam.ivCleanRam)
                displayCacheSize(context, binding.layoutRam.tvTotalRAM)
                releaseMemory()
            } catch (e: java.lang.Exception) {
            }
        }
    }
}

class BattViewHolder(
    private val binding: LayoutBattClockUtilBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Payload) {
        binding.layoutBatt.ivBattery.setOnClickListener {
            try {
                openBatteryOptimizationSettings(context)
            } catch (e: Exception) {
            }
        }
    }
}

fun openBatteryOptimizationSettings(context: Context) {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    } else {
        // For devices below API 23, direct battery optimization settings are not available.
        intent.setAction(Intent.ACTION_POWER_USAGE_SUMMARY)
    }
    if (intent.resolveActivity(context.getPackageManager()) != null) {
        context.startActivity(intent)
    } else {
        // Handle the case where the intent cannot be resolved.
        Toast.makeText(context, "Battery settings not available", Toast.LENGTH_SHORT).show()
    }
}

// Clean the cache by deleting all files in the cache directory
private fun cleanCache(context: Context) {
    try {
        val cacheDir: File = context.getCacheDir()
        deleteDirectory(cacheDir)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

// Delete all files in a directory recursively
private fun deleteDirectory(directory: File?) {
    if (directory != null && directory.isDirectory) {
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }
    }
}

// Release unused memory (optional)
private fun releaseMemory() {
    System.gc() // Request garbage collection
}

// Display cache size in TextView
private fun displayCacheSize(context: Context, tvTotalRAM: TextView) {
    val cacheDir: File = context.getCacheDir()
    val cacheSize = getDirectorySize(cacheDir)
    Handler(Looper.getMainLooper()).post {
        tvTotalRAM.setText(
            formatSize(cacheSize)
        )
    }
}

// Animate the ImageView
private fun startImageAnimation(ivCleanRam: ImageView) {
    val rotate = RotateAnimation(
        0f, 360f,  // Start and end angle
        Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot X (center)
        Animation.RELATIVE_TO_SELF, 0.5f // Pivot Y (center)
    )
    rotate.duration = 1000 // 1-second duration for one rotation
    rotate.repeatCount = Animation.INFINITE // Infinite repeat
    ivCleanRam.startAnimation(rotate)

    // Stop the animation after RAM cleaning is done
    ivCleanRam.postDelayed(Runnable { ivCleanRam.clearAnimation() }, 3000)
}

// Simulate cleaning RAM with animation
private fun simulateRamCleaning(tvTotalRAM: TextView) {
    val currentRam: Long = tvTotalRAM.getText().toString().trim { it <= ' ' }.toLong()
    val animator = ValueAnimator.ofInt(currentRam.toInt(), 0) // From current RAM to 0
    animator.setDuration(3000) // 3 seconds duration
    Handler(Looper.getMainLooper()).post {
        animator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.animatedValue as Int
            tvTotalRAM.setText(animatedValue.toString())
        }
    }
    animator.start()
}

// Calculate the total size of files in a directory
private fun getDirectorySize(directory: File?): Long {
    var size: Long = 0
    if (directory != null && directory.isDirectory) {
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
    }
    return size
}

// Format size from bytes to a human-readable format
private fun formatSize(size: Long): String {
    val kb = size / 1024
    val mb = kb / 1024
    return if (mb > 0) {
        mb.toString() + ""
    } else {
        kb.toString() + ""
    }
}


//package com.mobi.nubitalcatel.ui.adapters
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.mobi.nubitalcatel.core.models.ContainerOrder
//import com.mobi.nubitalcatel.core.models.External_game_pojo
//import com.mobi.nubitalcatel.core.models.NewsVideoPojo
//import com.mobi.nubitalcatel.core.models.OtherServicesPojo
//import com.mobi.nubitalcatel.core.models.Payload
//import com.mobi.nubitalcatel.core.models.Services_pojo
//import com.mobi.nubitalcatel.core.models.TopNewsPojo
//import com.mobi.nubitalcatel.core.models.ViewsOrder
//import com.mobi.nubitalcatel.core.models.WidgetItems
//import com.mobi.nubitalcatel.core.models.WidgetOrder
//import com.mobi.nubitalcatel.databinding.LayoutNewsVideoBinding
//import com.mobi.nubitalcatel.databinding.LayoutViewNoTitleBinding
//import com.mobi.nubitalcatel.databinding.LayoutViewWithTitleBinding
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//
//
////class WidgetAdapter(
////    private val items: List<WidgetItems>
////) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
////
////    private val viewHolderFactories: Map<Int, (LayoutInflater, ViewGroup) -> RecyclerView.ViewHolder> =
////        mapOf(
////            1 to { inflater, parent -> BannerViewHolder(LayoutViewNoTitleBinding.inflate(inflater, parent, false)) },
////            2 to { inflater, parent -> VideoListViewHolder(LayoutNewsVideoBinding.inflate(inflater, parent, false)) },
////            3 to { inflater, parent -> NewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            4 to { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            5 to { inflater, parent -> ServicesViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            6 to { inflater, parent -> VerticalNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            7 to { inflater, parent -> BannerViewHolder(LayoutViewNoTitleBinding.inflate(inflater, parent, false)) },
////            8 to { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            9 to { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            10 to { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////            11 to { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) },
////        )
////
////    override fun getItemViewType(position: Int): Int {
////        return when (val item = items[position]) {
////            is WidgetItems.CarouselWidget -> item.data.containerType
////            is WidgetItems.BannerWidget -> item.data.containerType
////            is WidgetItems.GridWidget -> item.data.containerType
////            is WidgetItems.ServicesWidget -> item.data.containerType
////            is WidgetItems.VerticalNewsWidget -> item.data.containerType
////            is WidgetItems.RowTagsNewsWidget -> item.data.containerType
////        }
////    }
////
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
////        val inflater = LayoutInflater.from(parent.context)
////        return viewHolderFactories[viewType]?.invoke(inflater, parent)
////            ?: throw IllegalArgumentException("Invalid viewType $viewType")
////    }
////
////    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
////        when (val item = items[position]) {
////            is WidgetItems.CarouselWidget -> (holder as VideoListViewHolder).bind(item.videos)
////            is WidgetItems.BannerWidget -> (holder as BannerViewHolder).bind(item.data,item.banners)
////            is WidgetItems.GridWidget -> (holder as NewsViewHolder).bind(item.data, item.newsList)
////            is WidgetItems.ServicesWidget -> (holder as ServicesViewHolder).bind(item.data, item.servicesList)
////            is WidgetItems.VerticalNewsWidget -> (holder as VerticalNewsViewHolder).bind(item.data, item.verticalNewsList)
////            is WidgetItems.RowTagsNewsWidget -> (holder as RowTagsNewsViewHolder).bind(item.data, item.rowTagsNewsList)
////
////        }
////    }
////
////    override fun getItemCount() = items.size
////
////    class VideoListViewHolder(private val binding: LayoutNewsVideoBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(videos: List<NewsVideoPojo>) {
////            val videoAdapter = VideoItemAdapter(videos, binding.recyclerVierwsDy)
////            binding.recyclerVierwsDy.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = videoAdapter
////            }
////        }
////    }
////
////    class BannerViewHolder(private val binding: LayoutViewNoTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(widget: WidgetOrder, bannerList: ArrayList<External_game_pojo>) {
////            // Example: use Glide to load banner image
////            val newsAdapter = RowBannerItemAdapter(
////                context = binding.root.context,
////                 games = bannerList,
////            )
////
////            binding.recyclerViewNoTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = newsAdapter
////            }
////            widget.heroImageUrl?.let { url ->
////            }
////        }
////    }
////
////    class NewsViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(widget: WidgetOrder, newsList: List<TopNewsPojo>) {
////            binding.tvTitleView.text = widget.title ?: ""
////
////            val newsAdapter = NewsAdapter(
////                context = binding.root.context,
////                items = newsList,
////                topCateName = widget.title ?: ""
////            )
////
////            binding.recyclerViewTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = newsAdapter
////            }
////        }
////    }
////
////    class ServicesViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(widget: WidgetOrder, servicesList: List<Services_pojo>) {
////            binding.tvTitleView.text = widget.title ?: ""
////
////            val newsAdapter = GridTitleAdapter(
////                context = binding.root.context,
////                serviceList = servicesList,
////            )
////
////            binding.recyclerViewTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = newsAdapter
////            }
////        }
////    }
////
////    class VerticalNewsViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(widget: WidgetOrder, servicesList: List<TopNewsPojo>) {
////            binding.tvTitleView.text = widget.title ?: ""
////
////            val newsAdapter = VerticalNewsAdapter(
////                context = binding.root.context,
////                items = servicesList,
////                cardTitle = ""
////            )
////
////            binding.recyclerViewTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = newsAdapter
////            }
////        }
////    }
////
////    class RowTagsNewsViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(widget: WidgetOrder, servicesList: List<TopNewsPojo>) {
////            binding.tvTitleView.text = widget.title ?: ""
////
////            val tagsNewsAdapter = RowTagAdapter(
////                context = binding.root.context,
////                dataList = servicesList,
////            )
////
////            binding.recyclerViewTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = tagsNewsAdapter
////            }
////        }
////    }
////    fun Payload.toNewsVideoPojo(viewType: ViewsOrder): NewsVideoPojo {
////        return NewsVideoPojo(
////            title = title ?: viewType.viewDescription,
////            url = endpointUrl ?: ""
////        )
////    }
////}
//
//class WidgetAdapter(
//    private val viewTypes: List<ViewsOrder>,
//    private val widgetContainers: HashMap<Int, ContainerOrder>
//) : ListAdapter<WidgetItems, RecyclerView.ViewHolder>(WidgetDiffCallback()) {
//
//    private val viewPool = RecyclerView.RecycledViewPool()
//    private val viewTypeMap = viewTypes.associateBy { it.id }
//
//    private val viewHolderFactories: Map<Int, (LayoutInflater, ViewGroup) -> RecyclerView.ViewHolder> =
//        widgetContainers.values.associate { container ->
//            container.id to when (container.containerName) {
//                "CONTAINER_CAROUSEL_HORIZONTAL_NO_TITLE" ->
//                    { inflater, parent -> VideoListViewHolder(LayoutNewsVideoBinding.inflate(inflater, parent, false)) }
////                "CONTAINER_CAROUSEL_VERTICAL", "CONTAINER_CAROUSEL_HORIZONTAL_WITH_TITLE" ->
////                    { inflater, parent -> VideoListViewHolder(LayoutNewsVideoBinding.inflate(inflater, parent, false)) }
//                "CONTAINER_HORIZONTAL_LAYOUT_WITH_TITLE", "CONTAINER_HORIZONTAL_LAYOUT_WITHOUT_TITLE", "CARD_HORIZONTAL_LAYOUT_WITH_TITLE_TABS" ->
//                    { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) }
//                "CONTAINER_VERTICAL_LAYOUT_WITH_TITLE", "CONTAINER_VERTICAL_LAYOUT_WITHOUT_TITLE" ->
//                    { inflater, parent -> VerticalNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) }
//                "CONTAINER_STRIP_SINGLE" ->
//                    { inflater, parent -> RowTagsNewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) }
//                "CONTAINER_WIDGETS_LAYOUT" ->
//                    { inflater, parent -> ServicesViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) }
//                "CONTAINER_CONTAINER_SCORES_INFO" ->
//                    { inflater, parent -> NewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) }
//                else ->
//                    { inflater, parent -> DefaultViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false)) }
//            }
//        }
//
//    override fun getItemViewType(position: Int): Int {
//        return when (val item = getItem(position)) {
//            is WidgetItems.CarouselWidget -> item.data.containerType
//            is WidgetItems.BannerWidget -> item.data.containerType
//            is WidgetItems.GridWidget -> item.data.containerType
//            is WidgetItems.ServicesWidget -> item.data.containerType
//            is WidgetItems.VerticalNewsWidget -> item.data.containerType
//            is WidgetItems.RowTagsNewsWidget -> item.data.containerType
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        return viewHolderFactories[viewType]?.invoke(inflater, parent)
//            ?: DefaultViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false))
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        when (val item = getItem(position)) {
//            is WidgetItems.CarouselWidget -> (holder as VideoListViewHolder).bind(item.videos, item.data.autoScroll ?: false, item.data.horizontalScroll ?: true)
//            is WidgetItems.BannerWidget -> (holder as BannerViewHolder).bind(item.data, item.banners)
//            is WidgetItems.GridWidget -> (holder as NewsViewHolder).bind(item.data, item.newsList)
//            is WidgetItems.ServicesWidget -> (holder as ServicesViewHolder).bind(item.data, item.servicesList)
//            is WidgetItems.VerticalNewsWidget -> (holder as VerticalNewsViewHolder).bind(item.data, item.verticalNewsList)
//            is WidgetItems.RowTagsNewsWidget -> (holder as RowTagsNewsViewHolder).bind(item.data, item.rowTagsNewsList)
//        }
//    }
//}
//
//class WidgetDiffCallback : DiffUtil.ItemCallback<WidgetItems>() {
//    override fun areItemsTheSame(oldItem: WidgetItems, newItem: WidgetItems): Boolean {
//        return when {
//            oldItem is WidgetItems.CarouselWidget && newItem is WidgetItems.CarouselWidget -> oldItem.data.id == newItem.data.id
//            oldItem is WidgetItems.BannerWidget && newItem is WidgetItems.BannerWidget -> oldItem.data.id == newItem.data.id
//            oldItem is WidgetItems.GridWidget && newItem is WidgetItems.GridWidget -> oldItem.data.id == newItem.data.id
//            oldItem is WidgetItems.ServicesWidget && newItem is WidgetItems.ServicesWidget -> oldItem.data.id == newItem.data.id
//            oldItem is WidgetItems.VerticalNewsWidget && newItem is WidgetItems.VerticalNewsWidget -> oldItem.data.id == newItem.data.id
//            oldItem is WidgetItems.RowTagsNewsWidget && newItem is WidgetItems.RowTagsNewsWidget -> oldItem.data.id == newItem.data.id
//            else -> false
//        }
//    }
//
//    override fun areContentsTheSame(oldItem: WidgetItems, newItem: WidgetItems): Boolean {
//        return oldItem == newItem
//    }
//}
//
//// Updated ViewHolders
//class VideoListViewHolder(private val binding: LayoutNewsVideoBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(videos: List<NewsVideoPojo>, autoScroll: Boolean, horizontalScroll: Boolean) {
//        val videoAdapter = VideoItemAdapter(videos, binding.recyclerVierwsDy)
//        binding.recyclerVierwsDy.apply {
//            layoutManager = LinearLayoutManager(context, if (horizontalScroll) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL, false)
//            setHasFixedSize(true)
//            isNestedScrollingEnabled = false
//            setRecycledViewPool(RecyclerView.RecycledViewPool())
//            adapter = videoAdapter
//            if (autoScroll) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    while (isActive) {
//                        delay(3000)
//                        val nextPosition = ((layoutManager as LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0) + 1
//                        smoothScrollToPosition(nextPosition % videoAdapter.itemCount)
//                    }
//                }
//            }
//        }
//    }
//}
//
//class BannerViewHolder(private val binding: LayoutViewNoTitleBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(widget: WidgetOrder, bannerList: ArrayList<External_game_pojo>) {
//        val bannerAdapter = RowBannerItemAdapter(binding.root.context, bannerList)
//        binding.recyclerViewNoTitle.apply {
//            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//            setHasFixedSize(true)
//            isNestedScrollingEnabled = false
//            adapter = bannerAdapter
//        }
//        widget.heroImageUrl?.let { url ->
//            // Load with Glide/Coil if needed
//        }
//    }
//}
//
//class NewsViewHolder(private val binding: LayoutViewWithTitleBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(widget: WidgetOrder, newsList: List<TopNewsPojo>) {
//        binding.tvTitleView.text = widget.title ?: ""
//        val newsAdapter = NewsAdapter(binding.root.context, newsList, widget.title ?: "")
//        binding.recyclerViewTitle.apply {
//            layoutManager = GridLayoutManager(context, 2)
//            setHasFixedSize(true)
//            isNestedScrollingEnabled = false
//            adapter = newsAdapter
//        }
//    }
//}
//
//class ServicesViewHolder(private val binding: LayoutViewWithTitleBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(widget: WidgetOrder, servicesList: List<Services_pojo>) {
//        binding.tvTitleView.text = widget.title ?: ""
//        val servicesAdapter = GridTitleAdapter(binding.root.context, servicesList)
//        binding.recyclerViewTitle.apply {
//            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//            setHasFixedSize(true)
//            isNestedScrollingEnabled = false
//            adapter = servicesAdapter
//        }
//    }
//}
//
//class VerticalNewsViewHolder(private val binding: LayoutViewWithTitleBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(widget: WidgetOrder, verticalNewsList: List<TopNewsPojo>) {
//        binding.tvTitleView.text = widget.title ?: ""
////        val newsAdapter = VerticalNewsAdapter(binding.root.context, verticalNewsList, "")
////        binding.recyclerViewTitle.apply {
////            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
////            setHasFixedSize(true)
////            isNestedScrollingEnabled = false
////            adapter = newsAdapter
////        }
//    }
//}
//
//class RowTagsNewsViewHolder(private val binding: LayoutViewWithTitleBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(widget: WidgetOrder, rowTagsNewsList: List<TopNewsPojo>) {
//        binding.tvTitleView.text = widget.title ?: ""
//        val tagsNewsAdapter = RowTagAdapter(binding.root.context, rowTagsNewsList)
//        binding.recyclerViewTitle.apply {
//            layoutManager = LinearLayoutManager(context, if (widget.horizontalScroll == true) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL, false)
//            setHasFixedSize(true)
//            isNestedScrollingEnabled = false
//            adapter = tagsNewsAdapter
//        }
//    }
//}
//
//class DefaultViewHolder(private val binding: LayoutViewWithTitleBinding) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(widget: WidgetOrder, viewType: ViewsOrder?) {
//        binding.tvTitleView.text = "Unsupported widget: ${widget.containerTypeDescription} (${viewType?.viewDescription ?: "Unknown"})"
//    }
//}
//
////class WidgetAdapter(
////    private val items: List<WidgetItems>
////) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
////
////    companion object {
////        private const val TYPE_VIDEO = 1
////        private const val TYPE_SERVICES = 2
////        private const val TYPE_ADMOB = 3
////        private const val TYPE_BANNER = 4
////        private const val TYPE_NEWS = 5
////    }
////
////    override fun getItemViewType(position: Int): Int {
////        return when (items[position]) {
////            is WidgetItems.BannerWidget -> TYPE_BANNER
////            is WidgetItems.CarouselWidget -> TYPE_VIDEO
////            is WidgetItems.GridWidget -> TYPE_NEWS
////        }
//////        return when (items[position].containerTypeDescription) {
//////            "Carousel" -> TYPE_VIDEO
//////            "Strip" -> TYPE_SERVICES
//////            "admob_one", "admob_two", "admob_three",
//////            "admob_four", "admob_five", "admob_six", "admob_seven" -> TYPE_ADMOB
//////            "banner_news_new", "entertainment" -> TYPE_BANNER
//////            "SingleContainer", "QuadContainer", "news3" -> TYPE_NEWS
//////            else -> TYPE_SERVICES // fallback
//////        }
////    }
////
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
////
////        val inflater = LayoutInflater.from(parent.context)
////        return when (viewType) {
////            TYPE_BANNER -> BannerViewHolder(LayoutViewNoTitleBinding.inflate(inflater, parent, false))
////            TYPE_VIDEO -> VideoListViewHolder(LayoutNewsVideoBinding.inflate(inflater, parent, false))
////            TYPE_NEWS -> NewsViewHolder(LayoutViewWithTitleBinding.inflate(inflater, parent, false))
////            else -> throw IllegalArgumentException("Invalid viewType")
////        }
////
//////        val inflater = LayoutInflater.from(parent.context)
//////        return when (viewType) {
//////            TYPE_VIDEO -> VideoListViewHolder(
//////                LayoutNewsVideoBinding.inflate(inflater, parent, false)
//////            )
//////            TYPE_SERVICES -> ServicesViewHolder(
//////                LayoutViewWithTitleBinding.inflate(inflater, parent, false)
//////            )
//////            TYPE_BANNER -> BannerViewHolder(
//////                LayoutViewNoTitleBinding.inflate(inflater, parent, false)
//////            )
//////            TYPE_NEWS -> NewsViewHolder(
//////                LayoutViewWithTitleBinding.inflate(inflater, parent, false)
//////            )
//////            else -> AdmobViewHolder(
//////                LayoutViewWithTitleBinding.inflate(inflater, parent, false)
//////            )
//////        }
////    }
////
////    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
////        val widget = items[position]
////        when (val item = widget) {
////            is WidgetItems.BannerWidget -> (holder as BannerViewHolder).bind(item.data)
////            is WidgetItems.CarouselWidget -> (holder as VideoListViewHolder).bind(item.data)
////            is WidgetItems.GridWidget -> (holder as NewsViewHolder).bind(item.data)
////        }
//////        when (holder) {
//////            is VideoListViewHolder -> {
//////                // Example: bind a list of videos
//////                val sampleVideos = listOf(
//////                    NewsVideoPojo("Big Buck Bunny", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
//////                    NewsVideoPojo("Elephant Dream", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
//////                )
//////                holder.bind(sampleVideos)
//////            }
//////            is ServicesViewHolder -> {
//////                holder.bind(widget)
//////            }
//////            is BannerViewHolder -> {
//////                holder.bind("https://picsum.photos/600/200") // sample banner
//////            }
//////            is NewsViewHolder -> {
//////                holder.bind(widget)
//////            }
//////            is AdmobViewHolder -> {
//////                holder.bind(widget.title.toString())
//////            }
//////        }
////    }
////
////    override fun getItemCount() = items.size
////
////    // --- ViewHolders ---
////
////    class VideoListViewHolder(private val binding: LayoutNewsVideoBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(videos: List<NewsVideoPojo>) {
////            val videoAdapter = VideoItemAdapter(videos, binding.recyclerVierwsDy)
////            binding.recyclerVierwsDy.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                adapter = videoAdapter
////            }
////        }
////    }
////
////    class ServicesViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////
////        fun bind(widget: WidgetOrder) {
////            binding.tvTitleView.text = widget.title
////
////            val serviceList = arrayListOf(
////                OtherServicesPojo("YouTube", "https://youtube.com", "com.google.android.youtube", "", "", ),
////                OtherServicesPojo("Netflix", "https://netflix.com", "com.netflix.mediaclient", "", ""),
////                OtherServicesPojo("Spotify", "https://spotify.com", "com.spotify.music", "", "")
////            )
////
////            val adapter = SingleRowTextAdapter(binding.root.context, serviceList)
////
////            binding.recyclerViewTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                this.adapter = adapter
////            }
////        }
////    }
////
////
////    class BannerViewHolder(private val binding: LayoutViewNoTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(widget: WidgetOrder) {
////            // Use Glide/Picasso to load banner
//////            Glide.with(binding.root.context).load(imageUrl).into(binding.ivBanner)
////        }
////    }
////
////
////    class NewsViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////
////        fun bind(widget: WidgetOrder) {
////            binding.tvTitleView.text = widget.title
////
////            val newsList = listOf(
////                TopNewsPojo(
////                    title = "Breaking: Kotlin takes over Java",
////                    description = "Developers are loving the simplicity of Kotlin",
////                    image = "https://picsum.photos/200/300",
////                    postedDate = "18-08-2025 +0000",
////                    feedProvider = "https://picsum.photos/50",
////                    redirectLink = "https://kotlinlang.org",
////                    adMobId = "",
////                    isAds = false
////                ),
////                TopNewsPojo(
////                    title = "AI is the Future",
////                    description = "AI is now helping devs write better code.",
////                    image = "https://picsum.photos/200/301",
////                    postedDate = "19-08-2025 +0000",
////                    feedProvider = "https://picsum.photos/51",
////                    redirectLink = "https://openai.com",
////                    adMobId = "",
////                    isAds = false
////                )
////            )
////
////            val newsAdapter = NewsAdapter(
////                context = binding.root.context,
////                items = newsList,
////                topCateName = widget.title ?: ""
////            )
////
////            binding.recyclerViewTitle.apply {
////                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
////                setHasFixedSize(true)
////                isNestedScrollingEnabled = false
////                adapter = newsAdapter
////            }
////        }
////    }
////
////    class AdmobViewHolder(private val binding: LayoutViewWithTitleBinding) :
////        RecyclerView.ViewHolder(binding.root) {
////        fun bind(title: String) {
////            binding.tvTitleView.text = title
////        }
////    }
////}
