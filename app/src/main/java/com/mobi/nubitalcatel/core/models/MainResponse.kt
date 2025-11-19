package com.mobi.nubitalcatel.core.models

//data class ApiResponse<T>(
//    val success: Boolean,
//    val data: T?,
//    val message: String?
//)
//
//data class Widget(
//    val id: String,
//    val type: String,
//    val title: String,
//    val content: String,
//    val order: Int
//)

data class ApiResponse(
    val responseMessage: String,
    val responseStatus: Boolean,
    val responseStatusCode: Int,
    val responseObject: List<WidgetOrder>
)

data class ApiContainerResponse(
    val responseMessage: String,
    val responseStatus: Boolean,
    val responseStatusCode: Int,
    val responseObject: List<ContainerOrder>
)

data class ApiViewContainerResponse(
    val responseMessage: String,
    val responseStatus: Boolean,
    val responseStatusCode: Int,
    val responseObject: List<ViewsOrder>
)

data class ApiNewsCateContainerResponse(
    val responseMessage: String,
    val responseStatus: Boolean,
    val responseStatusCode: Int,
    val responseObject: List<TagsPojo>
)

data class ApiNewsDataContainerResponse(
    val responseMessage: String,
    val responseStatus: Boolean,
    val responseStatusCode: Int,
    val responseObject: ContentData
)

data class ContentData(
    val content: ArrayList<ContentNewsData>,
)

data class KeyValue(
    val key: String,
    val value: String
)

data class ContentNewsData(
    val title: String,
    val categoryId: Int,
    val redirectLink: String,
    val description: String,
    val imageUrl: String,
    val feedProvider: String,
    val feedProviderName: String,
)
data class ViewsOrder(
    val id: Int,
    val viewType: String,
    val viewDescription: String,
)

data class ContainerOrder(
    val id: Int,
    val containerName: String,
)

data class WidgetOrder(
    val id: Int,
    val orderSequence: Int,
    val containerType: Int,
    val viewTypeId: Int? = null,
    val containerTypeDescription: String?,
    val title: String?,
    val endpointUrl: String?,
    val heroImageUrl: String?,
    val thumbnailUrl: String?,
    val autoScroll: Boolean?,
    val horizontalScroll: Boolean?,
    val verticalScroll: Boolean?,
    val status: Int,
    val payload: List<Payload>
)

data class Payload(
    // Common fields
    val id: Int? = null,
    val orderSequence: Int? = null,
    val containerType: Int? = null,
    val containerTypeDescription: String? = null,
    val title: String? = null,
    val endpointUrl: String? = null,
    val heroImageUrl: String? = null,
    val thumbnailUrl: String? = null,

    // For service-type payloads
    val defaultFocus: Boolean? = null,

    // For news/article-type payloads
    val categoryId: Int? = null,
    val redirectLink: String? = null,
    val description: String? = null,
    val postedDate: Long? = null,
    val imageUrl: String? = null,
    val feedProvider: String? = null,
    val feedProviderName: String? = null,

    // Metadata (common to both)
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: Int? = null,
    val updatedBy: Int? = null,
    val createdByName: String? = null,
    val updatedByName: String? = null,
    val status: Int? = null
)


//data class WidgetOrder(
//    val id: Int,
//    val orderSequence: Int,
//    val containerType: Int,
//    val viewTypeId: Int? = null,
//    val containerTypeDescription: String?,
//    val title: String?,
//    val endpointUrl: String?,
//    val heroImageUrl: String?,
//    val thumbnailUrl: String?,
//    val autoScroll: Boolean?,
//    val horizontalScroll: Boolean?,
//    val verticalScroll: Boolean?,
//    val status: Int,
//    val payload: List<Payload>
//)
//
//data class Payload(
//    val id: Int,
//    val orderSequence: Int,
//    val containerType: Int,
//    val containerTypeDescription: String?,
//    val title: String?,
//    val endpointUrl: String?,
//    val heroImageUrl: String?,
//    val thumbnailUrl: String?,
//    val defaultFocus: Boolean
//)
fun Payload.toNewsVideoPojo(): NewsVideoPojo {
    return NewsVideoPojo(
        title = title ?: "Untitled",
        url = endpointUrl ?: ""
    )
}

