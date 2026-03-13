package com.example.alarm_jinxuan.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

class GlideUtil private constructor(){

    companion object {
        private var instance : GlideUtil ?= null
        fun createGlideUtil(): GlideUtil{
            if (instance == null) instance = GlideUtil()
            return instance!!
        }
    }

    fun loadImage(context: Context,resource: Int,view: ImageView) {
        Glide.with(context)
            .load(resource)
            .into(view)
    }
}