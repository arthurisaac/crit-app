package com.dtz.netservice.utils

import android.content.Context
import android.provider.ContactsContract
import android.provider.Telephony

object SMSes {

    fun smsList(context: Context): MutableList<MutableMap<String, Any>>  {
        val smsList: MutableList<MutableMap<String, Any>> = mutableListOf()

        val dateCol = Telephony.TextBasedSmsColumns.DATE
        val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
        val textCol = Telephony.TextBasedSmsColumns.BODY
        val typeCol = Telephony.TextBasedSmsColumns.TYPE // 1 - Inbox, 2 - Sent

        val projection = arrayOf(dateCol, numberCol, textCol, typeCol)

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection, null, null, null
        )

        val dateColIdx = cursor!!.getColumnIndex(dateCol)
        val numberColIdx = cursor.getColumnIndex(numberCol)
        val textColIdx = cursor.getColumnIndex(textCol)
        val typeColIdx = cursor.getColumnIndex(typeCol)

        while (cursor.moveToNext()) {
            val smsMap: MutableMap<String, Any> = mutableMapOf()

            val date = cursor.getString(dateColIdx)
            val number = cursor.getString(numberColIdx)
            val text = cursor.getString(textColIdx)
            val type = cursor.getString(typeColIdx)

            smsMap["date"] = date.toLong()
            smsMap["number"] = number
            smsMap["text"] = text
            smsMap["type"] = type.toInt()

            smsList.add(smsMap)
        }

        cursor.close()
        return smsList
    }

}