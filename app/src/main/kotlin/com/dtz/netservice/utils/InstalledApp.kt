package com.dtz.netservice.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract

object InstalledApp {

    @SuppressLint("QueryPermissionsNeeded")
    fun appsList(context: Context): MutableList<MutableMap<String, Any>>  {
        val appList: MutableList<MutableMap<String, Any>> = mutableListOf()

        val list = context.packageManager.getInstalledPackages(0)
        for (i in list.indices) {
            val appsMap: MutableMap<String, Any> = mutableMapOf()
            val packageInfo = list[i]
            //if (packageInfo!!.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val appName = packageInfo.applicationInfo.loadLabel(context.packageManager).toString()
            appsMap["name"] = appName
            //}
            appList.add(appsMap)
        }

        return appList
    }

}