# FuckCainiao

[许可证](LICENSE)
![Android SDK min 24](https://img.shields.io/badge/Android%20SDK-%3E%3D%2024-brightgreen?style=flat-square&logo=android)
![Android SDK target 36](https://img.shields.io/badge/Android%20SDK-target%2036-brightgreen?style=flat-square&logo=android)
![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue?style=flat-square)

菜鸟界面优化和广告移除

适配版作者：老余。

## 项目来源

本项目基于 [duzhaokun123/FuckCainiao](https://github.com/duzhaokun123/FuckCainiao)
修改，由老余维护此适配版本。保留上游提交历史和 [GPL-3.0 许可证](LICENSE)。

## 当前适配

模块版本：`0.2.7 (15)`。适配目标：菜鸟安卓版 `8.11.1105 (478)`。

发布包名：`io.github.reova.fuckcainiao`。这是独立维护的适配版。
使用本版时，停用原版模块，避免两个模块同时作用于菜鸟。

- 底部只保留「首页」和「我的」，支持栏目动态重建。
- 首页保留取包裹、寄包裹、出库码，过滤推广行与物换物入口。
- 保留开屏、推荐商品、悬浮广告、物流广告和旧版原生「我的」的过滤。
- 新版网页「我的」过滤热门活动与底部图片横幅，保留账号、地址簿、订单和设置。
- 设置页保留「关于 FuckCainiao」，支持新版链接附加的来源参数。
- 过滤物流详情的奖励浮层与包裹分组中的寄件价格推广，保留包裹数量和业务标签。
- 关于弹窗显示作者「老余」，没有捐赠按钮。
- 首页搜索框没有滚动文案；搜索页隐藏「你上个月买了多少包裹？」与「今日好物推荐」。

在 LSPosed 中启用模块，作用域只选择「菜鸟」，然后彻底停止并重新打开菜鸟。
本次没有清除菜鸟数据。搜索页改动见 [0.2.4 验证记录](docs/search-cleanup-0.2.4.md)。
此前的适配范围见 [0.2.3 验证记录](docs/compatibility-8.11.1023.md)。
当前发布包检查见 [0.2.6 发布记录](docs/release-0.2.6.md)。
此前独立包名的检查见 [0.2.5 发布记录](docs/release-0.2.5.md)。

## 构建与检查

使用 JDK 17 和 Android SDK 36。Java/Kotlin 字节码目标仍为 Java 11。

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintRelease :app:assembleRelease
```

默认输出：`app/build/outputs/apk/release/app-release.apk`。
签名沿用项目原来的本地 debug 签名配置。

网页过滤的测试使用临时安装的 jsdom，不参与模块运行：

```powershell
npm install --prefix .local/web-tests --no-save --no-audit --no-fund jsdom@26.1.0
$env:NODE_PATH = (Resolve-Path .local/web-tests/node_modules).Path
node app/src/test/js/personal-cleanup.test.cjs
```

JDK 17 在中文路径下读取测试启动参数时会出现类加载错误。构建目录位于 Google Drive 时，
同步占位文件也会影响 Gradle 的文件检查。遇到这些问题时，使用纯英文的本地目录构建。

## 下载

在 [GitHub Releases](https://github.com/reova/FuckCainiao/releases/latest) 下载 APK。

安装包文件名：`FuckCainiao-0.2.6-cainiao-8.11.1023.apk`。

## 更新记录

### 0.2.7

- 适配菜鸟 `8.11.1105 (478)`，原有 18 处 hook 在该版本全部仍然可用。
- 新增统一弹窗拦截：hook `com.cainiao.commonlibrary.popupmanager.a.c`，按 `PopupType`
  （ADS / GOOD_EVALUATE / APPUPDATE）与弹窗名称关键词丢弃插屏、好评、通知引导、升级等弹窗。
- 新增 DialogFragment 与 Dialog 两层黑名单拦截，覆盖不走统一队列的广告弹窗
  （抽奖、会员奖励、激励奖励、心愿免单、SKU 推广、通用弹屏、新用户引导等）。
- 新增精确入口拦截：首页营销插屏、包裹导入营销（Toast 与插屏两种形态）、物流会员奖励弹窗。
- 新增寄件页（H5）营销弹窗清理：进入 `/mcn/union-post` 时注入脚本，按容器 class 屏蔽
  `OrderMarketingModal`、`DiscountZone`、`WechatHomeAd` 等 18 个营销弹窗，保留选地址、
  选驿站、实名、费用等功能弹窗。
- 修正 `PackageImportAdManager` 的真实 dex 类名（Kotlin 混淆后为 `packagelist.ad.c`）。
- 按用户要求**保留**「我的」页 V3 会员条与绿色回收入口。

### 0.2.6

- GitHub 账号改名后，包名同步为 `io.github.reova.fuckcainiao`。
- 模块名称为「FuckCainiao」，作者信息为「老余」。
- 使用本版时，停用 `io.github.yuruichang.fuckcainiao` 与原版模块。
- 过滤逻辑与 0.2.5 保持一致。

### 0.2.5

- 使用独立包名 `io.github.yuruichang.fuckcainiao` 发布。
- 保留 0.2.4 的界面精简与搜索推广过滤。

## 上游历史效果

|              前               |             后              |
|:----------------------------:|:--------------------------:|
|  ![before](arts/before.png)  |  ![after](arts/after.png)  |
| ![before2](arts/before2.png) | ![after2](arts/after2.png) |

## Thanks

### 工具

[jadx](https://github.com/skylot/jadx)

开发者助手专业版(`cn.trinea.android.developertools.pro`)

### 库

[AOSP](https://source.android.com/)

[EzXHelper](https://github.com/KyuubiRan/EzXHelper)

[xposed](https://forum.xda-developers.com/xposed)
