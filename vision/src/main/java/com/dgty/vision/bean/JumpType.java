
package com.dgty.vision.bean;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Title: NodeType
 * Description: 动作判断
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-11-05$ 10:28$
 * Created by xxl.
 */
@IntDef({
        JumpType.UP,
        JumpType.DOWN,
        JumpType.UNKNOW,

})
@Retention(RetentionPolicy.SOURCE)
public @interface JumpType {
    int UP = 1;
    int DOWN = 2;
    int UNKNOW = 3;
}
