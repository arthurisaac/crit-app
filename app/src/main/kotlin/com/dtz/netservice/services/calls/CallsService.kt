package com.dtz.netservice.services.calls

import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.aykuttasil.callrecord.CallRecord
import com.dtz.netservice.services.base.BaseService
import com.dtz.netservice.utils.Consts.COMMAND_TYPE
import com.dtz.netservice.utils.Consts.PHONE_NUMBER
import com.dtz.netservice.utils.Consts.STATE_CALL_END
import com.dtz.netservice.utils.Consts.STATE_CALL_START
import com.dtz.netservice.utils.Consts.STATE_INCOMING_NUMBER
import com.dtz.netservice.utils.Consts.TYPE_CALL
import java.io.File
import javax.inject.Inject

/**
 * Created by luis rafael on 13/03/18.
 */
class CallsService : BaseService(), InterfaceServiceCalls {

    private var phoneNumber: String? = null
    private var callType = 0

    @Inject
    lateinit var interactor: InterfaceInteractorCalls<InterfaceServiceCalls>

    override fun onCreate() {
        super.onCreate()
        if (getComponent() != null) {
            getComponent()!!.inject(this)
            interactor.onAttach(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.setCallIntent()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun Intent.setCallIntent() {

        val commandType = getIntExtra(COMMAND_TYPE, 0)

        if (commandType != 0) {
            when (commandType) {
                STATE_INCOMING_NUMBER -> if (phoneNumber == null) {
                    phoneNumber = getStringExtra(PHONE_NUMBER)
                    callType = getIntExtra(TYPE_CALL,0)
                }
                STATE_CALL_START -> if (phoneNumber != null) interactor.startRecording(phoneNumber,callType)
                STATE_CALL_END -> {
                    phoneNumber = null
                    interactor.stopRecording()
                }
            }
        }
    }

    override fun stopServiceCalls() {
        stopSelf()
    }

    override fun onDestroy() {
        interactor.onDetach()
        clearDisposable()
        super.onDestroy()
    }
}