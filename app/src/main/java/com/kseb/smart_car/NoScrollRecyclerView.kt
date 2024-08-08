package com.kseb.smart_car

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NoScrollRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        // 터치 이벤트를 차단하여 스크롤 비활성화
        return false
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        // 터치 이벤트를 차단하여 스크롤 비활성화
        return false
    }
}