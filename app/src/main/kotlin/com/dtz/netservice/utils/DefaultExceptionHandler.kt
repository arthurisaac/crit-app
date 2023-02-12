package com.dtz.netservice.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dtz.netservice.app.ApplicationClass
import com.dtz.netservice.ui.activities.login.LoginActivity
import kotlin.system.exitProcess


class DefaultExceptionHandler(var activity: Activity) : Thread.UncaughtExceptionHandler {
    @SuppressLint("UnspecifiedImmutableFlag")
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            ApplicationClass.instance?.baseContext , 0, intent, PendingIntent.FLAG_ONE_SHOT
        )
        //Restart your app after 2 seconds
        val mgr = ApplicationClass.instance?.baseContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 1000] = pendingIntent
        //finishing the activity.
        activity.finish()
        //Stopping application
        exitProcess(2)
    }
}