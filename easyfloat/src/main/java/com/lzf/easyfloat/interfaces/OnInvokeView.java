package com.lzf.easyfloat.interfaces;

import android.view.View;

/**
 * @author: liuzhenfeng
 * @function: 设置浮窗内容的接口，由于kotlin暂不支持SAM，所以使用Java接口
 * @date: 2019-06-30  14:19
 */
public interface OnInvokeView {

    /**
     * 设置浮窗布局的具体内容
     *
     * @param view 浮窗布局
     */
    void invoke(View view);
}
