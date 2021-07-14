## 版本更新日志
#### v 2.0.3:
- [支持直接设置View（`xml/View`即可使用）](https://github.com/princekin-f/EasyFloat/issues/110)；
- [支持设置布局大小变化后，整体View的位置对齐方式](https://github.com/princekin-f/EasyFloat/pull/159)；
- [修复`onCreate`无法创建单页面浮窗的问题](https://github.com/princekin-f/EasyFloat/issues/132)；
- [修复在别的应用，横屏宽度异常问题](https://github.com/princekin-f/EasyFloat/issues/135)；
- [修复浮窗创建失败，无法创建相同TAG的问题](https://github.com/princekin-f/EasyFloat/issues/138)。

#### v 2.0.2:
- 解决Java关键字导致的包名冲突。

#### v 2.0.0:
- 新增拖拽关闭、侧滑创建、状态栏沉浸、拖拽边界等功能；
- 重构单页面浮窗（创建和PopupWindow同类型的子窗口），减少了API数目，提高利用率；
- 优化使用体验，无需手动init，无需手动调起软键盘。

#### v 1.3.4:
- 优化细节。

#### v 1.3.3:
- 支持传入`with(context)`实现部分系统浮窗功能；
- 对未合理`init`、未合理`with(context)`的情况，直接进行抛异常；
- 其他一些细节性的优化。

#### v 1.3.2:
- 优化细节。

#### v 1.3.1:
- 优化`createdResult`回调。

#### v 1.3.0：
- 可动态适配虚拟导航栏：`setDisplayHeight(displayHeight: OnDisplayHeight)`

#### v 1.2.9：
- [优化细节。](https://github.com/princekin-f/EasyFloat/issues/57)

#### v 1.2.8：
- 优化细节。

#### v 1.2.7：
- 优化浮窗闪烁，横屏不能拖拽到底等问题。

#### v 1.2.6：
- 优化细节。

#### v 1.2.5：
- 优化页面过滤。

#### v 1.2.4：
- 优化全面屏适配。

#### v 1.2.3:
- 新增仅后台显示（`ShowPattern.BACKGROUND`）。

#### v 1.2.2:
- 优化细节。

#### v 1.2.1:
- 适配小米全面屏。

#### v 1.2.0:
- 优化系统浮窗的管理方式。

#### v 1.1.3：
- 优化部分全面屏手机，系统浮窗不能拖到底部的问题。

#### v 1.1.2:
- 优化页面过滤和主动隐藏的逻辑。

#### v 1.1.1:
- 当系统浮窗包含`EditText`时，优化返回键的相关处理。

#### v 1.1.0：
- 开放浮窗权限申请的API；
- 更名浮窗回调方法名，不再使用方法重载的方式。

#### v 1.0.7:
- 优化页面过滤细节上的不足。

#### v 1.0.6:
- 新增浮窗`View`的获取，方便`View`的修改。

#### v 1.0.5:
- 优化代码和功能，支持`FloatCallbacks`的按需调用（Kotlin DSL）。

#### v 1.0.4:
- 可选择是否开启前台Service，可自定义通知栏消息。

#### v 1.0.3:
- 修改魅族手机，权限申请回调异常的问题；
- 为系统浮窗的`EditText`，提供了软键盘的打开、关闭后的焦点移除；
- 但暂未提供软键盘的关闭监听方案，希望大家一起努力。

#### v 1.0.2:
- 修改`enum`包名，解决Java特殊路径无法调用的问题；
- 添加`@JvmOverloads`注解，支持对Java的方法重载。