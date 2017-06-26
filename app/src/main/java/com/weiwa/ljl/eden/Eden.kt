package com.weiwa.ljl.eden

import android.os.Environment

import java.io.File

/**
 * Created by hzfd on 2017/5/17.
 */

object Eden {
    var CacheCategory = File(Environment.getExternalStorageDirectory().absolutePath + "/Eden")
}
