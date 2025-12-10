package com.example.dried_shrimp

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class FullScreenVideoView : VideoView {
    // 這三個建構函式是必須的
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * 覆寫 onMeasure 方法，這是實現全螢幕的關鍵。
     * 系統會呼叫這個方法來測量 View 應該有多大。
     * 預設的 VideoView 會考慮影片的寬高比，但我們在這裡忽略它。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // getDefaultSize 會取得父容器建議的尺寸
        val width = getDefaultSize(0, widthMeasureSpec)
        val height = getDefaultSize(0, heightMeasureSpec)

        // 強制將測量結果設定為父容器的寬和高
        setMeasuredDimension(width, height)
    }
}