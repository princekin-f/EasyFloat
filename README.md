## EasyFloat：Android悬浮窗框架
[![](https://jitpack.io/v/princekin-f/EasyFloat.svg)](https://jitpack.io/#princekin-f/EasyFloat)
[![License](https://img.shields.io/badge/License%20-Apache%202-337ab7.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> [EasyFloat：浮窗从未如此简单](https://www.jianshu.com/p/7d1a7c82094a)

## 特点功能：
- **支持单页面浮窗，无需权限申请**
- **支持全局浮窗、应用前台浮窗，需要授权悬浮窗权限**
- **自动权限检测、自动跳转浮窗权限管理页、自动处理授权结果**
- **支持系统浮窗的页面过滤**
- **支持拖拽，支持各种状态的回调**
- **支持默认位置的设定，支持对齐方式和偏移量的设定**
- **支持创建多个单页面浮窗、多个系统浮窗，Tag进行区分**
- **支持出入动画的设定，有默认动画，可自行替换（策略模式）**
- **使用简单、链式调用、可轻松修改浮窗View**
- **支持Kotlin DSL，可按需回调状态，摆脱Java的繁琐**
- **支持状态栏沉浸，侧滑打开、拖拽关闭**
- **支持解锁更多姿势，如：拖拽缩放、通知弹窗...**

|权限申请|系统浮窗|前台和过滤|
|:---:|:---:|:---:|
|![](https://github.com/princekin-f/EasyFloat/blob/master/readme/%E6%9D%83%E9%99%90%E7%94%B3%E8%AF%B7.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/readme/%E7%B3%BB%E7%BB%9F%E6%B5%AE%E7%AA%97.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/readme/%E6%B5%AE%E7%AA%97%E7%BC%A9%E6%94%BE.gif)|

|状态回调|拖拽边界|拓展使用|
|:---:|:---:|:---:|
|![](https://github.com/princekin-f/EasyFloat/blob/master/readme/Callbacks.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/readme/BoarderAndSlide.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/readme/DragUtils.gif)|

## 下载体验：
- [直接下载测试APK](http://fir.zsc7454.cn/qgp2)，或者扫码下载：

![](https://github.com/princekin-f/EasyFloat/blob/master/readme/downloadImage.png)

## 关于集成：
- **在项目的根目录的`build.gradle`添加：**
```
allprojects {
    repositories {
        ...
	maven { url 'https://jitpack.io' }
    }
}
```
- **在应用模块的`build.gradle`添加：**
```
dependencies {
    implementation 'com.github.princekin-f:EasyFloat:2.0.3'
}
```

```
dependencies {
    // 上一代稳定版
    implementation 'com.github.princekin-f:EasyFloat:1.3.4'
}
```
[老版本 v1.3.4 使用文档](https://github.com/princekin-f/EasyFloat/blob/master/readme/README_1.3.4.md)

## 一行代码搞定Android浮窗，浮窗从未如此简单：
```
EasyFloat.with(this).setLayout(R.layout.float_test).show()
```

## 关于初始化：
> 2.0.0开始，无需初始化


## 关于权限声明：
- 权限声明为非必须；
- **当使用到系统浮窗（`ShowPattern.ALL_TIME`、`ShowPattern.FOREGROUND`、`ShowPattern.BACKROUND`）；**
- 需要在`AndroidManifest.xml`进行权限声明。
```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

## 完整使用示例：
```
EasyFloat.with(this)
    // 设置浮窗xml布局文件/自定义View，并可设置详细信息
    .setLayout(R.layout.float_app) { }
    // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
    .setShowPattern(ShowPattern.ALL_TIME)
    // 设置吸附方式，共15种模式，详情参考SidePattern
    .setSidePattern(SidePattern.RESULT_HORIZONTAL)
    // 设置浮窗的标签，用于区分多个浮窗
    .setTag("testFloat")
    // 设置浮窗是否可拖拽
    .setDragEnable(true)
    // 浮窗是否包含EditText，默认不包含
    .hasEditText(false)
    // 设置浮窗固定坐标，ps：设置固定坐标，Gravity属性和offset属性将无效
    .setLocation(100, 200)
    // 设置浮窗的对齐方式和坐标偏移量
    .setGravity(Gravity.END or Gravity.CENTER_VERTICAL, 0, 200)
    // 设置当布局大小变化后，整体view的位置对齐方式
    .setLayoutChangedGravity(Gravity.END)
    // 设置拖拽边界值
    .setBorder(100, 100，800，800)
    // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
    .setMatchParent(widthMatch = false, heightMatch = false)
    // 设置浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
    .setAnimator(DefaultAnimator())
    // 设置系统浮窗的不需要显示的页面
    .setFilter(MainActivity::class.java, SecondActivity::class.java)
    // 设置系统浮窗的有效显示高度（不包含虚拟导航栏的高度），基本用不到，除非有虚拟导航栏适配问题
    .setDisplayHeight { context -> DisplayUtils.rejectedNavHeight(context) }
    // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
    // ps：通过Kotlin DSL实现的回调，可以按需复写方法，用到哪个写哪个
    .registerCallback {
        createResult { isCreated, msg, view ->  }
        show {  }
        hide {  }
        dismiss {  }
        touchEvent { view, motionEvent ->  }
        drag { view, motionEvent ->  }
        dragEnd {  }
    }
    // 创建浮窗（这是关键哦😂）
    .show()
```

**在Java中使用Kotlin DSL不是很方便，状态回调还有一种常规的接口方式：**

```
.registerCallbacks(new OnFloatCallbacks() {
    // 各种回调...
    ...
})
```

如果想要在Java是使用Kotlin DSL，可以参考Demo。

### 悬浮窗权限的检测、申请：
- **无需主动进行权限申请，创建结果、申请结果可在`OnFloatCallbacks`的`createdResult`获取。**

```
// 权限检测
PermissionUtils.checkPermission(this)

// 权限申请，参数2为权限回调接口
PermissionUtils.requestPermission(this，OnPermissionResult)
```

### 浮窗的相关API：
```
// 关闭浮窗，force为强制关闭，有退出动画也不执行
dismiss(tag: String? = null, force: Boolean = false)

// 隐藏浮窗
hide(tag: String? = null)

// 显示浮窗
show(tag: String? = null)

// 设置是否可拖拽
dragEnable(dragEnable: Boolean, tag: String? = null)

// 浮窗是否显示
isShow(tag: String? = null)

// 获取我们设置的浮窗View
getFloatView(tag: String? = null)

// 更新浮窗坐标，未指定坐标执行吸附动画
updateFloat(tag: String? = null, x: Int = -1, y: Int = -1)

// *******************  系统浮窗独有  *******************
// 添加单个浮窗过滤页面
filterActivity(activity: Activity, tag: String? = null)

// 添加多个浮窗过滤页面
filterActivities(tag: String? = null, vararg clazz: Class<*>)

// 移除单个浮窗过滤页面
removeFilter(activity: Activity, tag: String? = null)

// 移除多个浮窗过滤页面
removeFilters(tag: String? = null, vararg clazz: Class<*>)

// 清空过滤页面
clearFilters(tag: String? = null)

```
**PS：`? = null` 代表可选参数，不填也行，默认值为null。**


## 拖拽关闭、侧滑创建：
```
// 在拖拽回调中，注册拖拽关闭
drag { view, motionEvent ->
    DragUtils.registerDragClose(motionEvent, object : OnTouchRangeListener {
        override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
            // 震动、视图调整等...
        }

        override fun touchUpInRange() {
            // 关闭浮窗等...
            EasyFloat.dismiss(tag, true)
        }
    })
}

// 在Activity的dispatchTouchEvent中，注册侧滑创建
DragUtils.registerSwipeAdd(ev, object : OnTouchRangeListener {
    override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
        // 震动、视图调整等...
    }

    override fun touchUpInRange() {
        // 浮窗创建等，详情参考：SwipeTestActivity
        showFloat()
    }
})
```


## 关于混淆：
> 自带混淆规则，正常情况下无需手动导入。

## 关于感谢：
- **权限适配：[FloatWindowPermission](https://github.com/zhaozepeng/FloatWindowPermission)**

## 关于更新：
- [查看版本更新日志](https://github.com/princekin-f/EasyFloat/blob/master/UpdateDoc.md)

## 交流和激励：
- **为了大家更好的交流和反馈，我们创建了QQ群：`818756969`**
- 如果该库对你提供了帮助，你可以小小的赏赞一下作者，同样作者也会非常感谢你！我们一起众筹云测😘

<div align="center">
<img src="https://github.com/princekin-f/EasyFloat/blob/master/readme/EasyFloatGroup.jpeg"  width="266">
<img src="https://github.com/princekin-f/EasyFloat/blob/master/readme/Alipay.jpeg" width="266">
<img src="https://github.com/princekin-f/EasyFloat/blob/master/readme/WeChatPay.jpeg" width="266" >
</div>


License
-------

    Copyright 2019 Liu Zhenfeng.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
