# 0.2.4 搜索界面精简验证记录

日期：2026-10-30。

## 交付范围

- 模块版本：`0.2.4 (12)`。显示作者：老余。
- 目标：菜鸟 `8.11.1023 (477)`。
- 设备：PLZ110 / Android 17 / LSPosed `2.2.1 (7912)`。
- 安装包：`FuckCainiao-0.2.4-cainiao-8.11.1023.apk`。
- APK SHA-256：`99b464ae3824f4ce5c9f2b69c4edecf3a6e28852682c4ef2f444e5f0c80f579d`。

## 改动

- 停止首页搜索提示的绑定和轮播，清空并隐藏文字组件。
- 保留搜索框本身、搜索按钮、相册和扫一扫入口。
- 隐藏搜索页的「你上个月买了多少包裹？」视频板块，并跳过该板块的广告加载。
- 隐藏「今日好物推荐」，并跳过该板块的推荐加载。
- 保留搜索输入、清空、取消和批量导入初始化。

对应入口：

- `HomeActionBarV891127.b(HomeSearchBarModelV9)`：只负责轮播提示；不跳过整个首页搜索栏初始化。
- `MainPackageSearchFragment.initPackageAdVideoView()`：视频推广入口。
- `MainPackageSearchFragment.initSearchHotGoodsView()`：商品推荐入口。

## 已验证

| 检查 | 结果 |
| --- | --- |
| `:app:testDebugUnitTest` | 8 项通过、0 失败；覆盖已有数据过滤规则 |
| `node app/src/test/js/personal-cleanup.test.cjs` | 6 组检查通过；覆盖已有网页过滤规则 |
| `:app:assembleRelease` | 通过 |
| `:app:lintRelease` | 0 错误、2 项原有警告：AGP 版本提示与缺少应用图标 |
| `apksigner verify --verbose` | APK v2 签名验证通过 |
| `git diff --check` | 通过 |

最终包安装并重新启动菜鸟后，检查了以下行为：

- 首页搜索提示的文字组件不再显示，搜索框与搜索按钮仍显示。
- 点击原搜索框中的空白区域，可以进入 `QueryPackageProActivity`。
- 搜索页不再显示目标视频板块和商品推荐板块。
- 批量复制单号、批量上传图片、小鸟包裹添加助手入口仍显示。
- 输入测试内容后，输入框与清除按钮正常显示。
- 清空后恢复搜索初始界面。
- 返回并再次打开搜索页后，目标板块没有恢复。

## 未验证范围

- 本次没有提交真实运单查询或执行批量导入。
- 本次没有重新覆盖全部物流、账号和寄件业务场景。
- 其他菜鸟版本、其他系统版本及服务端更换实现后的行为未验证。
- 此前的功能与验证记录见 [0.2.3 适配记录](compatibility-8.11.1023.md)。

没有清除菜鸟数据，没有付款、下单或退出登录。
