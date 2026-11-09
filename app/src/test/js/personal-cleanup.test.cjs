const assert = require('node:assert/strict');
const { JSDOM } = require('jsdom');
const fs = require('node:fs');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '../../main/assets/personal-cleanup.js'), 'utf8');
const url = 'https://page.cainiao.com/cn-app-web/profile/index.html#/';
function card(title) { return `<div style="width:100%;border-radius:0.24rem"><div>${title}</div><div><img></div></div>`; }
function fixture(page = url) {
 return new JSDOM(`<div id="root"><div class="adm-pull-to-refresh-content"><div id="page"><div id="account"><button>设置</button></div>${card('常用功能')}${card('我的订单')}${card('热门活动')}<div id="banner" class="adm-swiper" style="width:100%;margin-top:0.16rem;height:1.6rem"><img></div><div id="icon" class="adm-swiper" style="height:0.8rem"><img></div><div id="benefit" class="adm-swiper">寄件优惠</div></div></div></div>`, {url:page,runScripts:'outside-only',pretendToBeVisual:true});
}
(async () => {
 let passed=0;
 const dom=fixture(); const observers=[]; const NativeObserver=dom.window.MutationObserver; dom.window.MutationObserver=class extends NativeObserver { constructor(fn){super(fn);observers.push(this);} }; dom.window.eval(source); const doc=dom.window.document;
 assert.equal(doc.querySelectorAll('[data-fuckcainiao-hidden]').length,2);
 for(const id of ['page','account','icon','benefit']) assert.equal(doc.getElementById(id).hasAttribute('data-fuckcainiao-hidden'),false);
 assert.equal(Array.from(doc.querySelectorAll('[data-fuckcainiao-hidden]')).some(e=>e.textContent.includes('我的订单')),false); passed++;
 dom.window.eval(source); assert.equal(doc.querySelectorAll('style').length,1); passed++;
 const extra=doc.createElement('div'); extra.innerHTML=card('休闲娱乐'); doc.getElementById('page').append(extra.firstChild);
 await new Promise(r=>setTimeout(r,80)); assert.equal(doc.querySelectorAll('[data-fuckcainiao-hidden]').length,3); passed++;
 observers.forEach(o=>o.disconnect());
 for(const page of ['https://example.com/cn-app-web/profile/index.html','https://page.cainiao.com/other/index.html','https://page.cainiao.com/cn-app-web/profile/index.html#/my-interest']) {
  const d=fixture(page); d.window.eval(source); assert.equal(d.window.document.querySelectorAll('[data-fuckcainiao-hidden]').length,0); await new Promise(r=>setTimeout(r,40));  passed++;
 }
 console.log(`PASS: ${passed} DOM checks (section isolation, protected functions, duplicate injection, async refresh, URL and route scope)`);
})().catch(e=>{console.error(e);process.exitCode=1});
