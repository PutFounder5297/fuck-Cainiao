# 0.2.6 发布记录

日期：2026-10-30。

## 发布标识

- 模块名称：FuckCainiao。
- 作者：老余。
- 包名：`io.github.reova.fuckcainiao`。
- 版本：`0.2.6 (14)`。
- 适配目标：菜鸟 `8.11.1023 (477)`。
- 源码：<https://github.com/reova/FuckCainiao>。
- 许可证：GPL-3.0，保留上游来源说明与提交历史。

GitHub 账号改名后，本版同步更新发布包名。过滤逻辑与 0.2.5 一致。
模块名称不包含作者后缀，作者只写在作者信息中。

## 安装迁移

安装本版后，在 LSPosed 中启用 `io.github.reova.fuckcainiao`，作用域选择菜鸟。
停用旧包 `io.github.yuruichang.fuckcainiao` 和原版 `io.github.duzhaokun123.fuckcainiao`。
停止并重新打开菜鸟，无需清除菜鸟数据。

## 验证

- 8 项单元测试与 6 组网页检查通过。
- 发布构建通过，静态检查为 0 错误、2 项原有警告。
- 安装包名称为 FuckCainiao；描述和关于弹窗保留作者老余。
- 系统报告包名 `io.github.reova.fuckcainiao`、版本 `0.2.6 (14)`。
- 实机冷启动日志中，仅 `io.github.reova.fuckcainiao` 的 0.2.6 初始化。
- 原包与上一个包名没有同时加载。
- 当前手机检查首页与搜索页；未重新完整覆盖全部账号、寄件和物流业务场景。

发布标签为 `14-0.2.6`。安装包校验值随 Release 的 `SHA256SUMS` 分发。
