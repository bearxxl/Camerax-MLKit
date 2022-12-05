package com.dgty.vision.view

import android.content.Context
import com.dgty.vision.R
import com.lxj.xpopup.core.BottomPopupView

/**
 * Title: BottomDialog
 * Description: 底部弹出窗
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-03 15:53
 * Created by xxl.
 */
class BottomDialog(context: Context) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.item_bottom
    }
}