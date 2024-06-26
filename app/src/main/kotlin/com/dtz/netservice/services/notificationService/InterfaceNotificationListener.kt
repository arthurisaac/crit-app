package com.dtz.netservice.services.notificationService

import android.graphics.Bitmap

/**
 * Created by luis rafael on 27/03/19.
 */
interface InterfaceNotificationListener {

    fun setRunService(run : Boolean)
    fun getNotificationExists(id:String,exec:() -> Unit)
    fun setDataMessageNotification(type:Int,text:String?,title:String?,nameImage:String?,image:Bitmap?)
    fun setSavedMessageNotification(type:Int,text:String?,title:String?,nameImage:String?,image:String?)

}