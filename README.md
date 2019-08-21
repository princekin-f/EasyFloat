## EasyFloat：Android浮窗框架
[![](https://jitpack.io/v/princekin-f/EasyFloat.svg)](https://jitpack.io/#princekin-f/EasyFloat)
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
- **根据浮窗复杂度、重要性，可自主选择前后台Service**
- **使用简单、链式调用，无侵入性**
- **支持xml直接使用，满足拖拽控件的需求**
- **支持解锁更多姿势，如：拖拽缩放、通知弹窗...**


|权限申请|系统浮窗|
|:---:|:---:|
|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E6%9D%83%E9%99%90%E7%94%B3%E8%AF%B7.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E7%B3%BB%E7%BB%9F%E6%B5%AE%E7%AA%97.gif)|

|前台和过滤|状态回调|拓展使用|
|:---:|:---:|:---:|
|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E6%B5%AE%E7%AA%97%E7%BC%A9%E6%94%BE.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E6%B5%AE%E7%AA%97Callbacks.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/dialog%E5%92%8Cxml%E4%BD%BF%E7%94%A8.gif)|

## 关于集成：
- 在项目的根目录的`build.gradle`添加：
```
allprojects {
    repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
- 在应用模块的`build.gradle`添加：
```
dependencies {
    implementation 'com.github.princekin-f:EasyFloat:1.0.4'
}
```

## 一行代码搞定Android浮窗，浮窗从未如此简单：
```
EasyFloat.with(this).setLayout(R.layout.float_test).show()
```

## 关于初始化：
- 全局初始化为非必须；
- 当浮窗为仅前台显示，或者设置了浮窗过滤页面;
- 需要在项目的`Application`中进行全局初始化，进行页面生命周期检测。
```
EasyFloat.init(this, isDebug)
```

## 关于权限声明：
- 权限声明为非必须；
- 如果使用到系统浮窗（`ShowPattern.ALL_TIME`、`ShowPattern.FOREGROUND`）；
- 需要在`AndroidManifest.xml`进行权限声明。
```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
- 在使用到系统浮窗的情况下，不仅要声明浮窗权限，还要声明启动系统浮窗的服务；
- **该服务和上述系统浮窗权限，成对出现。**
```
<service android:name="com.lzf.easyfloat.service.FloatService" />
```
### 关于前台Service：
- 可根据系统浮窗的重要性和复杂度，选择是否开启前台Service（默认后台Service）；
- 从`Android 9.0`开始，前台Service需要在`AndroidManifest.xml`进行权限声明。
```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```
**PS：前台Service会在通知栏创建一条消息，有默认实现，也可进行消息自定义。**

## 完整使用示例：
```
EasyFloat.with(this)
    // 设置浮窗xml布局文件
    .setLayout(R.layout.float_app)
    // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
    .setShowPattern(ShowPattern.ALL_TIME)
    // 设置吸附方式，共15种模式，详情参考SidePattern
    .setSidePattern(SidePattern.RESULT_HORIZONTAL)
    // 设置浮窗的标签，用于区分多个浮窗
    .setTag("testFloat")
    // 设置浮窗是否可拖拽
    .setDragEnable(true)
    // 设置浮窗固定坐标，ps：设置固定坐标，Gravity属性和offset属性将无效
    .setLocation(100, 200)
    // 设置浮窗的对齐方式和坐标偏移量
    .setGravity(Gravity.END or Gravity.CENTER_VERTICAL, 0, 200)
    // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
    .setMatchParent(widthMatch = false, heightMatch = false)
    // 设置Activity浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
    .setAnimator(DefaultAnimator())
    // 设置系统浮窗的出入动画，使用同上
    .setAppFloatAnimator(AppFloatDefaultAnimator())
    // 设置系统浮窗的不需要显示的页面
    .setFilter(MainActivity::class.java, SecondActivity::class.java)
    // 是否启动前台Service，仅针对系统浮窗；有默认的Notification，可不传
    .startForeground(true, floatNotification(this))
    // 设置我们传入xml布局的详细信息
    .invokeView(OnInvokeView { })
    // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
    .registerCallbacks(object : OnFloatCallbacks {
        override fun createdResult(isCreated: Boolean, msg: String?, view: View?) {}

        override fun show(view: View) {}

        override fun hide(view: View) {}

        override fun dismiss() {}

        override fun touchEvent(view: View, event: MotionEvent) {}

        override fun drag(view: View, event: MotionEvent) {}

        override fun dragEnd(view: View) {}
    })
    // 创建浮窗（不要忘记哦😂）
    .show()
```

### 悬浮窗权限检测，可用于设置引导页面：
- 无需主动进行权限申请，创建结果、申请结果可在`OnFloatCallbacks`的`createdResult`获取。
```
PermissionUtils.checkPermission(this)
```

### Activity浮窗的相关API：
```
// 关闭浮窗
dismiss(activity: Activity? = null, floatTag: String? = null)

// 隐藏浮窗
hide(activity: Activity? = null, floatTag: String? = null)

// 显示浮窗
show(activity: Activity? = null, floatTag: String? = null)

// 设置是否可拖拽
setDragEnable(activity: Activity? = null, dragEnable: Boolean, floatTag: String? = null )

// 浮窗是否显示
isShow(activity: Activity? = null, floatTag: String? = null)
```

**PS：`? = null` 代表可选参数，不填也行，默认值为null。下同。**

### 系统浮窗的相关API：
```
// 关闭浮窗
dismissAppFloat(context: Context, tag: String? = null)

// 隐藏浮窗
hideAppFloat(context: Context, tag: String? = null)

// 显示浮窗
showAppFloat(context: Context, tag: String? = null)

// 设置是否可拖拽
appFloatDragEnable(dragEnable: Boolean, tag: String? = null)

// 浮窗是否显示
appFloatIsShow(tag: String? = null)

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

### 系统浮窗中使用`EditText`：
**1，为`EditText`设置点击事件，调用`openInputMethod`方法：**
```
InputMethodUtils.openInputMethod(editText, tag)
```
**2，软键盘关闭时，调用`closedInputMethod`方法：**
```
InputMethodUtils.closedInputMethod(tag)
```

### 直接在xml布局使用拖拽控件：
```
<com.lzf.easyfloat.widget.activityfloat.FloatingView
    android:id="@+id/floatingView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center">

    <ImageView
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:src="@mipmap/ic_launcher_round" />

</com.lzf.easyfloat.widget.activityfloat.FloatingView>
```
- **需要为FloatingView设置点击事件，不然无法拖拽：**
```
floatingView.setOnClickListener {}
```

## 关于混淆：
```
-keep class com.lzf.easyfloat.** {*;}
```

## 关于感谢：
- **权限适配：[FloatWindowPermission](https://github.com/zhaozepeng/FloatWindowPermission)**


---
## 更新日志：
#### v 1.0.4:
- 可选择是否开启前台Service，可自定义通知栏消息。

#### v 1.0.3:
- 修改魅族手机，权限申请回调异常的问题；
- 为系统浮窗的`EditText`，提供了软键盘的打开、关闭后的焦点移除；
- 但暂未提供软键盘的关闭监听方案，希望大家一起努力。

#### v 1.0.2:
- 修改`enum`包名，解决Java特殊路径无法调用的问题；
- 添加`@JvmOverloads`注解，支持对Java的方法重载。
