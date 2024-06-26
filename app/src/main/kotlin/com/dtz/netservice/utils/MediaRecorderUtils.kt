package com.dtz.netservice.utils

import android.media.MediaRecorder
import android.media.MediaRecorder.OnErrorListener
import android.util.Log
import com.pawegio.kandroid.e

/**
 * Created by luis rafael on 21/03/19.
 */
class MediaRecorderUtils(private val errorAction: () -> Unit) : MediaRecorder() {

    fun startRecording(audioSource: Int,fileName:String?){
        try {
            setAudioSource(audioSource)
            setOutputFormat(OutputFormat.THREE_GPP)
            setAudioEncoder(AudioEncoder.AMR_NB)
            setOutputFile(fileName)

            val errorListener = OnErrorListener { _, _, _ -> errorAction() }
            setOnErrorListener(errorListener)

            //reset()
            prepare()
            start()
        } catch (er: Throwable) {
            e(Consts.TAG, er.message.toString())
            errorAction()
        }
    }

    fun stopRecording(sendFile : () -> Unit){
        try {
            stop()
            sendFile()
        } catch (e: Throwable) {
            e(Consts.TAG, e.message.toString())
            errorAction()
        }
    }

}