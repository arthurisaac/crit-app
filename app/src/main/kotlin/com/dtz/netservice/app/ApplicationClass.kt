package com.dtz.netservice.app

import android.app.Application
import android.content.Context


class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        instance = this
    }

    override fun getApplicationContext(): Context {
        return super.getApplicationContext()
    }

    companion object {
        private var mContext: Context? = null
        var instance: ApplicationClass? = null
    }
}