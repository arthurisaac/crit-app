package com.dtz.netservice.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.widget.Toast
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.services.connect.ConnectService
import com.dtz.netservice.utils.Consts.CONNECTIVITY
import javax.inject.Inject

/**
 * Created by luis rafael on 13/03/18.
 */
class ConnectReceiver : BroadcastReceiver() {


    @Inject
    lateinit var firebase: InterfaceFirebase

    override fun onReceive(context: Context, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val noConnectivity: Boolean = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            if (noConnectivity) {
                context.setIntentType(false)
                //Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
            } else {
                context.setIntentType(true)
                //Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Context.setIntentType(type: Boolean) {
        val myIntent = Intent(this, ConnectService::class.java)
        myIntent.putExtra(CONNECTIVITY, type)
        startService(myIntent)
    }

}