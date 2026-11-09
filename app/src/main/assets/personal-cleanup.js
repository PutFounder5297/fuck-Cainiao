(function () {
    'use strict';
    if (window.__fuckCainiaoPersonalCleanup) return;
    if (location.protocol !== 'https:' || location.hostname !== 'page.cainiao.com' ||
        location.pathname !== '/cn-app-web/profile/index.html') return;
    window.__fuckCainiaoPersonalCleanup = true;

    var style = document.createElement('style');
    style.textContent = '[data-fuckcainiao-hidden] { display: none !important; }';
    (document.head || document.documentElement).appendChild(style);

    function hide(element) {
        if (element && !element.hasAttribute('data-fuckcainiao-hidden')) {
            element.setAttribute('data-fuckcainiao-hidden', '');
        }
    }

    function clean() {
        var route = location.hash.split('?')[0];
        if (route && route !== '#' && route !== '#/') return;
        var content = document.querySelector('.adm-pull-to-refresh-content');
        if (!content) return;
        Array.prototype.forEach.call(content.querySelectorAll('div'), function (element) {
            if (element.children.length) return;
            var title = element.textContent.trim();
            if (title !== '热门活动' && title !== '休闲娱乐') return;
            var section = element.parentElement;
            // The profile card owns its title directly. Do not walk up to a page container.
            if (section && section.firstElementChild === element &&
                section.style.borderRadius === '0.24rem' && section.style.width === '100%') {
                hide(section);
            }
        });
        Array.prototype.forEach.call(content.querySelectorAll('.adm-swiper'), function (element) {
            // Match the bottom banner, excluding small carousels in functional cards.
            if (!element.textContent.trim() && element.querySelector('img') &&
                element.style.width === '100%' && element.style.marginTop === '0.16rem' &&
                (element.style.height === '1.6rem' || element.style.height === '2.4rem')) {
                hide(element);
            }
        });
    }

    var pending = false;
    function schedule() {
        if (pending) return;
        pending = true;
        requestAnimationFrame(function () {
            pending = false;
            clean();
        });
    }
    new MutationObserver(schedule).observe(document.documentElement, {
        childList: true,
        subtree: true,
        characterData: true
    });
    window.addEventListener('hashchange', schedule);
    clean();
})();
