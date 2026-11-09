package io.github.duzhaokun123.fuckcainiao

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupRulesTest {
    @Test
    fun groupCleanupRemovesPricePromotionAndPreservesParcelSummary() {
        val data = linkedMapOf<String, Any>("title" to "近日签收", "subtitle" to "4件",
            "tagItem" to mapOf("data" to mapOf("text" to "寄件1.6元起")))
        val row = mapOf("data" to data, "decorator" to mapOf("groupId" to "signed"))
        assertTrue(CleanupRules.cleanPackageGroup(row))
        assertFalse(data.containsKey("tagItem"))
        assertEquals("近日签收", data["title"])
        assertEquals("4件", data["subtitle"])
        assertFalse(CleanupRules.cleanPackageGroup(row))
    }

    @Test
    fun groupCleanupPreservesFunctionalLabelsAndAcceptsMissingData() {
        val data = linkedMapOf<String, Any>("tagItem" to mapOf("text" to "寄件订单"))
        assertFalse(CleanupRules.cleanPackageGroup(mapOf("data" to data)))
        assertTrue(data.containsKey("tagItem"))
        assertFalse(CleanupRules.cleanPackageGroup(emptyMap<String, Any>()))
        assertFalse(CleanupRules.isShippingPromotion("共有3件"))
    }
    @Test
    fun aboutRoutingAcceptsReferrerWithoutCapturingOtherLinks() {
        assertTrue(CleanupRules.isAboutUrl(CleanupRules.ABOUT_URL + "?referrer=guoguo%3A%2F%2Fgo"))
        assertTrue(CleanupRules.isAboutUrl(CleanupRules.ABOUT_URL + "#about"))
        assertFalse(CleanupRules.isAboutUrl(CleanupRules.ABOUT_URL + "_other"))
        assertFalse(CleanupRules.isAboutUrl(null))
    }
    @Test
    fun navigationKeepsHomeAndPersonalRegardlessOfOrder() {
        val keys = listOf("personal_center", "im", "homepage", "jijian", "station")
        assertEquals(listOf("personal_center", "homepage"), keys.filter(CleanupRules::keepNavigation))
        assertTrue(CleanupRules.keepNavigation("personal_center_h5"))
    }

    @Test
    fun headerFiltersReorderedPromotionsWithoutRemovingSearchAndTools() {
        val search = mapOf("type" to "native_search_bar")
        val tools = mapOf("type" to "kingkong", "bizData" to mapOf("items" to arrayListOf(
            mapOf("key" to "send"), mapOf("key" to "exchange_old_things"), mapOf("key" to "pickup")
        )))
        val rows = arrayListOf<Any?>(mapOf("type" to "promotion_v2"), search,
            mapOf("type" to "vip3_horizontal_bar"), tools, mapOf("type" to "todo_list_v2"),
            mapOf("type" to "icons"))
        CleanupRules.cleanHeader(rows)
        assertEquals(listOf(search, tools), rows)
        val items = ((tools["bizData"] as Map<*, *>)["items"] as List<*>)
        assertEquals(listOf("send", "pickup"), items.map { (it as Map<*, *>)["key"] })
    }

    @Test
    fun headerAcceptsEmptyAndUnknownDataAndRepeatedRefresh() {
        CleanupRules.cleanHeader(arrayListOf<Any?>())
        val rows = arrayListOf<Any?>(null, mapOf("type" to "unknown_functional_card"))
        CleanupRules.cleanHeader(rows)
        CleanupRules.cleanHeader(rows)
        assertEquals(2, rows.size)
    }

    @Test
    fun personalPromotionUsesFieldsInsteadOfMatchingArbitraryText() {
        assertTrue(CleanupRules.isPersonalPromotion(mapOf("group_id" to "entertainment")))
        assertTrue(CleanupRules.isPersonalPromotion(mapOf("materialContentMapper" to
            mapOf("group_type" to "bottom_place_holder"))))
        assertFalse(CleanupRules.isPersonalPromotion(mapOf("title" to "entertainment")))
        assertFalse(CleanupRules.isPersonalPromotion(null))
    }

    @Test
    fun popupShieldBlocksAdsButKeepsFunctionalDialogs() {
        assertTrue(CleanupRules.isBlockedPopup("ADS", null))
        assertTrue(CleanupRules.isBlockedPopup("GOOD_EVALUATE", "任意名称"))
        assertTrue(CleanupRules.isBlockedPopup("APPUPDATE", null))
        assertTrue(CleanupRules.isBlockedPopup("OTHER", "营销插屏弹窗"))
        assertTrue(CleanupRules.isBlockedPopup("OTHER", "包裹导入营销弹窗"))
        assertTrue(CleanupRules.isBlockedPopup("OTHER", "首页CPA剪切板弹框"))
        assertTrue(CleanupRules.isBlockedPopup("NOTIFICATION", "通知选择图文弹窗"))
        assertTrue(CleanupRules.isBlockedPopup("OTHER", "红包弹窗"))
        assertFalse(CleanupRules.isBlockedPopup("OTHER", "剪切板识别运单号弹窗"))
        assertFalse(CleanupRules.isBlockedPopup("STATION_PRIVACY", "驿站授权弹窗"))
        assertFalse(CleanupRules.isBlockedPopup("RELATION_UPDATE", "常用手机号纳入亲友包裹"))
        assertFalse(CleanupRules.isBlockedPopup("NOTIFICATION", "privacy_pop_dialog"))
        assertFalse(CleanupRules.isBlockedPopup(null, null))
    }

    @Test
    fun dialogBlacklistCoversAdClassesAndSparesFunctionalOnes() {
        assertTrue(CleanupRules.isBlockedDialog("com.cainiao.wireless.packagelist.ad.PackageImportAdDialog"))
        assertTrue(CleanupRules.isBlockedDialog("com.cainiao.wireless.widget.dialog.AppVersionUpdateDialog"))
        assertFalse(CleanupRules.isBlockedDialog("com.cainiao.wireless.packagelist.remarkdialog.PackageRemarkDialog"))
        assertFalse(CleanupRules.isBlockedDialog("com.cainiao.wireless.widget.dialog.LogisticHelperPopupDialog"))
        assertFalse(CleanupRules.isBlockedDialog(""))
    }
}
