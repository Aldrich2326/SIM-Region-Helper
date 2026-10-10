# 1.1.0-preview2 验证记录

日期：2026-10-11。此版本将 Nrfr Next 更名为 **SIM 地区助手 / SIM Region Helper**，同步应用内名称、源码入口、仓库文档和预览图。

- `:app:testDebugUnitTest :app:assembleRelease :app:lintRelease` 成功。
- 26 项测试通过，0 失败、0 错误；lint 完整运行，0 错误、24 警告。
- 更新后的界面预览使用虚构 SIM 数据；核对了长名称在大字体下的显示。
- APK 显示名称为“SIM 地区助手”，版本 1.1.0-preview2 / versionCode 3。
- 保留原包名和签名，支持覆盖升级并延续已有还原快照。
- APK 签名及 zipalign 校验通过，Apache-2.0、MIT、NOTICE、NOTICE-ANDROID17、CREDITS 五项内置文件与源码一致。
- 原 LICENSE、MIT 版权许可和上游 NOTICE-ANDROID17 未改动，原作者和上游仓库入口保留。
- 国家码配置逻辑与前一预览版一致。本次未在手机重新安装，也未修改国家码；此前真机测试边界继续适用，详见 [兼容性说明](COMPATIBILITY.md)。

安装包 `SIM-Region-Helper-v1.1.0-preview2.apk`：6,614,953 字节。

SHA-256：

```text
E86BCA703154BED2B4E7A6A1C34F63D6B8B76F5F531825F57648912C25C47878
```

签名证书 SHA-256 与上一版本一致：

```text
1700c01882521c94d6d1eadf96e20d2313f55aa7e23b30926120dd41f94036af
```
