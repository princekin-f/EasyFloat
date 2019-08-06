## EasyFloat：Android浮窗框架

> Android浮窗，从未如此简单...

### 特点功能：
- 支持单页面浮窗，无需权限申请
- 支持全局浮窗、应用前台浮窗，需要授权悬浮窗权限
- 自动权限检测、自动跳转浮窗权限管理页、自动处理授权结果
- 支持系统浮窗的页面过滤
- 支持拖拽，支持各种状态的回调
- 支持默认位置的设定，支持对齐方式和偏移量的设定
- 支持创建多个单页面浮窗、多个系统浮窗，Tag进行区分
- 支持出入动画的设定，有默认动画，可自行替换（策略模式）
- 使用简单、链式调用，侵入性低
- 支持xml直接使用，满足拖拽控件的需求
- 支持解锁更多姿势，如：拖拽缩放、通知弹窗...


|权限申请|系统浮窗|
|:---:|:---:|
|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E6%9D%83%E9%99%90%E7%94%B3%E8%AF%B7.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E7%B3%BB%E7%BB%9F%E6%B5%AE%E7%AA%97.gif)|

|前台和过滤|状态回调|拓展使用|
|:---:|:---:|:---:|
|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E6%B5%AE%E7%AA%97%E7%BC%A9%E6%94%BE.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/%E6%B5%AE%E7%AA%97Callbacks.gif)|![](https://github.com/princekin-f/EasyFloat/blob/master/gif/dialog%E5%92%8Cxml%E4%BD%BF%E7%94%A8.gif)|


### 一行代码搞定Android浮窗，浮窗从未如此简单：
```
EasyFloat.with(this).setLayout(R.layout.float_app).show()
```

### 关于初始化：
> 全局初始化为非必须，当浮窗为仅前台显示，或者设置了浮窗过滤页面，需要进行全局初始化，进行进行页面生命周期检测。
```
EasyFloat.init(this, isDebug)
```

### 关于权限声明：
> 权限声明为非必须，如果使用到系统浮窗（ShowPattern.ALL_TIME、ShowPattern.FOREGROUND），需要在`AndroidManifest.xml`进行权限声明。
```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```
> 在使用到系统浮窗的情况下，不仅要声明浮窗权限，还要声明启动系统浮窗的服务。该服务和浮窗权限成对出现。
```
<service android:name="com.lzf.easyfloat.service.FloatService" />
```

### 完整使用示例：
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

### 关于混淆：
> 在默认的混淆规则下，是能够正常使用的，若混淆时出现意外，可以选择过滤掉此框架。
```
-keep class com.lzf.easyfloat.** {*;}
```

#### 关于感谢：
> 权限适配：[FloatWindowPermission](https://github.com/zhaozepeng/FloatWindowPermission)
