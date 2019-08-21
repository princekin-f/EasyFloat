package com.lzf.easyfloat.enums

/**
 * @author: liuzhenfeng
 * @function: 浮窗的贴边模式
 * @date: 2019-07-01  17:34
 */
enum class SidePattern {

    // 默认不贴边，跟随手指移动
    DEFAULT,
    // 左、右、上、下四个方向固定（一直吸附在该方向边缘，只能在该方向的边缘移动）
    LEFT, RIGHT, TOP, BOTTOM,
    // 根据手指到屏幕边缘的距离，自动选择水平方向的贴边、垂直方向的贴边、四周方向的贴边
    AUTO_HORIZONTAL, AUTO_VERTICAL, AUTO_SIDE,
    // 拖拽时跟随手指移动，结束时贴边
    RESULT_LEFT, RESULT_RIGHT, RESULT_TOP, RESULT_BOTTOM,
    RESULT_HORIZONTAL, RESULT_VERTICAL, RESULT_SIDE

}