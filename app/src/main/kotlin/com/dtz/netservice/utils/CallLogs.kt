package com.dtz.netservice.utils

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract

object CallLogs {

    fun callList(context: Context): MutableList<MutableMap<String, Any>>  {
        val callLogList: MutableList<MutableMap<String, Any>> = mutableListOf()
        val numberCol = CallLog.Calls.NUMBER
        val typeCol: String = CallLog.Calls.TYPE // 1 - Incoming, 2 - Outgoing, 3 - Missed
        val dateCol: String = CallLog.Calls.DATE
        val durationCol: String = CallLog.Calls.DURATION

        val projection = arrayOf(numberCol, typeCol, dateCol, durationCol)
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection, null, null, null, null
        )

        val numberColIdx = cursor!!.getColumnIndex(numberCol)
        val typeColIdx = cursor.getColumnIndex(typeCol)
        val dateColIdx = cursor.getColumnIndex(dateCol)
        val durationColIdx = cursor.getColumnIndex(durationCol)

        while (cursor.moveToNext()) {
            val callLogMap: MutableMap<String, Any> = mutableMapOf()
            val number = cursor.getString(numberColIdx)
            val duration = cursor.getString(durationColIdx)
            val type = cursor.getString(typeColIdx)
            val date = cursor.getString(dateColIdx)

            callLogMap["date"] = date.toLong()
            callLogMap["number"] = number
            callLogMap["type"] = type.toInt()
            callLogMap["duration"] = duration.toLong()

            callLogList.add(callLogMap)
        }
        cursor.close()
        return callLogList
    }

}