// Similar conversion functions for other POJOs (e.g., TopNewsPojo, Services_pojo, External_game_pojo)
fun Payload.toTopNewsPojo(): TopNewsPojo {
    return TopNewsPojo(
        title = title ?: "Untitled",
        description = title ?: "",
        image = heroImageUrl ?: "",
        postedDate = "",
        feedProvider = "",
        redirectLink = endpointUrl ?: "",
        adMobId = "",
        isAds = false
    )
}

fun Payload.toServicesPojo(): Services_pojo {
    return Services_pojo(
        title = title ?: "Untitled",
        banner_image = endpointUrl ?: ""
    )
}

fun TopNewsPojo.toPayload(): Payload {
    return Payload(
        id = 0,
        orderSequence = 0,
        containerType = 0,
        containerTypeDescription = null,
        title = this.title,
        endpointUrl = this.redirectLink,
        heroImageUrl = this.imageUrl ?: this.image,
        thumbnailUrl = this.imageUrl ?: this.image,
        defaultFocus = false
    )
}

data class TagsPojo(
    var id: String? = null,
    var categoryName: String? = null
)

data class MiddleNewsPojoOld(
    var title: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var isAds: Boolean = false,
    var postedDate: String? = null,
    var posted_time: String? = null,
    var package_name: String? = null,
    var feedProvider: String? = null,
    var feedProviderName: String? = null,
    var redirectLink: String? = null,
    var adMobId: String? = null,
    var ad_network: String? = null,
    var ad_type: String? = null,
    var ad_display_type: String? = null,
    var isAddLoaded: Boolean = false,
    var isAddLoading: Boolean = false,
    var open_with: String? = null,
    var newsBy: String? = null
)

data class TopNewsPojo(
    var title: String? = null,
    var newsBy: String? = null,
    var description: String? = null,
    var feedProvider: String? = null,
    var postedDate: String? = null,
    var posted_time: String? = null,
    var redirectLink: String? = null,
    var feedProviderName: String? = null,
    var package_name: String? = null,
    var adMobId: String? = null,
    var ad_type: String? = null,
    var image: String? = null,
    var imageUrl: String? = null,
    var ad_display_type: String? = null,
    var isAddLoaded: Boolean = false,
    var isAds: Boolean = false,
    var isAddLoading: Boolean = false,
    var ad_network: String? = null,
    var open_with: String? = null,
)

data class RowNewsPojo(
    var id: Int? = null,
    var title: String? = null,
    var categoryId: Int? = null,
    var description: String? = null,
    var redirectLink: String? = null,
    var imageUrl: String? = null,
    var feedProvider: String? = null,
    var feedProviderName: String? = null,
    var postedDate: Long? = null,
    var status: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var createdBy: Int? = null,
    var createdByName: String? = null,
    var updatedBy: Int? = null,
    var updatedByName: String? = null
)

data class External_game_pojo(
    var id: String? = null,
    var title: String? = null,
    var category: String? = null,
    var banner_image: String? = null,
    var banner_thumb_image: String? = null,
    var output_link: String? = null,
    var package_name: String? = null,
    var action: String? = null,
    var portrait: String? = null,
    var redirect_link: String? = null,
    var ad_unit_id: String? = null,
    var ad_network: String? = null,
)

data class OtherServicesPojo(
    var title: String? = null,
    val icon: String? = null,
    val open_with: String? = null,
    val package_name: String? = null,
    val redirect: String? = null,
)

data class Services_pojo(
    private var id: String? = null,
    val title: String? = null,
    val banner_image: String? = null,
    val output_link: String? = null,
    val package_name: String? = null,
    val redirect_link: String? = null,
    val open_with: String? = null
)

data class TopApps_Pojo(
    private var id: String? = null,
    val title: String? = null,
    val banner_image: String? = null,
    val output_link: String? = null,
    val package_name: String? = null,
    val redirect_link: String? = null,
    val open_with: String? = null
)

data class RegisterDeviceResponse(
    val responseMessage: String?,
    val responseStatus: Boolean?,
    val responseStatusCode: Int?,
    val responseObject: DeviceInfo?
)

data class DeviceInfo(
    val id: Int?,
    val androdId: String?,
    val imeiPrimary: String?,
    val imeiSecondary: String?,
    val manufacturer: String?,
    val osVersion: String?,
    val apiLevel: String?,
    val timeZone: String?,
    val localeLanguage: String?,
    val localeCountry: String?,
    val status: Int?,
    val token: String?
)
