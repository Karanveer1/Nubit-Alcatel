package com.mobi.nubitalcatel.ui.fragments

import WidgetAdapter
import WithTitleTabsViewHolder
import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobi.nubitalcatel.R
import com.mobi.nubitalcatel.core.models.ContainerOrder
import com.mobi.nubitalcatel.core.models.KeyValue
import com.mobi.nubitalcatel.core.models.Payload
import com.mobi.nubitalcatel.core.models.TagsPojo
import com.mobi.nubitalcatel.core.models.TopNewsPojo
import com.mobi.nubitalcatel.core.models.ViewsOrder
import com.mobi.nubitalcatel.core.models.WidgetOrder
import com.mobi.nubitalcatel.core.network.NetworkModule
import com.mobi.nubitalcatel.databinding.FragmentNubitViewBinding
import com.mobi.nubitalcatel.ui.activity.WebviewActivity
import com.mobi.nubitalcatel.utils.CommonMethods
import com.mobi.nubitalcatel.utils.CommonMethods.Companion.ACTION_STOP_AUDIO
import com.mobi.nubitalcatel.utils.FullScreenImageDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class NubitFragment : Fragment(), WithTitleTabsViewHolder.OnNewsCategoryClickListener {

    private var _binding: FragmentNubitViewBinding? = null
    private val binding get() = _binding!!

    private val _freqAppsLiveData = MutableLiveData<List<Payload>>()
    val freqAppsLiveData: LiveData<List<Payload>> get() = _freqAppsLiveData

    //"https://api.mobinity.in/minusone-service/content/getContent?pageNumber=0&pageSize=10"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNubitViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.WHITE  // Set light background
        val decorView = requireActivity().window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.shimmerLayout.startShimmer()
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.rvWidgets.visibility = View.GONE
        useDefaultBackground()
        getFrequntApps()
        /***
         * commented for testing
         */
        lifecycleScope.launch {
            val masterCache = CommonMethods.loadJson("widgets_order_cache", requireActivity())
            if (masterCache.isNullOrEmpty()) {
                // No cache → hit API and wait
                if (fetchMasterContainerApi()) {
                    fetchViewTypesApi()
                    fetchNewsCategoriesApi()
                }
            } else {
                // Cache exists → load immediately
                loadFromCache()

                // In background update API
                launch {
                    if (fetchMasterContainerApi()) {
                        fetchViewTypesApi()
                        fetchNewsCategoriesApi()
                    }
                }
            }
        }

        lifecycleScope.launch {
            fetchNewsCategoriesApi()
        }
//        loadFromCache()

        binding.mainLayout.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY > 500) {
                    binding.ivScrollTop.visibility = View.VISIBLE
                } else {
                    binding.ivScrollTop.visibility = View.GONE
                }
            }
        )
        binding.ivScrollTop.setOnClickListener {
            binding.mainLayout.smoothScrollTo(0, 0)
            }
//        TrackSelector trackSelector =
//            new DefaultTrackSelector(context);
//        videoPlayer = new SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector).build();
//
//
//        videoSurfaceView = new PlayerView(this.context);
//        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

//        nested_scroolview_home.setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                switch(event.getAction())
//                {
//                    case MotionEvent.ACTION_DOWN:
//                        x1 = event.getX();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        x2 = event.getX();
//                        float deltaX = x2 - x1;
//
//                        if (Math.abs(deltaX) > MIN_DISTANCE)
//                        {
//                            // Left to Right swipe action
//                            if (x2 > x1)
//                            {
//                                Toast.makeText(context, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show ();
//                            }
//
//                            // Right to left swipe action
//                            else
//                            {
//
////                                context?.startHandlingTouches(touchDownY);
//                                Toast.makeText(context, "Right to Left swipe [Previous]", Toast.LENGTH_SHORT).show ();
//                            }
//
//                        }
//                        else
//                        {
//                            // consider as something else - a screen tap for example
//                        }
//                        break;
//                }
//                return false;
//            }
//        });


