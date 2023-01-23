package com.dtz.netservice.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import com.dtz.netservice.R
import com.dtz.netservice.utils.Consts.ADDRESS_AUDIO_CALLS
import com.dtz.netservice.utils.Consts.ADDRESS_AUDIO_RECORD
import com.dtz.netservice.utils.Consts.ADDRESS_IMAGE
import com.dtz.netservice.utils.Consts.TAG
import com.pawegio.kandroid.e
import com.pawegio.kandroid.longToast
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

/**
 * Created by luis rafael on 20/03/18.
 */
object LocationHelper {

    private lateinit var locationManager: LocationManager

    //location
    private fun Context.getLocation(): Location? {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        return null;
    }

}