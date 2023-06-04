package com.enuri.android.binding

import android.util.Log
import android.view.View
import android.widget.Toast
import com.enuri.android.ApplicationEnuri
import com.enuri.android.extend.activity.BaseActivity

class ShopClickBinding(private val baseActivity: BaseActivity, val v: View, val data : ShopData) {
    fun onClickListener(view: View) {
        if (!data.url.isNullOrEmpty()){
            //화면 이동 또는 여러 상태에서 클릭시 이동 가능
        }
    }
}