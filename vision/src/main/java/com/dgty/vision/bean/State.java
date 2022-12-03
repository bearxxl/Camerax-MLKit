
package com.dgty.vision.bean;


import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Title: NodeType
 * Description: 工作状态
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-11-05$ 10:28$
 * Created by xxl.
 */
@StringDef({
        State.idle,
        State.running,
})
@Retention(RetentionPolicy.SOURCE)
public @interface State {
    String idle = "idle";
    String running = "running";
}
