package com.lzf.easyfloat.example.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.anim.DefaultAnimator;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.R;
import com.lzf.easyfloat.example.logger;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.permission.PermissionUtils;
import com.lzf.easyfloat.utils.DisplayUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author: liuzhenfeng
 * @function: Java兼容性测试
 * @date: 2019-08-15  18:38
 */
public class JavaTestActivity extends Activity {

    private final String TAG = "JavaTestActivity";

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);

        findViewById(R.id.testJava).setOnClickListener(v -> {
            EasyFloat.with(this)
                    .setTag(TAG)
                    .setLayout(R.layout.float_custom, view ->
                            view.findViewById(R.id.textView).setOnClickListener(v1 -> toast("onClick")))
                    .setGravity(Gravity.END, 0, 100)
                    // 在Java中使用Kotlin DSL回调
                    .registerCallback(builder -> {
                        builder.createResult((aBoolean, s, view) -> {
                            toast("createResult：" + aBoolean.toString());
                            return null;
                        });

                        builder.dismiss(() -> {
                            toast("dismiss");
                            return null;
                        });

                        // ...可根据需求复写其他方法

                        return null;
                    })
                    .show();
        });

        findViewById(R.id.tvCloseFloat).setOnClickListener(v -> EasyFloat.dismiss(TAG));
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void test() {
        EasyFloat.with(this)
                // 设置浮窗xml布局文件
                .setLayout(R.layout.float_app, view -> {
                    // view就是我们传入的浮窗xml布局
                })
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
                .setGravity(Gravity.END | Gravity.CENTER_VERTICAL, 0, 200)
                // 设置拖拽边界值
                .setBorder(100, 100, 800, 800)
                // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
                .setMatchParent(false, false)
                // 设置浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
                .setAnimator(new DefaultAnimator())
                // 设置系统浮窗的不需要显示的页面
                .setFilter(MainActivity.class, SecondActivity.class)
                // 设置系统浮窗的有效显示高度（不包含虚拟导航栏的高度），基本用不到，除非有虚拟导航栏适配问题
                .setDisplayHeight(DisplayUtils.INSTANCE::rejectedNavHeight)
                // 浮窗的一些状态回调，如：创建结果、显示、隐藏、销毁、touchEvent、拖拽过程、拖拽结束。
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void createdResult(boolean isCreated, @Nullable String msg, @Nullable View view) {

                    }

                    @Override
                    public void show(@NotNull View view) {

                    }

                    @Override
                    public void hide(@NotNull View view) {

                    }

                    @Override
                    public void dismiss() {

                    }

                    @Override
                    public void touchEvent(@NotNull View view, @NotNull MotionEvent event) {

                    }

                    @Override
                    public void drag(@NotNull View view, @NotNull MotionEvent event) {

                    }

                    @Override
                    public void dragEnd(@NotNull View view) {

                    }
                })
                // Kotlin DSL实现回调效果，和registerCallbacks二选一即可，该方式主要针对Kotlin，Java使用起来并不怎么方便
                .registerCallback(builder -> {
                    builder.createResult((aBoolean, s, view) -> {
                        logger.e("Java使用kotlin DSL：" + aBoolean);
                        return null;
                    });

                    builder.dismiss(() -> {
                        toast("dismiss");
                        return null;
                    });

                    // ...可根据需求复写其他方法

                    return null;
                })
                // 创建浮窗（这是关键哦😂）
                .show();


        // 测试方法重载
        EasyFloat.dragEnable(false);

        PermissionUtils.checkPermission(this);

    }

}
