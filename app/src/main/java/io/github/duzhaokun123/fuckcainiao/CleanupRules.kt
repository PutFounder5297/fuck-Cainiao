package io.github.duzhaokun123.fuckcainiao

internal object CleanupRules {
    const val ABOUT_URL = "guoguo://go/about_fuck_cainiao"

    fun isAboutUrl(value: String?): Boolean =
        value?.substringBefore('?')?.substringBefore('#') == ABOUT_URL

    fun isShippingPromotion(value: Any?): Boolean = when (value) {
        is String -> value.contains("寄件") &&
            listOf("元", "优惠", "领券", "折").any(value::contains)
        is Map<*, *> -> value.values.any(::isShippingPromotion)
        is List<*> -> value.any(::isShippingPromotion)
        else -> false
    }

    fun cleanPackageGroup(row: Map<*, *>): Boolean {
        val content = row["data"] as? MutableMap<*, *> ?: return false
        if (!isShippingPromotion(content["tagItem"])) return false
        content.remove("tagItem")
        return true
    }

    fun keepNavigation(key: String): Boolean =
        key == "homepage" || key == "personal_center" || key == "personal_center_h5"

    fun isPersonalPromotion(value: Any?): Boolean {
        val row = value as? Map<*, *> ?: return false
        val content = row["materialContentMapper"] as? Map<*, *>
        return row["group_id"] == "entertainment" ||
            row["group_type"] == "bottom_place_holder" ||
            content?.get("group_id") == "entertainment" ||
            content?.get("group_type") == "bottom_place_holder"
    }

    fun templateName(value: Any?): String? =
        ((value as? Map<*, *>)?.get("template") as? Map<*, *>)?.get("name") as? String

    fun isAbout(value: Any?): Boolean =
        ((value as? Map<*, *>)?.get("materialContentMapper") as? Map<*, *>)
            ?.get("jumpUrl") == ABOUT_URL

    // ---- 弹窗与广告 ----

    /** 菜鸟 PopupType 中属于打扰/广告的类型。NOTIFICATION 不在此列：部分功能引导也用它。 */
    private val blockedPopupTypes = setOf("ADS", "GOOD_EVALUATE", "APPUPDATE")

    /** 弹窗名称关键词，覆盖走统一队列但没有标记为 ADS 的营销弹窗。 */
    private val blockedPopupWords = listOf(
        "营销", "插屏", "广告", "好评", "抽奖", "红包", "膨胀", "奖励",
        "通知", "更新", "升级", "推广", "福利", "活动", "CPA", "推荐", "领券", "弹屏"
    )

    fun isBlockedPopup(typeName: String?, name: String?): Boolean {
        if (typeName != null && typeName in blockedPopupTypes) return true
        val shown = name ?: return false
        return blockedPopupWords.any(shown::contains)
    }

    /** 直接弹出的广告/推广弹窗。功能弹窗（取件码、备注、驿站授权等）不在名单内。 */
    private val blockedDialogs = setOf(
        "com.cainiao.wireless.packagelist.ad.PackageImportAdDialog",
        "com.cainiao.wireless.packagelist.luckydraw.ui.LuckyDrawDialog",
        "com.cainiao.wireless.packagelist.luckydraw.ui.PackageLuckyDrawDialog",
        "com.cainiao.wireless.packagelist.luckydraw.ui.PackageLuckyDrawToast",
        "com.taobao.cainiao.logistic.ui.jsnewview.view.LogisticDetailMemberRewardDialog",
        "com.cainiao.wireless.ads.ecpm.view.EcpmGetRewardDialog",
        "com.cainiao.wireless.ads.view.guide_ads.dialog.GuideAdsGetRewardDialog",
        "com.cainiao.wireless.ads.view.reward.WishFreeRewardImageDialog",
        "com.cainiao.wireless.ads.view.reward.WishFreeSelectRewardPopup",
        "com.cainiao.wireless.ads.sku_dialog.SKUWebViewDialog",
        "com.cainiao.wireless.widget.dialog.AppVersionUpdateDialog",
        "com.cainiao.wireless.widget.dialog.CommonPopScreenDialog",
        "com.cainiao.wireless.homepage.view.guide.NewUserAuthGuideDialog",
        "com.cainiao.wireless.shop.task.guide.chain.GuoJiangGuideDialog",
        "com.taobao.cainiao.service.impl.business.biz.view.GetGrowthValueDialog",
        "com.taobao.cainiao.service.impl.business.biz.view.GetGrowthValueToast"
    )

    fun isBlockedDialog(className: String): Boolean = className in blockedDialogs

    fun cleanHeader(rows: MutableList<*>) {
        // Header cards can be reordered by the server. Keep functional cards by type.
        rows.removeAll { value ->
            val type = (value as? Map<*, *>)?.get("type") as? String ?: return@removeAll false
            type.startsWith("promotion") || type.startsWith("vip3_horizontal_bar") ||
                type.startsWith("new_user_award") || type.startsWith("todo_list") || type == "icons"
        }
        rows.forEach { value ->
            val row = value as? Map<*, *> ?: return@forEach
            for (key in listOf("bizData", "data")) {
                val content = row[key] as? Map<*, *> ?: continue
                val items = content["items"] as? MutableList<*> ?: continue
                items.removeAll { (it as? Map<*, *>)?.get("key") == "exchange_old_things" }
            }
        }
    }
}