//        lifecycleScope.launch {
//            loadContainersFromCache()
//            try {
//                val response = NetworkModule.minusOneApi.getMasterContainer()
//                if (response.responseStatus) {
//                    val json = Gson().toJson(response.responseObject)
//                    CommonMethods.saveJson("widgets_order_cache", json, requireActivity())
////                    showWidgets(response.responseObject)
//                } else {
//                    // fallback to cache
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Fallback to cache if API fails
//            }
//
//            try {
//                val response = NetworkModule.minusOneApi.getViewTypes()
//                if (response.responseStatus) {
//                    val json = Gson().toJson(response.responseObject)
//                    CommonMethods.saveJson("view_types", json, requireActivity())
//                } else {
//                    // fallback to cache
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Fallback to cache if API fails
//            }
//
//            delay(3000)
//            stopShimmer()
//            try {
//                val response = NetworkModule.minusOneApi.getData()
//
//                if (response.responseStatus) {
//                    // Save locally
//                    val json = Gson().toJson(response.responseObject)
//                    CommonMethods.saveJson("data_cache", json, requireActivity())
//                    loadFromCache()
//
////                    showWidgets(response.responseObject)
//                } else {
//                    // fallback to cache
//                    loadFromCache()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Fallback to cache if API fails
//                loadFromCache()
//            }
//        }
        }

        private suspend fun fetchMasterContainerApi(): Boolean {
            val response = NetworkModule.minusOneApi.getMasterContainer()
            return if (response.responseStatus) {
                val json = Gson().toJson(response.responseObject)
                CommonMethods.saveJson("widgets_order_cache", json, requireActivity())
                true
            } else {
                false
            }
        }

        private suspend fun fetchViewTypesApi() {
            val response = NetworkModule.minusOneApi.getViewTypes()
            if (response.responseStatus) {
                val json = Gson().toJson(response.responseObject)
                CommonMethods.saveJson("view_types", json, requireActivity())
                fetchDataApi()
            }
        }

        private suspend fun fetchNewsCategoriesApi() {
            val response = NetworkModule.minusOneApi.getNewsCategories()
            if (response.responseStatus) {
                val json = Gson().toJson(response.responseObject)
                CommonMethods.saveJson("news_categories", json, requireActivity())
                fetchNewsDataApi()
//            fetchNewsDataApiNewTest()
            }
        }

        private suspend fun fetchDataApi() {
//        try {
            val response = NetworkModule.minusOneApi.getData()
            delay(1000)
            stopShimmer()
            if (response.responseStatus) {
                val json = Gson().toJson(response.responseObject)
                CommonMethods.saveJson("data_cache", json, requireActivity())
//                loadFromCache()
            } else {
//                loadFromCache()
            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            loadFromCache()
//        }
        }

        fun showInfoBottomSheet(
            context: Context,
            imageRes: Int,
            title: String,
            description: String
        ) {
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_ads, null)
            dialog.setContentView(view)
            dialog.setOnShowListener { dialogInterface ->
                val d = dialogInterface as BottomSheetDialog
                val bottomSheet =
                    d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(R.drawable.bottom_sheet_bg)
            }
            val imageBanner = view.findViewById<ImageView>(R.id.imageBanner)
            val btnTitle = view.findViewById<TextView>(R.id.btnTitle)
            val ivCross = view.findViewById<ImageView>(R.id.ivCross)
            val textTitle = view.findViewById<TextView>(R.id.textTitle)
            val textDescription = view.findViewById<TextView>(R.id.textDescription)

            imageBanner.setImageResource(imageRes)
            btnTitle.setOnClickListener {
                val intent = Intent(binding.root.context, WebviewActivity::class.java)
                intent.putExtra("url", "https://www.rapido.bike/Home")
                intent.putExtra("title", "Rapido")
                binding.root.context.startActivity(intent)
                dialog.dismiss()
            }
            ivCross.setOnClickListener {
                dialog.dismiss()
            }
            textTitle.text = title
            textDescription.text = description

            dialog.show()
        }

    override fun onResume() {
        super.onResume()

        var adcFrq = CommonMethods.getInt(requireActivity(), "full_screen_ad_frq") ?: 0
        adcFrq++  // Increment counter

        when {
            adcFrq == 3 -> {
                // Show overlay ad on 3rd visit
                init_overlay_ads_Views()
            }
            adcFrq == 4 -> {
                // Show info bottom sheet on 4th visit
                showInfoBottomSheet(
                    requireActivity(),
                    R.drawable.banner_one,
                    "India's largest bike taxi",
                    "Get your first ride here! Visit us"
                )
                adcFrq = 0 // Reset counter after showing bottom sheet
            }
        }

        // Save the updated counter
        CommonMethods.saveInt(requireActivity(), "full_screen_ad_frq", adcFrq)
    }

    private suspend fun fetchNewsDataApi() {
            try {
                val bodies = mapOf(
                    "trending_news" to listOf(KeyValue("CATEGORY_IN", "1")),
                    "world_news" to listOf(KeyValue("CATEGORY_IN", "3")),
                    "showbiz" to listOf(KeyValue("CATEGORY_IN", "8")),
                    "sports" to listOf(KeyValue("CATEGORY_IN", "5"))
                )

                bodies.forEach { (category, body) ->
                    try {
                        val response = NetworkModule.minusOneApi.getNewsData(body)

                        if (response.responseStatus) {
                            val data = response.responseObject
                            if (data != null && !data.content.isEmpty()) {
                                val json = Gson().toJson(data.content)
                                val cacheKey = "${category}_cache"
                                CommonMethods.saveJson(cacheKey, json, requireActivity())
                                Log.d("NewsAPI", "$category data cached successfully")
                            } else {
                                Log.e("NewsAPI", "Empty or unsuccessful $category response")
                            }
                        } else {
                            Log.e(
                                "NewsAPI",
                                "$category response failed with code ${response.responseStatusCode}"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("NewsAPI", "Error fetching $category data", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("NewsAPI", "Error in fetchNewsDataApi", e)
            }
        }

        private suspend fun fetchTabsNewsDataApi(catId: String) {
            try {
                val body = listOf(KeyValue("CATEGORY_IN", catId))

                val response = NetworkModule.minusOneApi.getNewsData(body)

                if (response.responseStatus) {
                    val data = response.responseObject
                    if (data != null && data.content.isNotEmpty()) {
                        val json = Gson().toJson(data.content)
                        val cacheKey = "${catId}_cache"
//                    CommonMethods.saveJson(cacheKey, json, requireActivity())
//                    val newsCache = CommonMethods.loadJson(cacheKey, requireActivity())
                        if (!json.isNullOrEmpty()) {
                            try {
                                val type = object : TypeToken<List<TopNewsPojo>>() {}.type
                                widgetAdapter.updateTrendingNews(
                                    Gson().fromJson(
                                        json.toString(),
                                        type
                                    ) ?: emptyList()
                                )
                            } catch (e: Exception) {
                            }
                        } else {
                        }
                        Log.d("NewsAPI", "$catId data cached successfully")
                    } else {
                        Log.e("NewsAPI", "Empty or unsuccessful $catId response")
                    }
                } else {
                    Log.e(
                        "NewsAPI",
                        "$catId response failed with code ${response.responseStatusCode}"
                    )
                }

            } catch (e: Exception) {
                Log.e("NewsAPI", "Error fetching data for category: $catId", e)
            }
        }

//    private suspend fun fetchNewsDataApi() {
//        try {
//            val body = listOf(KeyValue("CATEGORY_IN", "1"))
//            val worldNewsBody = listOf(KeyValue("CATEGORY_IN", "3"))
//            val showbizNewsBody = listOf(KeyValue("CATEGORY_IN", "4"))
//            val response = NetworkModule.minusOneApi.getNewsData(body)
//
//            if (response.responseStatus) {
//                val data = response.responseObject
//                if (data != null && !data.content.isEmpty()) {
//                    val json = Gson().toJson(data.content)
//                    CommonMethods.saveJson("news_data_cache", json, requireActivity())
//                } else {
//                    Log.e("API", "Empty or unsuccessful response")
//                    // loadFromCache()
//                }
//            } else {
//                Log.e("API", "Response failed with code ${response.responseStatusCode}")
//                // loadFromCache()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // loadFromCache()
//        }
//    }


//    private suspend fun fetchNewsDataApi() {
////        try {
//            val response = NetworkModule.minusOneApi.getNewsData()
//            if (response.responseStatus) {
//                val json = Gson().toJson(response.responseObject)
//                CommonMethods.saveJson("news_data_cache", json, requireActivity())
//            } else {
////                loadFromCache()
//            }
////        } catch (e: Exception) {
////            e.printStackTrace()
//////            loadFromCache()
////        }
//    }

        fun loadViewTypes(context: Activity): List<ViewsOrder> {
            val viewTypesJson = CommonMethods.loadJson("view_types", context)
            return if (!viewTypesJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<ViewsOrder>>() {}.type
                Gson().fromJson(viewTypesJson, type) ?: emptyList()
            } else {
                emptyList()
            }
        }

        private fun init_overlay_ads_Views() {
//        Log.e("checkloffccwil", "11>>" + arraylist_overlay_ads_banner.size)
//        if (arraylist_overlay_ads_banner.isEmpty()) {
//            return
//        }
//        val overlayAdsCopy: List<Adv_banner_Pojo> = ArrayList<Any?>(arraylist_overlay_ads_banner)
//        val fullScreenBanners: MutableList<Adv_banner_Pojo> = ArrayList<Adv_banner_Pojo>()
//        for (banner in overlayAdsCopy) {
//            Log.e("checkloffccwil", "22>>" + overlayAdsCopy.size + ">>" + banner.getLink_type())
//            if ("bottom_banner" == banner.getLink_type()) {
//                handleBottomBanner(banner)
//            } else if ("full_screen_banner" == banner.getLink_type()) {
////                    "full_screen_video".equals(banner.getLink_type())) {
//                fullScreenBanners.add(banner)
//            }
//        }
//        if (!fullScreenBanners.isEmpty()) {
//            var adcFrq: Int = MyApplication.app_sharedPreferences.getInt("full_screen_ad_frq", 0)
//            var apiFcrequency = 1
//            try {
//                apiFcrequency = fullScreenBanners[0].getFrequency().toInt()
//            } catch (ignored: java.lang.Exception) {
//            }
//            adcFrq++
//            if (adcFrq >= apiFcrequency) {
//                Handler(Looper.getMainLooper()).post {
//                    val mediaList: ArrayList<FullScreenImageDialog.MediaItem> =
//                        ArrayList<FullScreenImageDialog.MediaItem>()
//                    for (fsBanner in fullScreenBanners) {
//                        // Add exactly like your static sample list
//                        mediaList.add(
//                            FullScreenImageDialog.MediaItem(
//                                fsBanner.getBanner_image(),
//                                FullScreenImageDialog.MediaItem.Type.IMAGE,
//                                fsBanner.getRedirect_link()
//                            )
//                        )
//                    }
//                    FullScreenImageDialog.showDialog((context as FragmentActivity?)!!, FullScreenImageDialog.getSampleMediaList())
//                }
//                adcFrq = 0
//            }
            FullScreenImageDialog.showDialog(
                (context as FragmentActivity?)!!,
                getSampleMediaList()
            );
//            MyApplication.app_editor.putInt("full_screen_ad_frq", adcFrq).apply()
//        }
        }

        private var mediaList: ArrayList<FullScreenImageDialog.MediaItem?> = ArrayList()
        public fun getSampleMediaList(): ArrayList<FullScreenImageDialog.MediaItem?> {
            // Sample video 1
//            mediaList!!.add(
//                FullScreenImageDialog.MediaItem(
//                    "https://streaming.vidring.app/content/1080P/Ads/vidring_ads_0001.mp4",
//                    FullScreenImageDialog.MediaItem.Type.VIDEO,
//                    ""
//                )
//            );
//            // Sample video 2
//            mediaList!!.add(
//                FullScreenImageDialog.MediaItem(
//                    "https://streaming.vidring.app/content/1080P/Ads/vidring_ads_0005.mp4",
//                    FullScreenImageDialog.MediaItem.Type.VIDEO,
//                    ""
//                )
//            );
            // Another image
            mediaList!!.add(
                FullScreenImageDialog.MediaItem(
                    "https://vistory.s3.ap-south-1.amazonaws.com/assets/WhatsApp%20Image%202025-04-09%20at%2021.07.22.jpeg",
                    FullScreenImageDialog.MediaItem.Type.IMAGE,
                    ""
                )
            );
            mediaList!!.add(
                FullScreenImageDialog.MediaItem(
                    "https://vistory.s3.ap-south-1.amazonaws.com/assets/40a13cda42e692240439c01b33cd001f.jpg",
                    FullScreenImageDialog.MediaItem.Type.IMAGE,
                    ""
                )
            );
            return mediaList
        }

//    private fun loadFromCache() {
//        val viewTypesIdsList: HashMap<Int, ViewsOrder> = HashMap()
//        val viewTypesCache = CommonMethods.loadJson("view_types", requireActivity())
//        if (!viewTypesCache.isNullOrEmpty()) {
//            val type = object : TypeToken<List<ViewsOrder>>() {}.type
//            val widgetList: List<ViewsOrder> = Gson().fromJson(viewTypesCache, type)
//
//            for (item in widgetList) {
//                viewTypesIdsList[item.id] = item
//            }
//            val mapJson = Gson().toJson(viewTypesIdsList)
//            CommonMethods.saveJson("view_types_cache", mapJson, requireActivity())
//        }
//        Log.e("checkcachevalues", ">view_types 22>" + viewTypesCache)
//
//        val widgetIdsList: HashMap<Int, ContainerOrder> = HashMap()
//        val idsCache = CommonMethods.loadJson("widgets_order_cache", requireActivity())
//        if (!idsCache.isNullOrEmpty()) {
//            val type = object : TypeToken<List<ContainerOrder>>() {}.type
//            val widgetList: List<ContainerOrder> = Gson().fromJson(idsCache, type)
//
//            for (item in widgetList) {
//                widgetIdsList[item.id] = item
//            }
//            val mapJson = Gson().toJson(widgetIdsList)
//            CommonMethods.saveJson("widgets_ids_cache", mapJson, requireActivity())
//            Log.e("checkcachevalues", ">widgets_ids_cache 22>" + mapJson)
//        }
//
//        val dataCache = CommonMethods.loadJson("data_cache", requireActivity())
//        Log.e("checkcachevalues", ">data_cache>" + dataCache)
//        if (!dataCache.isNullOrEmpty()) {
//            val type = object : TypeToken<List<WidgetOrder>>() {}.type
//            val widgetList: List<WidgetOrder> = Gson().fromJson(dataCache, type)
//            showWidgets(loadViewTypes(requireActivity()), widgetIdsList, widgetList)
//        }
//    }


        private fun loadFromCache() {

            val viewTypesById: HashMap<Int, ViewsOrder> = HashMap()
            run {
                val viewTypesCache = CommonMethods.loadJson("view_types", requireActivity())
                val type = object : TypeToken<List<ViewsOrder>>() {}.type
                val views: List<ViewsOrder> = Gson().fromJson(viewTypesCache, type)
//            val views: List<ViewsOrder> = Gson().fromJson(viewsJson, type)
                for (item in views) {
                    viewTypesById[item.id] = item
                }
                CommonMethods.saveJson(
                    "view_types_cache",
                    Gson().toJson(viewTypesById),
                    requireActivity()
                )
            }

            // Parse containers and index by id
            val containersById: HashMap<Int, ContainerOrder> = HashMap()
            run {
                val idsCache = CommonMethods.loadJson("widgets_order_cache", requireActivity())
                val type = object : TypeToken<List<ContainerOrder>>() {}.type
                val containers: List<ContainerOrder> = Gson().fromJson(idsCache, type)
//            val containers: List<ContainerOrder> = Gson().fromJson(containerJson, type)
                for (item in containers) {
                    containersById[item.id] = item
                }
                CommonMethods.saveJson(
                    "widgets_ids_cache",
                    Gson().toJson(containersById),
                    requireActivity()
                )
            }

            // Parse payload widgets as nullable, filter out any malformed/null entries, then sort
            val dataCache = CommonMethods.loadJson("data_cache", requireActivity())
            if (dataCache.isNullOrEmpty()) {
                return
            }
            val widgets: List<WidgetOrder> = run {
                val type = object : TypeToken<List<WidgetOrder?>>() {}.type
                val raw: List<WidgetOrder?> = Gson().fromJson(dataCache, type)
//            val raw: List<WidgetOrder?> = Gson().fromJson(payloadJson, type)
                raw.filterNotNull()
            }
            val sortedWidgets = widgets.sortedBy { it.orderSequence }
            freqAppsLiveData.observe(viewLifecycleOwner) { freqApps ->
                val updatedWidgets = sortedWidgets.map { widget ->
                    if (widget.viewTypeId == 6) {
                        widget.copy(payload = freqApps)
                    } else widget
                }

                showWidgets(viewTypesById, containersById, updatedWidgets)
            }
//        showWidgets(viewTypesById, containersById, sortedWidgets)

        }

        private fun loadContainersFromCache() {
            val cached = CommonMethods.loadJson("widgets_order_cache", requireActivity())
            if (!cached.isNullOrEmpty()) {
                val type = object : TypeToken<List<ContainerOrder>>() {}.type
                val widgetList: List<ContainerOrder> = Gson().fromJson(cached, type)

                val containerMap: HashMap<Int, ContainerOrder> = HashMap()
                for (item in widgetList) {
                    containerMap[item.id] = item
                }

                val mapJson = Gson().toJson(containerMap)
                CommonMethods.saveJson("widgets_ids_cache", mapJson, requireActivity())
                Log.e("cgecjklocallflow", ">>" + mapJson)
            }
        }

        private lateinit var widgetAdapter: WidgetAdapter
        private fun showWidgets(
            viewTypes: HashMap<Int, ViewsOrder>,
            containers: HashMap<Int, ContainerOrder>,
            widgets: List<WidgetOrder>
        ) {
            // Load news data and categories
            val newsData = loadNewsData("trending_news")
            val showbizData = loadNewsData("showbiz")
            val sportsData = loadNewsData("sports")
            val worldData = loadNewsData("world_news")
            val newsCategories = loadNewsCategories()

            stopShimmer()

            widgetAdapter = WidgetAdapter(
                containers, viewTypes, widgets, newsData,
                newsCategories, showbizData, worldData, sportsData, this
            )

            binding.rvWidgets.layoutManager = LinearLayoutManager(context)
            binding.rvWidgets.adapter = widgetAdapter
            binding.rvWidgets.isNestedScrollingEnabled = false

//        freqAppsLiveData.observe(viewLifecycleOwner) { freqApps ->
//            widgetAdapter.updateFreqApps(freqApps)
//        }
        }

//    private fun loadNewsData(): List<TopNewsPojo> {
//        val newsCache = CommonMethods.loadJson("news_data_cache", requireActivity())
//        return if (!newsCache.isNullOrEmpty()) {
//            try {
//                val type = object : TypeToken<List<TopNewsPojo>>() {}.type
//                Gson().fromJson(newsCache, type) ?: emptyList()
//            } catch (e: Exception) {
//                Log.e("NewsData", "Failed to parse news data", e)
//                emptyList()
//            }
//        } else {
//            emptyList()
//        }
//    }

        private fun loadNewsData(category: String): List<TopNewsPojo> {
            val cacheKey = "${category}_cache"
            val newsCache = CommonMethods.loadJson(cacheKey, requireActivity())
            return if (!newsCache.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<TopNewsPojo>>() {}.type
                    Gson().fromJson(newsCache, type) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("NewsData", "Failed to parse $category data", e)
                    emptyList()
                }
            } else {
                Log.d("NewsData", "No cache found for $category")
                emptyList()
            }
        }

        private fun loadNewsCategories(): List<TagsPojo> {
            val categoriesCache = CommonMethods.loadJson("news_categories", requireActivity())
            return if (!categoriesCache.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<TagsPojo>>() {}.type
                    Gson().fromJson(categoriesCache, type) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("NewsCategories", "Failed to parse news categories", e)
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

//    @RequiresApi(api = Build.VERSION_CODES.N)
//    private fun getFrequntApps() {
//        val appOps = requireActivity().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//        val mode = appOps.checkOpNoThrow(
//            AppOpsManager.OPSTR_GET_USAGE_STATS,
//            Process.myUid(),
//            requireActivity().packageName
//        )
//        if (mode == AppOpsManager.MODE_ALLOWED) {
//            // Permission granted, proceed with fetching and displaying apps
//            val usageStatsManager =
//                requireActivity().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
//            val endTime = System.currentTimeMillis()
//            val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago
//            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
//
//            // Iterate through the events and collect app usage data
//            val appUsageMap: MutableMap<String, Long> = java.util.HashMap()
//            while (usageEvents.hasNextEvent()) {
//                val event = UsageEvents.Event()
//                usageEvents.getNextEvent(event)
//                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
//                    val packageName = event.packageName
//                    val usageCount = appUsageMap.getOrDefault(packageName, 0L)
//                    appUsageMap[packageName] = usageCount + 1
//                }
//            }
//
//            // Sort the apps by their usage count
//            val sortedApps: List<Map.Entry<String, Long>> =
//                ArrayList<Map.Entry<String, Long>>(appUsageMap.entries)
//            Collections.sort(
//                sortedApps
//            ) { (_, value): Map.Entry<String, Long>, (_, value1): Map.Entry<String, Long> ->
//                value1.compareTo(
//                    value
//                )
//            }
//            Log.e("cheedckfrag", ">sortedApps>" + sortedApps.size)
//            val topApps = sortedApps.subList(0, min(sortedApps.size, 8))
//
//            val pm = requireActivity().packageManager
//            val payloadList = topApps.mapIndexed { index, entry ->
//                val packageName = entry.key
//                val appName = try {
//                    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
//                } catch (e: Exception) {
//                    packageName
//                }
//
//                val iconUri = try {
//                    val iconDrawable = pm.getApplicationIcon(packageName)
//                    val bitmap = (iconDrawable as BitmapDrawable).bitmap
//                    val iconFile = File(requireContext().cacheDir, "$packageName.png")
//                    FileOutputStream(iconFile).use {
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
//                    }
//                    iconFile.toURI().toString()
//                } catch (e: Exception) {
//                    null
//                }

//                Payload(
//                    id = index,
//                    orderSequence = index,
//                    containerType = 0,
//                    containerTypeDescription = "Frequent App",
//                    title = appName,
//                    endpointUrl = null,
//                    heroImageUrl = iconUri,
//                    thumbnailUrl = null,
//                    defaultFocus = false
//                )
//            }
//
////            populateGridView(topApps)
//        } else {
//            // Permission denied, request permission from the user
//            requireActivity().startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
//        }
//    }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun getFrequntApps() {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val appOps =
                    requireActivity().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    requireActivity().packageName
                )

                if (mode != AppOpsManager.MODE_ALLOWED) {
                    withContext(Dispatchers.Main) {
                        requireActivity().startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                    return@launch
                }

                val usageStatsManager =
                    requireActivity().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago
                val usageEvents = usageStatsManager.queryEvents(startTime, endTime)

                val appUsageMap: MutableMap<String, Long> = HashMap()
                while (usageEvents.hasNextEvent()) {
                    val event = UsageEvents.Event()
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        val packageName = event.packageName
                        val usageCount = appUsageMap.getOrDefault(packageName, 0L)
                        appUsageMap[packageName] = usageCount + 1
                    }
                }

                val sortedApps = appUsageMap.entries.sortedByDescending { it.value }.take(8)
                val pm = requireActivity().packageManager

                val payloadList = sortedApps.mapIndexed { index, entry ->
                    val packageName = entry.key
                    val appName = try {
                        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
                    } catch (e: Exception) {
                        packageName
                    }
                    val iconUri = try {
                        val iconDrawable = pm.getApplicationIcon(packageName)
                        val bitmap = when (iconDrawable) {
                            is BitmapDrawable -> iconDrawable.bitmap
                            is AdaptiveIconDrawable -> {
                                val bitmap = Bitmap.createBitmap(
                                    iconDrawable.intrinsicWidth,
                                    iconDrawable.intrinsicHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                                iconDrawable.draw(canvas)
                                bitmap
                            }

                            else -> null
                        }

                        if (bitmap != null) {
                            val iconFile = File(requireContext().cacheDir, "$packageName.png")
                            FileOutputStream(iconFile).use {
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            }
                            iconFile.absolutePath // ✅ better than .toURI()
                        } else null
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }

//                val iconUri = try {
//                    val iconDrawable = pm.getApplicationIcon(packageName)
//                    val bitmap = (iconDrawable as BitmapDrawable).bitmap
//                    val iconFile = File(requireContext().cacheDir, "$packageName.png")
//                    FileOutputStream(iconFile).use {
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
//                    }
//                    iconFile.toURI().toString()
//                } catch (e: Exception) {
//                    null
//                }


                    Payload(
                        id = index,
                        orderSequence = index,
                        containerType = 0,
                        containerTypeDescription = "Frequent App",
                        title = appName,
                        endpointUrl = null,
                        heroImageUrl = iconUri,
                        thumbnailUrl = null,
                        defaultFocus = false
                    )
                }

                // Post value to LiveData (on main thread)
                withContext(Dispatchers.Main) {
                    _freqAppsLiveData.value = payloadList
                }
            }
        }


//    private fun showWidgets(
//        viewTypesIdsList: List<ViewsOrder>,
//        widgetIdsList: HashMap<Int, ContainerOrder>,
//        widgetList: List<WidgetOrder>
//    ) {
//        Log.e("debugFlow","method>>")
//        val sampleBanners = arrayListOf(
//            External_game_pojo(
//                "Big Buck Bunny",
//                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//                banner_image = "https://picsum.photos/200/300"
//            ),
//            External_game_pojo(
//                "Big Buck Bunny",
//                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//                banner_image = "https://picsum.photos/200/301"
//            ),
//            External_game_pojo(
//                "Big Buck Bunny",
//                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//                banner_image = "https://picsum.photos/200/302"
//            )
//        )
//
//        val sampleVideos = arrayListOf(
//            NewsVideoPojo(
//                "Big Buck Bunny",
//                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//            ),
//            NewsVideoPojo(
//                "Elephant Dream",
//                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
//            )
//        )
//        val servicesData = arrayListOf(
//            Services_pojo("Big Buck Bunny", "Service 1"),
//            Services_pojo("Elephant Dream", "Service 2")
//        )
//
//        val newsList = arrayListOf(
//            TopNewsPojo(
//                title = "Breaking: Kotlin takes over Java :Developers are loving the simplicity of Kotlin",
//                description = "Developers are loving the simplicity of Kotlin",
//                image = "https://picsum.photos/200/300",
//                postedDate = "18-08-2025 +0000",
//                feedProvider = "https://picsum.photos/50",
//                redirectLink = "https://kotlinlang.org",
//                adMobId = "",
//                isAds = false
//            ),
//            TopNewsPojo(
//                title = "AI is the Future: AI is now helping devs write better code",
//                description = "AI is now helping devs write better code.",
//                image = "https://picsum.photos/200/301",
//                postedDate = "19-08-2025 +0000",
//                feedProvider = "https://picsum.photos/51",
//                redirectLink = "https://openai.com",
//                adMobId = "",
//                isAds = false
//            )
//        )
//
//        val newssList = arrayListOf(
//            TopNewsPojo(
//                title = "Breaking: Kotlin takes over Java",
//                description = "Developers are loving the simplicity of Kotlin",
//                image = "https://picsum.photos/200/300",
//                postedDate = "18-08-2025 +0000",
//                feedProvider = "https://picsum.photos/50",
//                redirectLink = "https://kotlinlang.org",
//                adMobId = "",
//                isAds = false
//            ),
//            TopNewsPojo(
//                title = "AI is the Future",
//                description = "AI is now helping devs write better code.",
//                image = "https://picsum.photos/200/301",
//                postedDate = "19-08-2025 +0000",
//                feedProvider = "https://picsum.photos/51",
//                redirectLink = "https://openai.com",
//                adMobId = "",
//                isAds = false
//            )
//        )
//
//        val paylloadList = arrayListOf(
//            Payload(
//                id = 1,
//                orderSequence = 1,
//                containerType = 1,
//                containerTypeDescription = "18-08-2025 +0000",
//                title = "https://picsum.photos/50",
//                endpointUrl = "https://kotlinlang.org",
//                heroImageUrl = "",
//                thumbnailUrl = "",
//                defaultFocus = true
//            ),
//
//            )
//
//        val sortedWidgets = widgetList.sortedBy { it.orderSequence }
//        val viewTypeMap = viewTypesIdsList.associateBy { it.id }
//        val defaultViewType = ViewsOrder(id = -1, viewDescription = "Unknown", viewType = "unknown")
//
//        val widgetItems = sortedWidgets.map { widget ->
//            val viewType = widget.containerType?.let { viewTypeMap[it] } ?: defaultViewType
//            when (val containerId = widget.containerType) {
//                1, 2, 3 -> WidgetItems.CarouselWidget(
//                    data = widget,
//                    videos = ArrayList(widget.payload.map { it.toNewsVideoPojo() }),
//                    viewType = viewType
//                )
//
//                4, 5, 11 -> WidgetItems.RowTagsNewsWidget(
//                    data = widget,
//                    rowTagsNewsList = ArrayList(widget.payload.map { it.toTopNewsPojo() }),
//                    viewType = viewType
//                )
//
//                6, 7 -> WidgetItems.VerticalNewsWidget(
//                    data = widget,
//                    verticalNewsList = ArrayList(widget.payload.map { it.toTopNewsPojo() }),
//                    viewType = viewType
//                )
//
//                8 -> WidgetItems.RowTagsNewsWidget(
//                    data = widget,
//                    rowTagsNewsList = ArrayList(widget.payload.map { it.toTopNewsPojo() }),
//                    viewType = viewType
//                )
//
//                9 -> WidgetItems.ServicesWidget(
//                    data = widget,
//                    servicesList = ArrayList(widget.payload.map { it.toServicesPojo() }),
//                    viewType = viewType
//                )
//
//                10 -> WidgetItems.GridWidget(
//                    data = widget,
//                    newsList = ArrayList(widget.payload.map { it.toTopNewsPojo() }),
//                    viewType = viewType
//                )
//
//                else -> WidgetItems.GridWidget(
//                    data = widget,
//                    newsList = ArrayList(widget.payload.map { it.toTopNewsPojo() }),
//                    viewType = viewType
//                )
//            }
//        }
//
//        val adapter = WidgetAdapter(viewTypesIdsList, widgetIdsList)
//        binding.rvWidgets.layoutManager = LinearLayoutManager(context)
//        binding.rvWidgets.adapter = adapter
//        adapter.submitList(widgetItems)
//
////        val sortedWidgets = widgetList.sortedBy { it.orderSequence }
////        val idToId: Map<Int, Int> = widgetIdsList.mapValues { it.value.id }
////
////        val widgetItems = sortedWidgets.map { widget ->
//////            val widgetData = fetchWidgetData(widget)
////            val containerId = idToId[widget.containerType] ?: 0
////
////            when (containerId) {
////                1 -> WidgetItems.BannerWidget(widget,sampleBanners)
////                2 -> WidgetItems.CarouselWidget(widget, sampleVideos)
////                3 -> WidgetItems.GridWidget(widget, newsList)
////                4 -> WidgetItems.RowTagsNewsWidget(widget, newssList)
////                5 -> WidgetItems.ServicesWidget(widget, servicesData)
////                6 -> WidgetItems.VerticalNewsWidget(widget, newsList)
////                7 -> WidgetItems.VerticalNewsWidget(widget, newsList)
////                8 -> WidgetItems.RowTagsNewsWidget(widget, newssList)
////                9 -> WidgetItems.RowTagsNewsWidget(widget, newssList)
////                10 -> WidgetItems.RowTagsNewsWidget(widget, newssList)
////                11 -> WidgetItems.RowTagsNewsWidget(widget, newssList)
////                else -> WidgetItems.BannerWidget(widget,sampleBanners)
////            }
////        }
////
////        val adapter = WidgetAdapter(widgetItems)
////        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
////        binding.rvWidgets.adapter = adapter
//    }

        private fun fetchWidgetData(widget: WidgetOrder): List<Any> {
            return when (widget.containerType) {
                1 -> widget.payload
//            2 -> callCarouselApi(widget.endpointUrl)
//            3 -> callGridApi(widget.endpointUrl)
                else -> emptyList()
            }
        }

//    private fun showWidgets(widgetList: List<WidgetOrder>) {
//        val sortedWidgets = widgetList.sortedBy { it.orderSequence }
//        val widgetItems = sortedWidgets.map { widget ->
//            when (widget.containerType) {
//                1 -> WidgetItems.BannerWidget(widget)
//                2 -> WidgetItems.CarouselWidget(widget)
//                3 -> WidgetItems.GridWidget(widget)
//                else -> WidgetItems.BannerWidget(widget)
//            }
//        }
//
//        val adapter = WidgetAdapter(widgetItems)
//        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvWidgets.adapter = adapter
//    }

        override fun onDestroyView() {
            super.onDestroyView()

            _binding = null
        }

        override fun onPause() {
            super.onPause()
            // Stop any playing media when fragment goes to background
            val intent = Intent(ACTION_STOP_AUDIO)
            requireActivity().sendBroadcast(intent)
        }

        private fun useDefaultBackground() {
            val defaultDrawable =
                ContextCompat.getDrawable(requireActivity(), R.drawable.wallpaper_new)
//        binding.ivBackground.setImageDrawable(defaultDrawable)
        }

        private fun stopShimmer() {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.rvWidgets.visibility = View.VISIBLE
        }

        override fun onCategoryClick(id: String, catName: String) {
            viewLifecycleOwner.lifecycleScope.launch {
                fetchTabsNewsDataApi(id)
            }
        }

//    private fun init_TopNews_Views() {
//        /**
//         * Tags for news
//         */
//        val top_newstags_adapter = News_Tags_Adapter(context, arraylist_news_category, this)
//        val layoutManager1: RecyclerView.LayoutManager = LinearLayoutManager(
//            context,
//            LinearLayoutManager.HORIZONTAL, false
//        )
//        rvNewsTags.setLayoutManager(layoutManager1)
//        rvNewsTags.setHasFixedSize(true)
//        rvNewsTags.setItemViewCacheSize(5)
//        rvNewsTags.setAdapter(top_newstags_adapter)
//        /**
//         * Top news section
//         */
//        val top_news_adapter = Top_News_Adapter(context, arraylist_top_news, topCateName)
//        top_News_recyclerView.setHasFixedSize(true)
//        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
//            context,
//            LinearLayoutManager.VERTICAL, false
//        )
//        top_News_recyclerView.setLayoutManager(layoutManager)
//        top_News_recyclerView.setNestedScrollingEnabled(false)
//        top_News_recyclerView.setHasFixedSize(true)
//        top_News_recyclerView.setItemViewCacheSize(5)
//        top_News_recyclerView.setAdapter(top_news_adapter)
//    }

    }

//class NubitFragment : Fragment() {
//
//    private var _binding: FragmentNubitViewBinding? = null
//    private val binding get() = _binding!!
//
//    private val widgetViewModel: WidgetViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentNubitViewBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val adapter = WidgetAdapter(emptyList())
//        binding.rvWidgets.adapter = adapter
//        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
//
//        widgetViewModel.widgets.observe(viewLifecycleOwner) { widgets ->
//            if (!widgets.isNullOrEmpty()) {
//                val sorted = widgets.sortedBy { it.sort_order }
//                binding.rvWidgets.adapter = WidgetAdapter(sorted)
//            }
//        }
//
//        widgetViewModel.loadWidgets(order = "latest")
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
