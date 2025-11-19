package com.mobi.nubitalcatel.core.models

import com.google.android.exoplayer2.ui.SubtitleView.ViewType


sealed class WidgetItems {
    data class CarouselWidget(
        val data: WidgetOrder,
        val videos: ArrayList<NewsVideoPojo>,
        val viewType: ViewsOrder
    ) : WidgetItems()

    data class BannerWidget(
        val data: WidgetOrder,
        val banners: ArrayList<External_game_pojo>,
        val viewType: ViewsOrder
    ) : WidgetItems()

    data class GridWidget(
        val data: WidgetOrder,
        val newsList: ArrayList<TopNewsPojo>,
        val viewType: ViewsOrder
    ) : WidgetItems()

    data class ServicesWidget(
        val data: WidgetOrder,
        val servicesList: ArrayList<Services_pojo>,
        val viewType: ViewsOrder
    ) : WidgetItems()

    data class VerticalNewsWidget(
        val data: WidgetOrder,
        val verticalNewsList: ArrayList<TopNewsPojo>,
        val viewType: ViewsOrder
    ) : WidgetItems()

    data class RowTagsNewsWidget(
        val data: WidgetOrder,
        val rowTagsNewsList: ArrayList<TopNewsPojo>,
        val viewType: ViewsOrder
    ) : WidgetItems()

}
