package com.dtz.netservice.utils

import android.content.Context
import android.provider.ContactsContract

object Contacts {

    fun contactsList(context: Context): MutableList<MutableMap<String, Any>>  {
        val contactsList: MutableList<MutableMap<String, Any>> = mutableListOf()

        val nameCol = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        val numberCol = ContactsContract.CommonDataKinds.Phone.NUMBER

        val projection = arrayOf(nameCol, numberCol,)
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )

        val nameColIdx = cursor!!.getColumnIndex(nameCol)
        val numberColIdx = cursor.getColumnIndex(numberCol)


        while (cursor.moveToNext()) {
            val contactsMap: MutableMap<String, Any> = mutableMapOf()
            val number = cursor.getString(numberColIdx)
            val name = cursor.getString(nameColIdx)

            contactsMap["number"] = number
            contactsMap["name"] = name

            contactsList.add(contactsMap)
        }
        cursor.close()
        return contactsList
    }

}