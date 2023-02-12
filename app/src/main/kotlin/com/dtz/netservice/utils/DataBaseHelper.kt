package com.dtz.netservice.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dtz.netservice.data.model.Calls
import com.dtz.netservice.data.model.Location
import com.dtz.netservice.data.model.NotificationMessages

val DATABASENAME = "CRIT"
val TABLE_POSITION = "Positions"
val COL_POSITION_ID = "id"
val COL_POSITION_LATITUDE = "latitude"
val COL_POSITION_LONGITUDE = "longitude"
val COL_POSITION_ADDRESS = "address"
val COL_POSITION_DATETIME = "dateTime"
val COL_POSITION_UUID = "uuid"

val TABLE_NOTIFICATION = "Notifications"
val COL_NOTIFICATION_ID = "id"
val COL_NOTIFICATION_TEXT = "text"
val COL_NOTIFICATION_TITLE = "title"
val COL_NOTIFICATION_TYPE = "type"
val COL_NOTIFICATION_DATETIME = "dateTime"
val COL_NOTIFICATION_IMAGENAME = "nameImage"
val COL_NOTIFICATION_URLIMAGE = "urlImage"
val COL_NOTIFICATION_UUID = "uuid"

val TABLE_CALL_LOG = "call_logs"
val COL_CALL_LOG_ID = "id"
val COL_CALL_LOG_CONTACT = "contact"
val COL_CALL_LOG_PHONE_NUMBER = "phoneNumber"
val COL_CALL_LOG_DATETIME = "datetime"
val COL_CALL_LOG_DURATION = "duration"
val COL_CALL_LOG_TYPE = "type"


