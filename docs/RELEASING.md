# 构建、验证与发布

当前发布验证记录见 [1.1.0-preview2](VALIDATION-preview2.md)；[前一版本记录](VALIDATION.md)保留历史名称与当时结果。

SIM 地区助手 使用 JDK 21、Gradle 8.9、AGP 8.7.3、Kotlin 2.0.21、Android SDK 35 / build-tools 34.0.0。`minSdk=29`，`targetSdk=34`。

设置 `ANDROID_HOME` 或在未跟踪的 `local.properties` 中设置 `sdk.dir`。运行：

```sh
./gradlew :app:testDebugUnitTest :app:assembleRelease :app:lintRelease
```

Windows 可使用 `gradlew.bat`。单元测试包括输入与权限握手、国家码稳定核验，以及使用虚构 SIM 数据的 Compose UI 测试。Robolectric 首次运行需下载其 Android 测试运行时。

UI 测试不调用真实电话配置服务，截图输出到 `app/build/ui-previews/`。检查浅色、深色、小屏、横屏、平板和大字体预览后，可将公开用的虚构数据截图放入 `docs/images/`。这些预览不是实际 SIM 修改成功的证据。

## 签名

Release 构建输出未签名 APK。使用维护者私有密钥先 `zipalign`，再 `apksigner sign`，最后运行 `apksigner verify --verbose --print-certs`、对齐校验、清单与内置许可文件检查，并计算 SHA-256。私钥和密码不得提交到 Git、CI 或 Release。发布包与先前本地测试包保持同一签名，版本号递增。

GitHub Actions 仅运行测试、构建与 lint；不包含私钥，不自动发布 APK。维护者人工上传已核验的签名 APK。自动生成的源码压缩包不包含私钥或本地构建环境。

## 发布记录

- 发布为预览版，明确记录受控真机验证范围，不将主机 UI 截图等同于真机兼容。
- Release 提供签名 APK、SHA256SUMS 和版本说明；保留原作与直接来源链接。
- APK 自带 Apache-2.0、MIT、NOTICE、NOTICE-ANDROID17 与 CREDITS；关于页支持离线查看许可。
- 完整版权、作者与来源关系见根目录 [CREDITS.md](../CREDITS.md) 和 [NOTICE](../NOTICE)。
