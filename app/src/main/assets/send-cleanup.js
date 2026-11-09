// 寄件页（page.cainiao.com/mcn/union-post）营销弹窗清理。
// 页面内有一套带优先级的 modal 注册表，容器 class 为 mn-modal + mnz-<kebab-name>。
// 这里只点名营销/广告类弹窗，寄件功能弹窗（选地址、选驿站、实名、费用等）保持原样。
(function () {
  if (window.__cnSendClean) return;
  window.__cnSendClean = true;

  // mnz- + 驼峰转短横线，对应页面里的营销弹窗
  var BLOCK = [
    'mnz-order-marketing-modal',           // OrderMarketingModal（券包/活动）
    'mnz-order-marketing-non-coupon-modal',// OrderMarketingNonCouponModal
    'mnz-order-create-marketing',          // OrderCreateMarketing
    'mnz-per-order-discount',              // PerOrderDiscount
    'mnz-discount-zone',                   // DiscountZone
    'mnz-over-time-coupon',                // OverTimeCoupon
    'mnz-universal-standard-ad',           // UniversalStandardAd
    'mnz-wechat-home-ad',                  // WechatHomeAd
    'mnz-wechat-per-order-discount',       // WechatPerOrderDiscount
    'mnz-alipay-per-order-discount',       // AlipayPerOrderDiscount
    'mnz-standard-ad',                     // StandardAd
    'mnz-detail-per-order-popup',          // DetailPerOrderPopup
    'mnz-member-entry-guide-modal',        // MemberEntryGuideModal
    'mnz-goods-guide',                     // GoodsGuide
    'mnz-notification-guide',              // NotificationGuide
    'mnz-ap-subscribe-message',            // ApSubscribeMessage
    'mnz-wechat-subscribe',                // WechatSubscribe
    'mnz-black-vip-auth'                   // BlackVipAuth
  ];

  var hide = function (el) {
    try {
      el.style.setProperty('display', 'none', 'important');
      el.style.setProperty('pointer-events', 'none', 'important');
    } catch (e) {}
  };

  // 营销弹窗被拿掉后，配套的 .mn-mask 会继续挡住点击，需要一起隐藏。
  var hideMasks = function () {
    var masks = document.querySelectorAll('.mn-mask');
    for (var i = 0; i < masks.length; i++) hide(masks[i]);
  };

  var clean = function () {
    var hit = false;
    for (var i = 0; i < BLOCK.length; i++) {
      var els = document.getElementsByClassName(BLOCK[i]);
      for (var j = els.length - 1; j >= 0; j--) {
        hide(els[j]);
        hit = true;
      }
    }
    if (hit) hideMasks();
  };

  clean();
  setTimeout(clean, 300);
  setTimeout(clean, 1200);
  setTimeout(clean, 2500);
  setInterval(clean, 800);

  new MutationObserver(clean).observe(document.documentElement, {
    childList: true,
    subtree: true
  });
})();
