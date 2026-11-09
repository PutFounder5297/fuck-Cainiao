# 0.2.5 独立包名发布记录

日期：2026-10-30。

## 发布标识

- 名称：FuckCainiao 老余版。
- 包名：`io.github.yuruichang.fuckcainiao`。
- 版本：`0.2.5 (13)`。
- 适配目标：菜鸟 `8.11.1023 (477)`。
- 源码仓库：<https://github.com/reova/FuckCainiao>。
- 上游来源：README 的「项目来源」节。
- 许可证：保留 GPL-3.0 与上游提交历史。

相较 0.2.4，本版改变发布包名和模块显示名称，过滤逻辑保持一致。
旧包 `io.github.duzhaokun123.fuckcainiao` 与本版是两个独立应用。
安装后只启用一个模块，作用域选择菜鸟，然后停止并重新打开菜鸟。

## 验证

- `:app:testDebugUnitTest`：8 项通过，0 失败。
- `node app/src/test/js/personal-cleanup.test.cjs`：6 组检查通过。
- `:app:assembleRelease`：通过。
- `:app:lintRelease`：0 错误、2 项原有警告。
- APK v2 签名验证通过。
- 独立包安装成功，系统报告版本 `0.2.5 (13)`。
- LSPosed 中停用旧包，启用「FuckCainiao 老余版」，作用域为菜鸟。
- 菜鸟冷启动日志显示仅本版 `0.2.5` 初始化，全部适配入口注册成功。
- 新包实机检查：首页主要入口与两个底部栏目保留，滚动搜索提示不显示，搜索页两个推广板块不显示。

其他业务场景没有在此次改包名后重新完整回归。
此前记录见 [0.2.4 搜索精简记录](search-cleanup-0.2.4.md) 与 [0.2.3 适配记录](compatibility-8.11.1023.md)。

## 分发

GitHub Release 使用标签 `13-0.2.5`，同时发布 APK 与 `SHA256SUMS`。
共享仓库申请与发布状态以实际 GitHub 页面为准。
