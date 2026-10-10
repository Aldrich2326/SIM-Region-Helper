# 1.1.0-preview1 验证记录

日期：2026-10-10。验证对象为 Nrfr Next 的 Android release 构建。

## 主机检查

运行 `:app:testDebugUnitTest :app:assembleRelease :app:lintRelease` 成功。

- 单元测试共 **26 项**，0 失败、0 错误。其中 14 项为 Compose 界面交互与布局检查。
- Lint 完整运行，**0 错误、23 警告**。警告未全部消除；不能将该结果描述为零警告。
- 对 UI 的 SIM 未就绪、无 SIM、重复地区、处理中禁用，以及必须点击应用按钮才触发操作进行了检查。
- 使用虚构 SIM 数据渲染浅色、深色、375dp 小屏、横屏、平板、2 倍字体与权限/作者页面，并核对预览。每个主界面布局均检查应用按钮可通过滚动到达。
- 作者与许可入口可达；Apache-2.0 与 MIT 全文可离线打开。
- 普通页面切换不使用自定义位移动画；采用系统文字大小和原生点击反馈。

截图采用 Robolectric 的 Android 原生图形绘制，不包含真实 SIM 信息或手机截图；它们不能代替厂商真机兼容验证。未宣称已完成真实读屏器、Android 16 新界面安装、实际通话或移动数据测试。

## 安装包

- 显示名称：Nrfr Next。
- 版本：1.1.0-preview1，versionCode 2。
- 包名保留此前本地构建的历史 ID，以支持相同签名升级与还原快照延续。
- 已签名 APK：6,614,953 字节，SHA-256：

```text
06D3CAE0D8D6D55F71835BCAC6B38490D31A7DBA0938EE3E40CD6A1ACF86CCB9
```

- 签名验证及 zipalign 对齐校验通过，release 非调试包。
- 签名证书 SHA-256：`1700c01882521c94d6d1eadf96e20d2313f55aa7e23b30926120dd41f94036af`，与先前本地构建一致。
- 清单无 INTERNET 权限；Apache-2.0、MIT、NOTICE、NOTICE-ANDROID17、CREDITS 五个文件与源码逐字节一致。
- 原 LICENSE、MIT 许可及直接上游 NOTICE-ANDROID17.md 未改动。
- 签名私钥、密码、个人诊断与本地设备序列号不进入源码仓库及发行包。

## 真机范围

先前本地构建完成 Android 16 真机启动和权限检查，用户随后反馈“好像可以用了”。本次仅修改并验证新界面和发布物，没有重新在手机安装新版本或修改国家码。受控保存、还原、电话及移动数据验证仍未完成，详见 [兼容性说明](COMPATIBILITY.md)。