class DataBaseHelper(var context: Context) : SQLiteOpenHelper(context, DATABASENAME, null, 1) {
    override fun onCreate(p0: SQLiteDatabase?) {
        // create position table
        val createPositionTable =
            "CREATE TABLE $TABLE_POSITION ($COL_POSITION_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COL_POSITION_LATITUDE VARCHAR(256) , $COL_POSITION_LONGITUDE VARCHAR(256), $COL_POSITION_ADDRESS VARCHAR(256), $COL_POSITION_DATETIME VARCHAR(256), $COL_POSITION_UUID VARCHAR(256) )"
        p0?.execSQL(createPositionTable)

        // create notification table
        val createNotificationTable =
            "CREATE TABLE $TABLE_NOTIFICATION ($COL_NOTIFICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_NOTIFICATION_TEXT VARCHAR(256), $COL_NOTIFICATION_TITLE VARCHAR(256), $COL_NOTIFICATION_TYPE VARCHAR(256), $COL_NOTIFICATION_DATETIME VARCHAR(256), $COL_NOTIFICATION_IMAGENAME VARCHAR(256), $COL_NOTIFICATION_URLIMAGE VARCHAR(256), $COL_NOTIFICATION_UUID VARCHAR(256) )"
        p0?.execSQL(createNotificationTable)

        // create callLogs table
        val createCallLogsTable =
            "CREATE TABLE $TABLE_CALL_LOG ($COL_CALL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_CALL_LOG_CONTACT VARCHAR(256), $COL_CALL_LOG_PHONE_NUMBER VARCHAR(256), $COL_CALL_LOG_DATETIME VARCHAR(256), $COL_CALL_LOG_DURATION VARCHAR(256), $COL_CALL_LOG_TYPE VARCHAR(256) )"
        p0?.execSQL(createCallLogsTable)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    fun insertPosition(location: Location) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_POSITION_LATITUDE, location.latitude)
        contentValues.put(COL_POSITION_LONGITUDE, location.longitude)
        contentValues.put(COL_POSITION_ADDRESS, location.address)
        contentValues.put(COL_POSITION_DATETIME, location.dateTime)
        contentValues.put(COL_POSITION_UUID, location.uuid)
        database.insert(TABLE_POSITION, null, contentValues)
    }

    fun insertNotification(notificationMessages: NotificationMessages) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_NOTIFICATION_TEXT, notificationMessages.text)
        contentValues.put(COL_NOTIFICATION_TITLE, notificationMessages.title)
        contentValues.put(COL_NOTIFICATION_TYPE, notificationMessages.type)
        contentValues.put(COL_NOTIFICATION_DATETIME, notificationMessages.dateTime)
        contentValues.put(COL_NOTIFICATION_IMAGENAME, notificationMessages.nameImage)
        contentValues.put(COL_NOTIFICATION_URLIMAGE, notificationMessages.urlImage)
        contentValues.put(COL_NOTIFICATION_UUID, notificationMessages.uuid)

        database.insert(TABLE_NOTIFICATION, null, contentValues)
    }

    fun readPositionData(): MutableList<Location> {
        val list: MutableList<Location> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $TABLE_POSITION"
        val result = db.rawQuery(query, null)
        try {
            if (result.moveToFirst()) {
                do {
                    val location = Location()
                    location.latitude =
                        result.getString(result.getColumnIndexOrThrow(COL_POSITION_LATITUDE))
                            .toDouble()
                    location.longitude =
                        result.getString(result.getColumnIndexOrThrow(COL_POSITION_LONGITUDE))
                            .toDouble()
                    location.address =
                        result.getString(result.getColumnIndexOrThrow(COL_POSITION_ADDRESS))
                    location.dateTime =
                        result.getString(result.getColumnIndexOrThrow(COL_POSITION_DATETIME))
                    location.uuid =
                        result.getString(result.getColumnIndexOrThrow(COL_POSITION_UUID))
                    list.add(location)
                } while (result.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        result.close()
        return list
    }

    fun deleteOnePositionData(uuid: String): Int {
        val db = this.readableDatabase
        return db.delete(TABLE_POSITION, "uuid=?", arrayOf(uuid))
    }

    fun readNotificationData(): MutableList<NotificationMessages> {
        val list: MutableList<NotificationMessages> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $TABLE_NOTIFICATION"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val notificationMessages = NotificationMessages()
                notificationMessages.text =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_TEXT))
                notificationMessages.title =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_TITLE))
                notificationMessages.type =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_TYPE)).toInt()
                notificationMessages.dateTime =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_DATETIME))
                notificationMessages.nameImage =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_IMAGENAME))
                notificationMessages.urlImage =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_URLIMAGE))
                notificationMessages.uuid =
                    result.getString(result.getColumnIndexOrThrow(COL_NOTIFICATION_UUID))

                list.add(notificationMessages)
            } while (result.moveToNext())
        }
        result.close()
        return list
    }

    fun deleteOneNotificationData(uuid: String): Int {
        val db = this.readableDatabase
        return db.delete(TABLE_NOTIFICATION, "uuid=?", arrayOf(uuid))
    }


    fun readCallLogsData(): MutableList<Calls> {
        val list: MutableList<Calls> = ArrayList()
        val db = this.readableDatabase
        val query = "Select * from $TABLE_CALL_LOG"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val call = Calls()
                call.contact = result.getString(result.getColumnIndexOrThrow(COL_CALL_LOG_CONTACT))
                call.phoneNumber =
                    result.getString(result.getColumnIndexOrThrow(COL_CALL_LOG_PHONE_NUMBER))
                call.dateTime =
                    result.getString(result.getColumnIndexOrThrow(COL_CALL_LOG_DATETIME))
                call.duration =
                    result.getString(result.getColumnIndexOrThrow(COL_CALL_LOG_DURATION))
                call.type =
                    result.getString(result.getColumnIndexOrThrow(COL_CALL_LOG_TYPE)).toInt()
                list.add(call)
            } while (result.moveToNext())
        }
        result.close()
        return list
    }

    fun insertCallLog(calls: Calls) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_CALL_LOG_CONTACT, calls.contact)
        contentValues.put(COL_CALL_LOG_PHONE_NUMBER, calls.phoneNumber)
        contentValues.put(COL_CALL_LOG_DATETIME, calls.dateTime)
        contentValues.put(COL_CALL_LOG_DURATION, calls.duration)
        contentValues.put(COL_CALL_LOG_TYPE, calls.type)

        val result = database.insert(TABLE_CALL_LOG, null, contentValues)
        if (result == (0).toLong()) {
            print("Appel enregistré")
        } else {
            print("Appel non enregistré")
        }
    }
}